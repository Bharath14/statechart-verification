package symbolic_execution.se_tree;

import ast.*;
import java.util.*;

public class SymVars extends Expression
{
    public String value;

    public SymVars(String value)
    {
        this.value = value;
    }
}