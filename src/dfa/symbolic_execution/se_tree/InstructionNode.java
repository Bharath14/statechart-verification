package symbolic_execution.se_tree;

import ast.*;

import java.util.Map;
import java.util.HashMap;


public class InstructionNode extends SETNode{

    //public final InstructionStatement s;
    public final SymbolicExpression expression;
    public final Declaration variable;
    public final Map<Declaration, SymbolicExpression> updates = new HashMap<Declaration, SymbolicExpression>();

    public InstructionNode(Declaration d, Expression e, SETNode leaf){
        super(leaf);
        this.variable = d;
        this.expression = evaluate_exp(e);
       
        this.updates.put(this.variable, this.expression);

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
            SymVars n = new SymVars(this.variable.vname+"_1", this.variable.getType());
            SymbolicExpression exp = new SymbolicExpression(n, " ");
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

   
    // A map of <variable, SymbolicExpression>
    
}
