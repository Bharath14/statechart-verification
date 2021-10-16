package symbolic_execution;

import symbolic_execution.se_tree.SETNode;

import java.util.*;

public class SymbolicExecutionResult{
    
    private  List<SETNode> done = new ArrayList<SETNode>();
    private  List<SETNode> live = new ArrayList<SETNode>();

    public SymbolicExecutionResult(){
        
    }
    public List<SETNode> getDoneNodes(){
        return done;
    }

    public List<SETNode> getLiveNodes(){
        return live;
    }

    public void setDoneNodes(List<SETNode> d)
    {
        done = d;
    }
    public void setLiveNodes(List<SETNode> l)
    {
        live = l;
    }
}
