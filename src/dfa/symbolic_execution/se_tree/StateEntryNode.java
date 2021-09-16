package symbolic_execution.se_tree;

import ast.*;

public class StateEntryNode extends SETNode{
    
    public final State s;
    
    public StateEntryNode(State s, SETNode leaf){
        super(leaf);
        this.s = s;
    }
}

