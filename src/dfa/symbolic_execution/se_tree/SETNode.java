package symbolic_execution.se_tree;

//import ast.*;

public class SETNode{
    
    public final SETNode parent;
    public Integer depth = 0;

    public SETNode(SETNode parent){
        this.parent = parent;
        if(parent != null)
        {
            this.depth = parent.depth;
        }
    }

} 