package MIPS;

import IR.IRFunc;
import IR.IRSymbol;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class Function {

    private String name;

    private ArrayList<IRSymbol> fParams;

    private HashMap<IRSymbol,Integer> symbolOff;

    //不太理解pos的意思，但是知道要有這個
    private int pos;
    public Function(IRFunc tmp) {
        name = tmp.getName();
        fParams = tmp.getfParams();
        symbolOff = new HashMap<>();
        //TODO symbolOffSet不知道是什么
        pos = 8;
        for (int i = 0;i < fParams.size();i++) {
            symbolOff.put(fParams.get(i),pos);
            pos += 4;
        }
    }

    public void alloc(int tmp) {
        this.pos += tmp;
    }

    public int getPos() {
        return this.pos;
    }

    public int getOff(IRSymbol tmp) {
        return this.symbolOff.get(tmp);
    }


    //在此function中新增
    public boolean getLocal(IRSymbol tmp) {
        return this.symbolOff.containsKey(tmp);
    }

    public void addLocal(IRSymbol tmp) {
        this.symbolOff.put(tmp,pos);
        pos += 4;
    }
}
