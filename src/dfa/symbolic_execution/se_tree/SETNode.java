package symbolic_execution.se_tree;

//import ast.*;

public class SETNode{
    
    public final SETNode parent;
    public Integer depth;
    public Integer treedepth;

    public SETNode(SETNode parent){
        this.parent = parent;
        if(parent != null)
        {
            this.depth = parent.depth;
            this.treedepth = parent.treedepth+1;
        }
        else
        {
            this.depth = 0;
            this.treedepth = 0;
        }
    }

} 