package symbolic_execution;

import ast.*;
import symbolic_execution.se_tree.DecisionNode;
import symbolic_execution.se_tree.InstructionNode;
import symbolic_execution.se_tree.SymbolicExpression;
import symbolic_execution.se_tree.SETNode;
import symbolic_execution.se_tree.StateEntryNode;
import symbolic_execution.se_tree.StateExitNode;
import symbolic_execution.se_tree.SymVars;
//import symbolic_execution.SymbolicExecutionResult;

import java.util.*;
/*import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;*/
import java.io.IOException;
//import javafx.util.Pair;

public class SymbolicExecutionEngine{
    
    private final Statechart statechart;
    private final Integer max_depth ;
    private List<State> conf = new ArrayList<State>(); 

    public SymbolicExecutionEngine(Statechart statechart, Integer max_depth){
            this.statechart = statechart;
            this.max_depth = max_depth;
    }
    public Integer get_depth()
    {
        return this.max_depth;
    }

    public SETNode initialize_variables(State st, String scope, SETNode leaf)
    {
        for(Declaration d: st.declarations)
        {
            if(d.scope.name.equals(scope))
            {
                if(d.typeName.name.equals("int"))
                {
                    IntegerConstant integer = new IntegerConstant(0);
                    leaf = new InstructionNode(d, integer, leaf );
                }
                else if(d.typeName.name.equals("string"))
                {
                    StringLiteral string = new StringLiteral(" ");
                    leaf = new InstructionNode(d, string, leaf );
                }
                else if(d.typeName.name.equals("boolean"))
                {
                    BooleanConstant bool = new BooleanConstant(false);
                    leaf = new InstructionNode(d, bool, leaf );
                }        
            }
        }
        return leaf;
    }

    public List<State> get_all_states(State s)
    {
        List<State> all_states = new ArrayList<State>();
        all_states.add(s);
        if(s.states.isEmpty())
        {
            return all_states;
        }
        State temp =s;
        for(State st: temp.states)
        {
            all_states.addAll(get_all_states(st));
        }
        return all_states;
    }

    public List<Transition> compute_outts(List<State> conf, List<Transition> all_ts)
    {
        List<Transition> out_ts = new ArrayList<Transition>();
        for(Transition tr :all_ts)
        {
            for(State st: conf)
            {
                if(tr.getSource().name.equals(st.name))
                {
                    out_ts.add(tr);
                }
            }
        }
        return out_ts;
    }

    public SymbolicExecutionResult enterSuperstate(State s, SETNode leaf)
    {
        //System.out.println("enterSuperstate");
        leaf = new StateEntryNode(s, leaf);
        leaf = initialize_variables(s, "local", leaf);
        //System.out.println(s.entry);
        System.out.println("Enter State :");
        System.out.println(s.name);
        return executeBlock(s.entry, leaf);
    }

    public  SymbolicExecutionResult enterSubstate(State s, SETNode leaf)
    {
        leaf = new StateEntryNode(s, leaf);
        leaf = initialize_variables(s, "local", leaf);
        leaf = initialize_variables(s, "parameter", leaf);
        System.out.println("Enter State :");
        System.out.println(s.name);
        return executeBlock(s.entry, leaf);
    }
    
    public List<SETNode> execute()
    {
        //initialise all static variables to v.type.init
        SETNode startnode = new SETNode(null);
        List<State> all_states = get_all_states(this.statechart);
        List<Transition> all_ts = new ArrayList<Transition>();
        for(State st: all_states)
        {
            startnode = initialize_variables(st, "static", startnode);
            all_ts.addAll(st.transitions);
        }
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();
        SymbolicExecutionResult res = new SymbolicExecutionResult();
        leaves.add(startnode);

        this.conf.add(this.statechart);
        State start = this.statechart.states.get(0);
        this.conf.add(start);
        while(!start.states.isEmpty())
        {
            start = start.states.get(0);
            this.conf.add(start);
        }
        for(State s: this.conf)
        { 
            List<SETNode> leaves_temp1 = new ArrayList<SETNode>();
            for(SETNode leaf: leaves)
            { 
                res = this.enterSubstate(s, leaf);
                done.addAll(res.getDoneNodes());
            
                if(res.getLiveNodes().isEmpty())
                {
                    break;
                }
                else
                {
                    leaves_temp1.addAll(res.getLiveNodes());
                }
            }
            leaves = leaves_temp1;
        }

        List<Transition> out_ts = new ArrayList<Transition>();
        out_ts = compute_outts(this.conf, all_ts);
        String s = " ";
        while(!leaves.isEmpty())
        {
            //out_ts = outgoing transitions from conf
            List<SETNode> leaves_temp2 = new ArrayList<SETNode>() ;
            for(SETNode leaf: leaves)
            {
                for(Transition t : out_ts)
                {
                    System.out.println(t.name);
                    System.out.println(t.guard);
                    Solver solver = new Solver(sym_eval(t.guard, leaf));
                    try
                    {
                        s = solver.solve();
                    }
                    catch(IOException i)
                    {
                        System.out.println("Error");
                    }
                    if(s.equals("sat"))
                    {
                        res = takeTransition(t, leaf);
                        done.addAll(res.getDoneNodes());
                        leaves_temp2.addAll(res.getLiveNodes());
                    }
                    else
                    {
                        done.add(leaf);
                    }
                }
            }
            out_ts = compute_outts(this.conf, all_ts);
            leaves = leaves_temp2;
        }

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res.getDoneNodes();
    }

    public  SymbolicExecutionResult exitState(State s, SETNode leaf)
    {
        leaf = new StateExitNode(s, leaf);
        System.out.println("Exit State :");
        System.out.println(s.name);
        return executeBlock(s.exit, leaf);
    }
    public  SymbolicExecutionResult takeTransition(Transition t, SETNode leaf)
    {
        List<SETNode> leaves = new ArrayList<SETNode>();
        leaves.add(leaf);
        List<SETNode> done = new ArrayList<SETNode>();

        /*compute t . sources and t . destination as per StaBL semantics .
        This will include possibly taking into account history
        states in the t . destinations list . For example , if a
        composite state d in t . destinations has history mark , then
        subsequent destination substates should be identified as
        the last active substate at the time of the last exit
        from d .*/

        State source = t.getSource();
        List<State> source_states = new ArrayList<State>();
        source_states.add(source);
        while(!source.getSuperstate().name.equals(t.getState().name))
        {
            source = source.getSuperstate();
            source_states.add(source);
        }
        List<SETNode> leaves_1 = new ArrayList<SETNode>();
        for(State s: source_states)
        {
            for(SETNode l: leaves)
            {
                SymbolicExecutionResult res = exitState(s, l);
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
            this.conf.remove(s);
        }
        leaves.clear();

        leaves.addAll(leaves_1);
        leaves_1.clear();
        for(SETNode l : leaves)
        {
            SymbolicExecutionResult res = executeBlock(t.action, l);
            done.addAll(res.getDoneNodes());
            leaves_1.addAll(res.getLiveNodes());
        }
        leaves.clear();

        leaves.addAll(leaves_1);

        State destination = t.getDestination();
        List<State> destination_Superstates = new ArrayList<State>();
        destination_Superstates.add(destination);
        while(!destination.getSuperstate().name.equals(t.getState().name))
        {
            destination = destination.getSuperstate();
            destination_Superstates.add(destination);
        }
        leaves_1.clear();
        for(State d: destination_Superstates)
        {
            for(SETNode l: leaves)
            {
                SymbolicExecutionResult res = enterSuperstate(d, l);
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
            this.conf.add(d);
        }
        leaves.clear();

        leaves.addAll(leaves_1);

        destination = t.getDestination();
        List<State> destination_Substates = new ArrayList<State>();
        while(!destination.states.isEmpty())
        {
            destination = destination.states.get(0);
            destination_Substates.add(destination);
        }
        leaves_1.clear();
        for(State d: destination_Substates)
        {
            for(SETNode l: leaves)
            {
                SymbolicExecutionResult res = enterSubstate(d, l);
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
            this.conf.add(d);
        }
        if(!destination_Substates.isEmpty())
        {
            leaves.clear();

            leaves.addAll(leaves_1);
        }
        SymbolicExecutionResult res = new SymbolicExecutionResult();

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    public SymbolicExecutionResult executeStatement(Statement s, List<SETNode> leaves)
    {
        SymbolicExecutionResult res = new SymbolicExecutionResult();
        if(s instanceof InstructionStatement)
        {
            for(SETNode l : leaves)
            {
                //System.out.println(s);
                res = executeInstruction(s, l);
            }
        }
        else if(s instanceof IfStatement)
        {
            for(SETNode l : leaves)
            {
                System.out.println("if");
                res = executeIf(s, l);
            }
        }
        else if(s instanceof StatementList)
        {
            for(SETNode l : leaves)
            {
                System.out.println("list");
                res = executeBlock(s, l);
            }
        }
        else
        {
            res.setDoneNodes(leaves);
        }
        return res;
    }
    public  SymbolicExecutionResult executeBlock(Statement block, SETNode leaf)
    {
        SymbolicExecutionResult res = new SymbolicExecutionResult();
        List<SETNode> leaves = new ArrayList<SETNode>();
        //List<SETNode> leaves_1 = new ArrayList<SETNode>();
        leaves.add(leaf);
        List<SETNode> done = new ArrayList<SETNode>();
        //System.out.println("block");
        if(block instanceof StatementList)
        {
            StatementList statementlist = (StatementList)block;

            for(Statement s: statementlist.getStatements())
            {
                res = executeStatement(s, leaves);
                done.addAll(res.getDoneNodes());
                leaves = res.getLiveNodes();
            }
        }
        else
        {
                res = executeStatement(block, leaves);
                done.addAll(res.getDoneNodes());
                leaves = res.getLiveNodes();
        }
        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    public  SymbolicExecutionResult executeInstruction(Statement i, SETNode leaf)
    {
        //SETNode l = new DecisionNode();
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();
        if(i instanceof AssignmentStatement)
        {
            System.out.println("Assignment Statement");
            SETNode l = new InstructionNode(((AssignmentStatement)i).lhs.getDeclaration(),((AssignmentStatement)i).rhs, leaf );
            //System.out.println(InstructionNode.updates.get(((AssignmentStatement)i).lhs.getDeclaration().vname));
            if(leaf.depth +1 > max_depth)
            {
                done.add(l);
            }
            else{
                leaves.add(l);
            }
        }
        if(i instanceof HaltStatement)
        {
            done.add(leaf);
        }

        SymbolicExecutionResult res = new SymbolicExecutionResult();

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    public Expression sym_eval(Expression e, SETNode leaf)
    {
        Expression expr;

        if(e instanceof BinaryExpression)
        {
            Expression left = sym_eval(((BinaryExpression)e).left, leaf);
            Expression right = sym_eval(((BinaryExpression)e).right, leaf);
            String operator = ((BinaryExpression)e).operator;

            expr = new BinaryExpression(left, right, operator);
            return expr;
        }
        else if (e instanceof SymbolicExpression)
        {
            if(((SymbolicExpression)e).right != null)
            {
                Expression left = sym_eval(((SymbolicExpression)e).left, leaf);
                Expression right = sym_eval(((SymbolicExpression)e).right, leaf);
                String operator = ((SymbolicExpression)e).operator;

                expr = new SymbolicExpression(left, right, operator);
                return expr;
            }
            else
            {
                Expression left = sym_eval(((SymbolicExpression)e).left, leaf);
                String operator = ((SymbolicExpression)e).operator;

                expr = new SymbolicExpression(left,operator);
                return expr;
            }
        }
        else if(e instanceof BooleanConstant || e instanceof IntegerConstant || e instanceof StringLiteral)
        {
            return e;
        }
        else if(e instanceof Name)
        {
            List<Object> p = backtrack(e, leaf);
            expr = (Expression)p.get(0);
            leaf = (SETNode)p.get(1);
            expr = sym_eval(expr, leaf);
            return expr;
        }
        else if(e instanceof SymVars)
        {
            return e;
        }
        return null;
    }
    public List<Object> backtrack(Expression e , SETNode leaf)
    {
        if(leaf instanceof InstructionNode)
        {
            //String s = ((Name)e).getDeclaration().vname;
            SymbolicExpression exp = ((InstructionNode)leaf).updates.get(((Name)e).getDeclaration());
            if(exp == null)
            {
                if(leaf.parent != null)
                {
                    leaf = leaf.parent;
                    return backtrack(e, leaf);
                }
                else
                {
                    return Arrays.asList(null, leaf.parent);
                }
            }
            else
            {
                return Arrays.asList(exp, leaf.parent);
            }
        }
        else
        {
            if(leaf.parent != null)
            {
                leaf = leaf.parent;
                return backtrack(e, leaf);
            }
            else
            {
                return Arrays.asList(null, leaf.parent);
            }
        }
    }
    public SymbolicExecutionResult executeIf(Statement i, SETNode leaf)
    {
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();
        Expression symeval = sym_eval(((IfStatement)i).condition, leaf);
        SymbolicExpression else_condition = new SymbolicExpression(((IfStatement)i).condition, "!");
        Expression symeval_else = sym_eval(else_condition, leaf);

        Solver solver = new Solver(symeval);
        String satisfiable = new String();
        try
        {
            satisfiable = solver.solve();
            //System.out.println(s);
        }
        catch(IOException exception)
        {
            System.out.println("Error");
        }

        if(satisfiable.equals("sat"))
        {
            SETNode leaf_1 = new DecisionNode(((IfStatement)i).condition, leaf);
            SymbolicExecutionResult res_1 = executeBlock(((IfStatement)i).then_body, leaf_1);
            done.addAll(res_1.getDoneNodes());
            leaves.addAll(res_1.getLiveNodes());
        }
        else
        {
            done.add(leaf);
        }
        

        Solver solver_else = new Solver(symeval_else);
        String else_satisfiable = new String();
        try
        {
            else_satisfiable = solver_else.solve();
            //System.out.println(s);
        }
        catch(IOException exception)
        {
            System.out.println("Error");
        }
        if(else_satisfiable.equals("sat"))
        {
            SETNode leaf_1 = new DecisionNode(((IfStatement)i).condition, leaf);
            SymbolicExecutionResult res_2 = executeBlock(((IfStatement)i).else_body, leaf_1);
            done.addAll(res_2.getDoneNodes());
            leaves.addAll(res_2.getLiveNodes());
        }
        else
        {
            done.add(leaf);
        }

        if(leaves.isEmpty())
        {
            SymbolicExecutionResult res = new SymbolicExecutionResult();
            done.add(leaf);
            res.setDoneNodes(done);
            //System.out.println("if");
            //System.out.println(res.getDoneNodes());
            return res;
        }
        else
        {
            SymbolicExecutionResult res = new SymbolicExecutionResult();
            res.setDoneNodes(done);
            res.setLiveNodes(leaves);
            //System.out.println("else");
            //System.out.println(res.getDoneNodes());

            return res;
        }
    }
}
