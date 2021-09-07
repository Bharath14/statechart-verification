package symbolic_execution;

import ast.*;
import set.SETDecisionNode;
import symbolic_execution.se_tree.SETNode;
import symbolic_execution.se_tree.StateEntryNode;
import symbolic_execution.SymbolicExecutionResult;

import java.util.ArrayList;
import java.util.List;

public class SymbolicExecutionEngine{
    
    private Statechart statechart;

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
        for(var v : s.vars)
        {
            if(v  == local)
            {
                leaf = new SETBBNode(v, v.type.init, leaf); 
            }
        }
        return executeBlock(s.entry, leaf);
    }
    public static SymbolicExecutionResult enterSubstate(State s, SETNode leaf)
    {
        leaf = new StateEntryNode(s, leaf);
        for(var v : s.vars)
        {
            if(v  == local || parameter)
            {
                leaf = new SETBBNode(v, v.type.init, leaf); 
            }
        }
        return executeBlock(s.entry, leaf);
    }
    public static SymbolicExecutionResult execute(Statechart S, int max_depth)
    {
        /*initialise all static variables to v.type.init*/
        List<SETNode> done;
        List<SETNode> leaves;
        SymbolicExecutionResult res;
        leaves.add(new StateEntryNode());
        /*conf = compute initial configuration*/
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
        while(!res.getLiveNodes().isEmpty())
        {
            /*out_ts = outgoing transitions from conf*/
            for(SETNode leaf: leaves)
            {
                List<SETNode> leaves_1 ;
                for t in out_ts
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
        return done,leaves;
    }

    public static SymbolicExecutionResult takeTransition(Transition t, SETNode leaf)
    {
        List<SETNode> leaves;
        leaves.add(leaf);
        List<SETNode> done;

        /*compute t . sources and t . destination as per StaBL semantics .
        This will include possibly taking into account history
        states in the t . destinations list . For example , if a
        composite state d in t . destinations has history mark , then
        subsequent destination substates should be identified as
        the last active substate at the time of the last exit
        from d .*/

        State s = t.getSource();
        List<SETNode> leaves_1;
        for(SETNode l: leaves)
        {
            SymbolicExecutionResult res = exitState(s, l);
            done.add(res.getDoneNodes());
            leaves_1.add(res.getLiveNodes());
        }
        leaves = leaves_1;

        leaves_1.clear();
        for(SETNode l : leaves)
        {
            SymbolicExecutionResult res = executeBlock(t.action);
            done.add(res.getDoneNodes());
            leaves_1.add(res.getLiveNodes());
        }
        leaves = leaves_1;

        State d = t.getDestination();
        leaves_1.clear();
        for(SETNode l: leaves)
        {
            SymbolicExecutionResult res = enterSuperstatestate(d, l);
            done.add(res.getDoneNodes());
            leaves_1.add(res.getLiveNodes());
        }
        leaves = leaves_1;

        /*State d = t.getDestination();
        leaves_1.clear();
        for(SETNode l: leaves)
        {
            SymbolicExecutionResult res = enterSubstate(d, l);
            done.add(res.getDoneNodes());
            leaves_1.add(res.getLiveNodes());
        }
        leaves = leaves_1;*/

        return done, leaves;

    }
    public static SymbolicExecutionResult executeStatement(Statement s, f, List<SETNode> leaves)
    {
        List<SETNode> leaves_1 ;
        List<SETNode> done;

        for(SETNode l : leaves)
        {
            SymbolicExecutionResult res = f(s, leaf);
            done.add(res.getDoneNodes());
            leaves_1.add(res.getLiveNodes());
        }

        return done, leaves_1;
    }
    public static SymbolicExecutionResult executeBlock(StatementList a, SETNode leaf)
    {
        SymbolicExecutionResult res
        List<SETNode> leaves;
        leaves.add(leaf);
        List<SETNode> done;

        for(Statement s: a.getStatements())
        {
            if(s = instruction)
            {
                res = executeStatement(s, executeInstruction(), leaves);
            }

            else if(s = if)
            {
                res = executeStatement(s, executeIf(), leaves);
            }

            else if(s = block)
            {
                res = executeStatement(s, executeBlock(), leaves);
            }
        }
        return res;
    }
    public static SymbolicExecutionResult executeInstruction(Statement i, SETNode leaf)
    {
        SETNode l = new SETDecisionNode();
        List<SETNode> done;
        List<SETNode> leaves;

        if(leaf.depth +1 >max_depth)
        {
            done.add(l);
        }
        else{
            leaves.add(l);
        }
        return done, leaves;
    }
    public static SymbolicExecutionResult executeIf(ifstatement, leaf)
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
