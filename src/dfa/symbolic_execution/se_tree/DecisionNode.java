package symbolic_execution.se_tree;

import ast.*;

//import java.util.Map;

public class DecisionNode extends SETNode{
    
    //public final ast.Expression;
    public SymbolicExpression value;
    
    public DecisionNode(Expression value, SETNode leaf){
        super(leaf);
        this.value = evaluate_exp(value);
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
        else if(e instanceof SymbolicExpression)
        {
            return ((SymbolicExpression)e);
        }
        else
        {
            return null;
        }
    }

    // Has a variable called value which is of the type SymbolicExpression
}
