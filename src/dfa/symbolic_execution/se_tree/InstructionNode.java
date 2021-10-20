package symbolic_execution.se_tree;

import ast.*;

import java.util.Map;
import java.util.HashMap;


public class InstructionNode extends SETNode{

    //public final InstructionStatement s;
    public SymbolicExpression expression;
    public String name;
    public static final Map<String, SymbolicExpression> updates = new HashMap<String, SymbolicExpression>();

    public InstructionNode(String s, Expression e, SETNode leaf){
        super(leaf);
        this.name = s;

        if(e instanceof BinaryExpression)
        {
            SymbolicExpression exp = new SymbolicExpression(((BinaryExpression)e).left, ((BinaryExpression)e).right, ((BinaryExpression)e).operator);
            this.expression = exp;
        }
        else if(e instanceof Name)
        {
             SymbolicExpression exp = new SymbolicExpression(e, " ");
             this.expression = exp;
        }
        else if(e instanceof BooleanConstant || e instanceof IntegerConstant || e instanceof StringLiteral)
        {
             SymbolicExpression exp = new SymbolicExpression(e, " ");
             this.expression = exp;
        }
        else if(e instanceof FunctionCall)
        {
            SymVars n = new SymVars(s+"_1");
            SymbolicExpression exp = new SymbolicExpression(n, " ");
            this.expression = exp;
        }
        else if(e instanceof SymbolicExpression)
        {
            this.expression = ((SymbolicExpression)expression);
        }
        else
        {
             this.expression = null;
        }
        InstructionNode.updates.put(this.name, this.expression);

        if(leaf != null)
        {
            this.depth = leaf.depth+1;
        }
    }

   
    // A map of <variable, SymbolicExpression>
    
}
