package symbolic_execution;

import ast.*;
import symbolic_execution.se_tree.DecisionNode;
import symbolic_execution.se_tree.InstructionNode;
import symbolic_execution.se_tree.SETNode;
import symbolic_execution.se_tree.StateEntryNode;
import symbolic_execution.se_tree.StateExitNode;
import symbolic_execution.SymbolicExecutionResult;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

public class SymbolicExecutionEngine{
    
    private Statechart statechart;
    private static Integer max_depth = 100;

    public SymbolicExecutionEngine(Statechart statechart) throws Exception{
        try{
            this.statechart = statechart;
            //this.execute(statechart,100);
        }
        catch (Exception e){
        }
    } 

    // TODO
    /*public execute(Statechart statechart){
        return null;
    }*/

    public static SymbolicExecutionResult enterSuperstate(State s, SETNode leaf)
    {
        leaf = new StateEntryNode(s, leaf);
        for(Declaration v : s.declarations)
        {
            if(v.scope.toString().equals("local"))
            {
                leaf = new InstructionNode(v, v.type.init, leaf); 
            }
        }
        return executeBlock(s.entry, leaf);
    }
    public static SymbolicExecutionResult enterSubstate(State s, SETNode leaf)
    {
        leaf = new StateEntryNode(s, leaf);
        for(Declaration v : s.declarations)
        {
            if(v.scope.toString().equals("local") || v.scope.toString().equals("parameter"))
            {
                leaf = new InstructionNode(v, v.type.init, leaf); 
            }
        }
        return executeBlock(s.entry, leaf);
    }
    public static SymbolicExecutionResult exitState(State s, SETNode leaf)
    {
        leaf = new StateExitNode(s, leaf);
        return executeBlock(s.exit, leaf);
    }
    public static SymbolicExecutionResult execute(Statechart S, int max_depth)
    {
        /*initialise all static variables to v.type.init*/
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();
        SymbolicExecutionResult res;
        leaves.add(new SETNode(null));
        /*conf = compute initial configuration*/
        List<State> conf;
        State start = S.states.get(0);
        conf.add(start);
        while(start.states != null)
        {
            start = start.states.get(0);
            conf.add(start);
        }

        for(State s: conf)
        {
            for(SETNode leaf: leaves)
            {
                res = SymbolicExecutionEngine.enterSuperstate(s, leaf);
                done.addAll(res.getDoneNodes());
                if(res.getLiveNodes().isEmpty())
                {
                    break;
                }
                else
                {
                    leaves.addAll(res.getLiveNodes());
                }
            }
        }
        List<Transition> ts = S.transitions;
        List<Transition> out_ts;
        for(Transition tr :ts)
        {
            for(State st: conf)
            {
                if(tr.getSource() == st)
                {
                    out_ts.add(tr);
                    break;
                }
            }
        }
        while(!res.getLiveNodes().isEmpty())
        {
            /*out_ts = outgoing transitions from conf*/
            for(SETNode leaf: leaves)
            {
                List<SETNode> leaves_1 ;
                for(Transition t : out_ts)
                {
                    if(t.guard is satisfiable)
                    {
                        res = takeTransition(t, leaf);
                        done.addAll(res.getDoneNodes());
                        leaves_1.addAll(res.getLiveNodes());
                    }
                }
                leaves.addAll(leaves_1);
            }
        }

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }

    public static SymbolicExecutionResult takeTransition(Transition t, SETNode leaf)
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

        State s = t.getSource();
        List<SETNode> leaves_1 = new ArrayList<SETNode>();
        for(SETNode l: leaves)
        {
            SymbolicExecutionResult res = exitState(s, l);
            done.addAll(res.getDoneNodes());
            leaves_1.addAll(res.getLiveNodes());
        }
        leaves = leaves_1;

        leaves_1.clear();
        for(SETNode l : leaves)
        {
            SymbolicExecutionResult res = executeBlock(t.action, l);
            done.addAll(res.getDoneNodes());
            leaves_1.addAll(res.getLiveNodes());
        }
        leaves = leaves_1;

        State d = t.getDestination();

        if(d.name.equals(s.getSuperstate().name))
        {
            leaves_1.clear();
            for(SETNode l: leaves)
            {
                SymbolicExecutionResult res = enterSuperstate(d, l);
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
            leaves = leaves_1;
        }

        else if(s.name.equals(d.getSuperstate().name))
        {
            leaves_1.clear();
            for(SETNode l: leaves)
            {
                SymbolicExecutionResult res = enterSubstate(d, l);
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
            leaves = leaves_1;
        }
        SymbolicExecutionResult res = new SymbolicExecutionResult();

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    /*public static SymbolicExecutionResult executeStatement(Statement s,Method f , List<SETNode> leaves)
    {
        List<SETNode> leaves_1 ;
        List<SETNode> done;

        for(SETNode l : leaves)
        {
            SymbolicExecutionResult res = f.invoke(s, l);
            done.addAll(res.getDoneNodes());
            leaves_1.addAll(res.getLiveNodes());
        }

        SymbolicExecutionResult res;

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }*/
    public static SymbolicExecutionResult executeBlock(Statement s, SETNode leaf)
    {
        SymbolicExecutionResult res = new SymbolicExecutionResult();
        List<SETNode> leaves = new ArrayList<SETNode>();
        leaves.add(leaf);
        List<SETNode> done = new ArrayList<SETNode>();

        //for(Statement s: a.getStatements())
        if(s.getClass().toString().equals("InstructionStatement"))
        {
            for(SETNode l : leaves)
            {
                SymbolicExecutionResult res_1 = executeInstruction(s, l);
                done.addAll(res_1.getDoneNodes());
                leaves.addAll(res_1.getLiveNodes());
            }
        }
        else if(s.getClass().toString().equals("IfStatement"))
        {
            for(SETNode l : leaves)
            {
                SymbolicExecutionResult res_2 = executeIf(s, l);
                done.addAll(res_2.getDoneNodes());
                leaves.addAll(res_2.getLiveNodes());
            }
        }
        else if(s.getClass().toString().equals("Statement"))
        {
            for(SETNode l : leaves)
            {
                SymbolicExecutionResult res_3 = executeBlock(s, l);
                done.addAll(res_3.getDoneNodes());
                leaves.addAll(res_3.getLiveNodes());
            }
        }
        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    public static SymbolicExecutionResult executeInstruction(Statement i, SETNode leaf)
    {
        SETNode l = new DecisionNode();
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();

        if(leaf.depth +1 > max_depth)
        {
            done.add(l);
        }
        else{
            leaves.add(l);
        }

        SymbolicExecutionResult res = new SymbolicExecutionResult();

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    public static SymbolicExecutionResult executeIf(Statement i, SETNode leaf)
    {
        
    }

    /*
    // Functions need implementation
    public static SymbolicExecutionResult takeTransition();
    public static SymbolicExecutionResult enterState();
    public static SymbolicExecutionResult exitState();
    public static SymbolicExecutionResult executeStatement();
    */

}
