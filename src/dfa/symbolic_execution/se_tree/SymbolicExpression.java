package symbolic_execution.se_tree;
import ast.Expression;

//import java.util.Map;
//import java.util.HashMap;

public class SymbolicExpression extends Expression
{
  public final Expression left;
  public final Expression right;

  public final String operator;

  /*public enum OperatorType {
    PLUS, // "+"
    MUL,  // "*"
    SUB,  // "-"
    DIV,  // "/"

    GE,   // ">="
    GT,   // ">"
    LE,   // "<="
    LT,   // "<"
    NE,   // "!="
    EQ,   // "="

    AND,  // "&&"
    OR    // "||"
  }

  public static final Map<OperatorType, String> operator_string = new HashMap<OperatorType, String>();

  {
    SymbolicExpression.operator_string.put(OperatorType.PLUS, "+");
    SymbolicExpression.operator_string.put(OperatorType.MUL,  "*");
    SymbolicExpression.operator_string.put(OperatorType.SUB,  "-");
    SymbolicExpression.operator_string.put(OperatorType.DIV,  "/");
    SymbolicExpression.operator_string.put(OperatorType.GE,  ">=");
    SymbolicExpression.operator_string.put(OperatorType.GT,   ">");
    SymbolicExpression.operator_string.put(OperatorType.LE,  "<=");
    SymbolicExpression.operator_string.put(OperatorType.LT,   "<");
    SymbolicExpression.operator_string.put(OperatorType.NE,  "!=");
    SymbolicExpression.operator_string.put(OperatorType.EQ,   "=");
    SymbolicExpression.operator_string.put(OperatorType.AND,  "&&");
    SymbolicExpression.operator_string.put(OperatorType.OR,   "||");
  }*/

  public SymbolicExpression(Expression left, Expression right, String operator) {
    this.left     = left;
    this.right    = right;
    this.operator = operator;
  }

  public SymbolicExpression(Expression expression, String operator) {
    this.left = expression;
    this.operator   = operator;
    this.right = null;
  }

  public String toString()
  {
    if(this.right != null && this.left != null)
    {
      String s = this.left.toString() + " " + this.operator + " " + this.right.toString();
      return s;
    }
    else if(this.left!= null)
    {
      String s = this.left.toString() + " " + this.operator;
      return s;
    }
    return "null";
  }
}