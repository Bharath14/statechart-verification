package symbolic_execution;

import ast.*;
import set.SETNode;

import java.util.List;

public class SymbolicExecutionResult{
    
    private  List<SETNode> done;
    private  List<SETNode> live;

    public SymbolicExecutionResult(){
        
    }



    public List<SETNode> getDoneNodes(){
        return done;
    }

    public List<SETNode> getLiveNodes(){
        return live;
    }
}
