package AST;

import Symbols.SymbolTable;
import Word.StringWord;

import java.util.ArrayList;

public class ASTNode {
    public static final int COMPUNIT = 0, DECL = 1, CONSTDECL = 2, BTYPE = 3, CONSTDEF = 4,
            CONSTINITVAL = 5, VARDECL = 6, VARDEF = 7, INITVAL = 8, FUNCDEF = 9, FUNCTYPE = 10,
            FUNCFPARAMS = 11, FUNCFPARAM = 12, BLOCK = 13, BLOCKITEM = 14, STMT = 15,
            EXP = 16, COND = 17, LVAL = 18, PRIMARYEXP = 19, NUMBER = 20, UNARYEXP = 21,
            UNARYOP = 22, FUNCRPARAMS = 23, MULEXP = 24, ADDEXP = 25, RELEXP = 26, EQEXP = 27,
            LANDEXP = 28, LOREXP = 29, CONSTEXP = 30, MAINFUNCDEF = 31,IDENT = 32,
            TOKEN = 32,BREAK = 33,CONTINUE = 34,RETURN = 35,PRINTF = 36,
            FORSTMT = 37,IF=38,FOR=39,GETINT=40,FORMATSTRING = 41;

    public static final String[] syntaxTypes = new String[]{"CompUnit", "Decl", "ConstDecl", "BType", "ConstDef",
            "ConstInitVal", "VarDecl", "VarDef", "InitVal", "FuncDef", "FuncType",
            "FuncFParams", "FuncFParam", "Block", "BlockItem", "Stmt",
            "Exp", "Cond", "LVal", "PrimaryExp", "Number", "UnaryExp",
            "UnaryOp", "FuncRParams", "MulExp", "AddExp", "RelExp", "EqExp",
            "LAndExp", "LOrExp", "ConstExp", "MainFuncDef","Ident","Break","Continue","Return",
            "Forstmt", "Printf","If"};

    //标记树结点的类型
    private final int type;

    //它的sons
    private ArrayList<ASTNode> sonNodes;

    //它对应的symboltable
    private SymbolTable curSymbolTable;

    //如果是IDENFR,那么它的value:
    private String idValue;

    //如果是funcType，那么它的type
    private String funcType;

    //如果是number，那么它的value
    private String number;

    //如果是unaryOp，那么它的value
    private String op;

    //如果是funcFParam,那么它的dim
    private int dim;

    //如果是exp，那么它的value以及是否被计算过了
    private int constValue;
    private boolean isCalculated;

    //如果是formatSting
    private StringWord stringWord;

    //TODO 返校以后新修改的
    private int pos;


    public ASTNode(int tmp) {
        this.type = tmp;
        this.sonNodes = new ArrayList<>();
        this.isCalculated = false;
        this.pos = 0;
    }


    public void setCurSymbolTable(SymbolTable tmp) {
        this.curSymbolTable = tmp;
        this.pos = tmp.getListPos();
    }

    public void addSonNode(ASTNode tmp) {
        this.sonNodes.add(tmp);
    }

    public void setIdValue(String tmp) {
        this.idValue = tmp;
    }

    public String getIdValue() {
        return this.idValue;
    }

    public void setFuncType(String tmp) {
        this.funcType = tmp;
    }

    public String getFuncType() {
        return this.funcType;
    }

    public void setNumber(String tmp) {
        this.number = tmp;
    }

    public int getNumber() {
        return Integer.parseInt(this.number);
    }

    public void setOp(String tmp) {
        this.op = tmp;
    }

    public String getOp() {
        return this.op;
    }

    public ArrayList<ASTNode> getSonsNodes() {
        return this.sonNodes;
    }

    public void setDim(int tmp) {
        this.dim = tmp;
    }

    public int getDim() {
        return this.dim;
    }

    public int getType() {
        return this.type;
    }

    public SymbolTable getCurSymbolTable() {
        return this.curSymbolTable;
    }


    //用于计算中间值的时候
    public void setIsCalculated(boolean tmp) {
        this.isCalculated = tmp;
    }

    public boolean getIsCalculated() {
        return this.isCalculated;
    }

    public void setConstValue(int tmp) {
        this.constValue = tmp;
    }

    public int getConstValue() {
        return this.constValue;
    }

    public void setFormatstring(StringWord tmp) {
        this.stringWord = tmp;
    }

    public StringWord getFormatString() {
        return this.stringWord;
    }

    public int getSymbolPos() {
        return this.pos;
    }
}
