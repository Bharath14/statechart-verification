package symbolic_execution.se_tree;

import ast.*;

import java.util.Map;

public class DecisionNode extends SETNode{
    
    //public final ast.Expression;
    public SymbolicExpression value;
    
    public DecisionNode(SymbolicExpression value, SETNode leaf){
        super(leaf);
        this.value = value;
    }

    // Has a variable called value which is of the type SymbolicExpression
}
