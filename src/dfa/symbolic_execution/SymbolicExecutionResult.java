package symbolic_execution;

import symbolic_execution.se_tree.SETNode;

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

    public void setDoneNodes(List<SETNode> d)
    {
        done = d;
    }
    public void setLiveNodes(List<SETNode> l)
    {
        live = l;
    }
}
