package Symbols;

import IR.IRLabel;
import IR.IRSymbol;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {

    private HashMap<String,SymbolVar> symbolVars;
    private HashMap<String,SymbolFunc> symbolFuncs;

    //为了lval专门设置的,只有变量，没有常数
    private ArrayList<SymbolVar> symbolVarList;


    //标记了变量和常数被引用
    private HashMap<SymbolVar, IRLabel> symbolVarRef;
    private HashMap<SymbolFunc,IRLabel> symbolFuncRef;


    //符号表和函数是多对一的关系
    private SymbolFunc nowFunction;

    //符号表的爸爸
    private SymbolTable parent;

    private int parentPos;

    //错误处理用，用于记录当前是否位于循环体
    private boolean inCycle;

    private IRSymbol cycleStart;

    private IRSymbol cycleEnd;


    public SymbolTable() {
        this.symbolVars = new HashMap<>();
        this.symbolVarRef = new HashMap<>();
        this.symbolFuncs = new HashMap<>();
        this.symbolFuncRef = new HashMap<>();
        this.symbolVarList = new ArrayList<>();
        this.inCycle = false;
        this.nowFunction = null;
        this.parent = null;
        this.parentPos = 0;
        this.cycleStart = null;
        this.cycleEnd = null;
    }



    //错误处理的时候用
    public boolean addSymbol(Symbol tmp) {
        if (this.symbolVars.containsKey(tmp.getIdent()) || this.symbolFuncs.containsKey(tmp.getIdent())) {
            return false;
        }
        if (tmp instanceof SymbolVar) {
            this.symbolVars.put(tmp.getIdent(), (SymbolVar) tmp);
            this.symbolVarList.add((SymbolVar) tmp);
            return true;
        }
        else if (tmp instanceof SymbolFunc){
            this.symbolFuncs.put(tmp.getIdent(),(SymbolFunc) tmp);
            return true;
        }
        else {
            System.out.println("出现了奇怪的symbol类型");
            return false;
        }
    }

    //各种设置
    public void setParentInfo(SymbolTable tmp) {
        this.parent = tmp;
        this.parentPos = tmp.getListPos();
    }

    public void setNowFunction(SymbolFunc tmp) {
        this.nowFunction = tmp;
    }

    public void setInCycle(boolean tmp) {
        this.inCycle = tmp;
    }


    //错误处理的时候用
    public boolean getInCycle() {
        SymbolTable tmp = this;
        while (tmp != null && !tmp.inCycle && tmp.nowFunction == null) {
            tmp = tmp.parent;
        }
        if (tmp == null) {
            return false;
        }
        else {
            return tmp.inCycle;
        }
    }

    public SymbolFunc getNowFunction() {
        SymbolTable tmp = this;
        while (tmp != null && tmp.nowFunction == null) {
            tmp = tmp.parent;
        }
        if (tmp == null) {
            return null;
        }
        else {
            return tmp.nowFunction;
        }
    }

    public SymbolFunc getBlockFunction() {
        return this.nowFunction;
    }

    //设置引用
    public void setSymbolVarRef(SymbolVar tmp1,IRLabel tmp2) {
        this.symbolVarRef.put(tmp1,tmp2);
    }

    public void setSymbolFuncRef(SymbolFunc tmp1,IRLabel tmp2) {
        this.symbolFuncRef.put(tmp1,tmp2);
    }


    //符号表查找
    public Symbol lookUpInGlobal(String name) {
        SymbolTable tmp = this;
        while (tmp != null) {
            Symbol res = tmp.lookUpInLocal(name);
            if (res != null) {
                return res;
            }
            tmp = tmp.parent;
        }
        return null;
    }

    public Symbol lookUpInLocal(String name) {
        if (symbolVars.containsKey(name)) {
            return symbolVars.get(name);
        }
        else if (symbolFuncs.containsKey(name)) {
            return symbolFuncs.get(name);
        }
        else {
            return null;
        }
    }

    //感觉上面那个查找有问题

    //var查找
    public SymbolVar lookUpVarGlobal(String name) {
        SymbolTable tmp = this;
        while (tmp != null) {
            SymbolVar res = tmp.lookUpVarLocal(name);
            if (res != null) {
                return res;
            }
            tmp = tmp.parent;
        }
        return null;
    }

    public SymbolVar lookUpVarLocal(String name) {
        return symbolVars.getOrDefault(name, null);
    }

    //func查找
    public SymbolFunc lookUpFuncGlobal(String name) {
        SymbolTable tmp = this;
        while (tmp != null) {
            SymbolFunc res = tmp.lookUpFuncLocal(name);
            if (res != null) {
                return res;
            }
            tmp = tmp.parent;
        }
        return null;
    }

    public SymbolFunc lookUpFuncLocal(String name) {
        return this.symbolFuncs.getOrDefault(name,null);
    }


    //lval专用查找
    public SymbolVar lvalGlobal(String name,int pos) {
        SymbolVar res = lvalLocal(name,pos);
        if (res != null) {
            return res;
        }
        int parentPos = this.parentPos;
        SymbolTable tmp = this.parent;
        while (tmp != null) {
            res = tmp.lvalLocal(name,parentPos);
            if (res != null) {
                return res;
            }
            parentPos = tmp.parentPos;
            tmp = tmp.parent;
        }
        return null;
    }

    public SymbolVar lvalLocal(String name,int pos) {
        for (int i = pos - 1;i >= 0;i--) {
            if (symbolVarList.get(i).getIdent().equals(name)) {
                return symbolVarList.get(i);
            }
        }
        return null;
    }

    //找到某个变量最近一次的引用
    public IRSymbol getGlobalVarRef(SymbolVar name) {
        SymbolTable tmp = this;
        while (tmp != null) {
            IRLabel irLabel = (IRLabel) tmp.getLocalVarRef(name);
            if (irLabel != null) {
                return irLabel;
            }
            tmp = tmp.parent;
        }
        return null;
    }

    public IRSymbol getLocalVarRef(SymbolVar name) {
        return this.symbolVarRef.getOrDefault(name,null);
    }

    //一些还没写好的
    public void setCycle(IRSymbol start,IRSymbol end) {
        this.cycleStart = start;
        this.cycleEnd = end;
    }
    public IRSymbol findCycleEnd() {
        SymbolTable tmp = this;
        while (tmp != null) {
            if (tmp.getCycleEnd() != null) {
                return tmp.getCycleEnd();
            }
            tmp = tmp.parent;
        }
        return null;
    }

    public IRSymbol findCycleStart() {
        SymbolTable tmp = this;
        while (tmp != null) {
            if (tmp.getCycleStart() != null) {
                return tmp.getCycleStart();
            }
            tmp = tmp.parent;
        }
        return null;
    }

    public IRSymbol getCycleEnd() {
        return this.cycleEnd;
    }

    public IRSymbol getCycleStart() {
        return this.cycleStart;
    }

    //专门为了lval的
    public int getListPos() {
        return this.symbolVarList.size();
    }

}
