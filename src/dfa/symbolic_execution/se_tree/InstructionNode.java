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
        this.expression = evaluate_exp(e);
       
        InstructionNode.updates.put(this.name, this.expression);

        if(leaf != null)
        {
            this.depth = leaf.depth+1;
        }
    }

    public SymbolicExpression evaluate_exp(Expression e)
    {
         if(e instanceof BinaryExpression)
        {
            SymbolicExpression exp = new SymbolicExpression(((BinaryExpression)e).left, ((BinaryExpression)e).right, ((BinaryExpression)e).operator);
            return exp;
        }
        else if(e instanceof Name)
        {
             SymbolicExpression exp = new SymbolicExpression(e, " ");
             return exp;
        }
        else if(e instanceof BooleanConstant || e instanceof IntegerConstant || e instanceof StringLiteral)
        {
             SymbolicExpression exp = new SymbolicExpression(e, " ");
             return exp;
        }
        else if(e instanceof FunctionCall)
        {
            SymVars n = new SymVars(this.name+"_1");
            SymbolicExpression exp = new SymbolicExpression(n, " ");
            return exp;
        }
        else if(e instanceof SymbolicExpression)
        {
            return ((SymbolicExpression)expression);
        }
        else
        {
            return null;
        }
    }

   
    // A map of <variable, SymbolicExpression>
    
}
