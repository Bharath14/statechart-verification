package symbolic_execution;

import ast.Expression;
import ast.Declaration;
import ast.Name;
import ast.BinaryExpression;
import ast.BooleanConstant;
import ast.IntegerConstant;
import ast.StringLiteral;
import ast.Type;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import symbolic_execution.se_tree.SymbolicExpression;
import symbolic_execution.se_tree.SymVars;

public class Solver
{
    Expression expression;
    public List<Declaration> symvars = new ArrayList<Declaration>();
    public Solver(Expression expression)
    {
        this.expression = expression;
        //this.symvars = symvars;
    }

    public String conversion(Expression expression)
    {
        String s = "";
        String formula = formula(expression);
        for (Map.Entry<String, Type> v : SymVars.symvarslist.entrySet()) 
        {
            if(v.getValue().name.equals("int"))
            {
                s = s + "(declare-fun " + v.getKey() + "()" + "Int" + ")" + "\n";
            }
            else if(v.getValue().name.equals("string"))
            {
                s = s + "(declare-fun " + v.getKey() + "()" + "String" + ")" + "\n";
            }
            else if(v.getValue().name.equals("bool"))
            {
                s = s + "(declare-fun " + v.getKey() + "()" + "Bool" + ")" + "\n";
            }
		}

        s = s + "(assert " + formula + ")\n";
		s = s + "(check-sat)\n";
		//s = s + "(get-model)\n";
		s = s + "(exit)";
		return s;

    }
    public String formula(Expression e)
    {
        String s = "";
        if(e instanceof BinaryExpression)
        {
            Expression left = ((BinaryExpression)e).left;
            Expression right = ((BinaryExpression)e).right;
            String operator = ((BinaryExpression)e).operator;
            if(operator.equals("||"))
            {
                s = s + "( or " + formula(left) + " " + formula(right) + ")";
            }
            else if(operator.equals("&&"))
            {
                s = s + "( and " + formula(left) + " " + formula(right) + ")";
            }
            else if(operator.equals("!="))
            {
                s = s +"(not" + "( = " + formula(left) + " " + formula(right) + ")" + ")";
            }
            else
            {
                s = s + "(" + operator + formula(left) + " " + formula(right) + ")";
            }
            return s;
        }
        else if(e instanceof SymbolicExpression)
        {
            Expression left = ((SymbolicExpression)e).left;
            Expression right = ((SymbolicExpression)e).right;
            String operator = ((SymbolicExpression)e).operator;
            if(right != null)
            {
                if(operator.equals("||"))
                {
                    s = s + "( or " + formula(left) + " " + formula(right) + ")";
                }
                else if(operator.equals("&&"))
                {
                    s = s + "( and " + formula(left) + " " + formula(right) + ")";
                }
                else if(operator.equals("!="))
                {
                    s = s +"(not" + "( = " + formula(left) + " " + formula(right) + ")" + ")";
                }
                else
                {
                    s = s + "("+ operator +" "+formula(left) + " " + formula(right) + ")";
                }
            }
            else 
            {
                if(operator.equals("!"))
                {
                    s = s +"(not" +" "+ formula(left) + " " + ")";
                }

                else
                {
                    s = s +formula(left);
                }
            }
            return s;
        }
        else if(e instanceof BooleanConstant || e instanceof IntegerConstant || e instanceof StringLiteral)
        {
            s = s + e.toString();
            return s;
        }
        else if(e instanceof Name)
        {
            s = s + ((Name)e).getDeclaration().vname;
            return s;
        }
        else if(e instanceof SymVars)
        {
            s = s + ((SymVars)e).value;
            return s;
        }
        return s;
    }
    public String solve() throws IOException
    {
        String input = this.conversion(this.expression);
        System.out.println("z3 input :\n" + input);

        FileWriter outFile;

		outFile = new FileWriter("input.smt2");
		PrintWriter out = new PrintWriter(outFile);
		out.println(input);
		out.close();
		String command = "z3 input.smt2";
		String output = this.cmdExec(command);
		System.out.println("z3 output :\n" + output);

        return output;

    }

    public String cmdExec(String command) throws IOException
    {
        String line;
		String output = "";

		Process p = Runtime.getRuntime().exec(command);
		BufferedReader input = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		line = input.readLine();
		while (line != null) {
			output += (line);
			line = input.readLine();
		}
		input.close();

		return output;
    }
}