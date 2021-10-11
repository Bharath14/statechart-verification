package symbolic_execution.se_tree;

import ast.*;

import java.util.Map;
import java.util.HashMap;


public class InstructionNode extends SETNode{

    //public final InstructionStatement s;
    public SymbolicExpression expression;
    public String name;
    public static final Map<String, SymbolicExpression> updates = new HashMap<String, SymbolicExpression>();

    public InstructionNode(String s, SymbolicExpression expression, SETNode leaf){
        super(leaf);
        this.name = s;
        this.expression = expression;
        InstructionNode.updates.put(this.name, this.expression);
    }

   
    // A map of <variable, SymbolicExpression>
    
}
