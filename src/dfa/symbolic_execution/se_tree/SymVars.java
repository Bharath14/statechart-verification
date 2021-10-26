package symbolic_execution.se_tree;

import ast.*;
import java.util.*;

public class SymVars extends Expression
{
    public String value;
    public Type type;
    public static final Map<String, Type> symvarslist =  new HashMap<String, Type>();

    public SymVars(String value, Type type)
    {
        this.value = value;
        this.type = type;
        SymVars.symvarslist.put(this.value, this.type);
    }

    public String toString()
    {
        String s = this.value ;
        return s;
    }
}