package symbolic_execution.se_tree;

import ast.*;

public class TransitionNode extends SETNode{
    
    public final State s;

    public TransitionNode(State s, SETNode leaf){
        super(leaf);
        this.s = s;
    }
}
