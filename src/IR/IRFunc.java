package IR;

import AST.ASTNode;

import java.util.ArrayList;

public class IRFunc implements IRSymbol{
    //函数的名字
    private String name;

    //入口
    private IRSymbol entry;

    //形参
    private ArrayList<IRSymbol> fParams;

    public IRFunc(String tmp1,IRSymbol tmp2,ArrayList<IRSymbol> tmp3) {
        this.name = tmp1;
        this.entry = tmp2;
        this.fParams = tmp3;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<IRSymbol> getfParams() {
        return this.fParams;
    }

    @Override
    public String toString() {
        return name + "()";
    }

    @Override
    public int getId() {
        return -1;
    }


}
