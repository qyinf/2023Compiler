package IR;

import AST.ASTNode;

public class IRLabel implements IRSymbol {
    private int id;
    private boolean isGlobal;

    private ASTNode forStmt2;

    public IRLabel(int tmp1,boolean tmp2) {
        this.id = tmp1;
        this.isGlobal = tmp2;
    }

    //Global设置
    public void setGlobal(boolean tmp) {
        this.isGlobal = tmp;
    }

    public boolean getGlobal() {
        return this.isGlobal;
    }


    public void setForStmt2(ASTNode tmp) {
        this.forStmt2 = tmp;
    }

    public ASTNode getForStmt2() {
        return this.forStmt2;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "#" + id;
    }
}
