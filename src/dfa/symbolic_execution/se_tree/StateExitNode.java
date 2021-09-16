package symbolic_execution.se_tree;

import ast.*;

public class StateExitNode extends SETNode{
    
    public final State s;

    public StateExitNode(State s, SETNode leaf){
        super(leaf);
        this.s = s;
    }
}
