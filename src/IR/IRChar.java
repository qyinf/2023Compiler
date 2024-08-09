package IR;

import java.util.ArrayList;

public class IRChar {
    public static final int ADD = 0, MINU = 1, MULT = 2, DIV = 3, ASSIGN = 4,
            LSHIFT = 5, RSHIFT = 6, GRE = 7, GEQ = 8, LSS = 9, LEQ = 10,
            EQL = 11, NEQ = 12, BR = 13, BZ = 14, CALL = 15, RET = 16,
            EXIT = 17, LOAD = 18, STORE = 19, ALLOCA = 20, GETINT = 21,
            PRINTS = 22, FUNC = 23, LABEL = 24, SETRET = 27,
            PRINTI = 28, BNZ = 29, MOD = 30, RASHIFT = 31, AND = 32;
    private int type;

    private IRSymbol op1;

    private IRSymbol op2;

    private IRSymbol op3;

    //函数参数
    private ArrayList<IRSymbol> list;
    public IRChar(int tmp,IRSymbol tmp1,IRSymbol tmp2,IRSymbol tmp3) {
        this.type = tmp;
        this.op1 = tmp1;
        this.op2 = tmp2;
        this.op3 = tmp3;
    }

    public void setList(ArrayList<IRSymbol> tmp) {
        this.list = tmp;
    }

    public int getType() {
        return this.type;
    }

    public IRSymbol getOp3() {
        return this.op3;
    }

    public IRSymbol getOp2() {
        return this.op2;
    }

    public IRSymbol getOp1() {
        return this.op1;
    }

    public ArrayList<IRSymbol> getList() {
        return this.list;
    }

    @Override
    public String toString() {
        if (type == ADD) {
            return op3.toString() + " = " + op1.toString() + " + " + op2.toString();
        } else if (type == MINU) {
            return op3.toString() + " = " + op1.toString() + " - " + op2.toString();
        } else if (type == MULT) {
            return op3.toString() + " = " + op1.toString() + " * " + op2.toString();
        } else if (type == DIV) {
            return op3.toString() + " = " + op1.toString() + " / " + op2.toString();
        } else if (type == MOD) {
            return op3.toString() + " = " + op1.toString() + " % " + op2.toString();
        } else if (type == LSHIFT) {
            return op3.toString() + " = " + op1.toString() + " << " + op2.toString();
        } else if (type == RSHIFT) {
            return op3.toString() + " = " + op1.toString() + " >> " + op2.toString();
        } else if (type == RASHIFT) {
            return op3.toString() + " = " + op1.toString() + " >>A " + op2.toString();
        } else if (type == AND) {
            return op3.toString() + " = " + op1.toString() + " & " + op2.toString();
        } else if (type == ASSIGN) {
            return op3.toString() + " = " + op1.toString();
        } else if (type == GRE) {
            return op3.toString() + " = " + op1.toString() + " > " + op2.toString();
        } else if (type == GEQ) {
            return op3.toString() + " = " + op1.toString() + " >= " + op2.toString();
        } else if (type == LSS) {
            return op3.toString() + " = " + op1.toString() + " < " + op2.toString();
        } else if (type == LEQ) {
            return op3.toString() + " = " + op1.toString() + " <= " + op2.toString();
        } else if (type == EQL) {
            return op3.toString() + " = " + op1.toString() + " == " + op2.toString();
        } else if (type == NEQ) {
            return op3.toString() + " = " + op1.toString() + " != " + op2.toString();
        } else if (type == BR) {
            return "BR " + op3.toString();
        } else if (type == BZ) {
            return "BZ " + op3.toString() + " IF " + op1.toString() + " ZERO";
        } else if (type == BNZ) {
            return "BNZ " + op3.toString() + " IF " + op1.toString() + " NOT ZERO";
        } else if (type == SETRET) {
            return "SETRET " + op3.toString();
        } else if (type == RET) {
            return "RET";
        } else if (type == EXIT) {
            return "EXIT";
        } else if (type == LOAD) {
            return "LOAD " + op3.toString() + " FROM BASE " + op1.toString() + " OFFSET " + op2.toString();
        } else if (type == STORE) {
            return "STORE " + op3.toString() + " TO BASE " + op1.toString() + " OFFSET " + op2.toString();
        } else if (type == ALLOCA) {
            return "ALLOCA " + op3.toString() + " SIZEOF " + op1.toString() + " BYTES";
        } else if (type == GETINT) {
            return op3.toString() + " = GETINT";
        } else if (type == PRINTS) {
            return "PRINTSTR " + op3;
        } else if (type == PRINTI) {
            return "PRINTINT " + op3;
        } else if (type == LABEL) {
            return op3.toString() + ":";
        } else if (type == CALL) {
            StringBuilder params = new StringBuilder();
            params.append(op3.toString()).append(" = CALL ").append(op1.toString());
            params.append(" PARAMS:");
            for (IRSymbol symbol : list) {
                params.append(" ").append(symbol.toString());
            }
            return params.toString();
        } else if (type == FUNC) {
            StringBuilder params = new StringBuilder();
            params.append("\nFUNC ").append(op3.toString());
            params.append(" PARAMS:");
            for (IRSymbol symbol : list) {
                params.append(" ").append(symbol.toString());
            }
            return params.toString();
        } else {
            return "错啦";
        }
    }
}
