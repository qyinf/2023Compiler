package IR;

import java.util.HashMap;

public class IRLabelManager {
    private int cnt;

    private HashMap<Integer,IRLabel> labelMap;

    public IRLabelManager() {
        this.cnt = 0;
        this.labelMap = new HashMap<>();
    }


    //分配的时候默认是false,可以后续改成true
    public IRLabel alloc() {
        IRLabel tmp = new IRLabel(cnt,false);
        this.labelMap.put(this.cnt,tmp);
        this.cnt++;
        return tmp;
    }
}
