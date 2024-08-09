package IR;

import AST.ASTNode;
import Symbols.SymbolFunc;
import Symbols.SymbolVar;
import Word.StringWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class IRGenerator {
    private ASTNode compUnit;

    //main函数
    private IRSymbol mainFunction;

    //分配标签
    private IRLabelManager irLabelGenerator;

    //常量数组
    private HashMap<SymbolVar,IRSymbol> constArray;

    //全局变量
    private HashMap<SymbolVar,IRSymbol> globalVars;

    //格式字符串
    private HashMap<IRSymbol,String> strs;

    //函数名和函数标签
    private HashMap<String,IRFunc> funcs;

    //四元式指令集
    private ArrayList<IRChar> irchars;

    //首先对const进行化简
    private Calculator calculator;

    public IRGenerator(ASTNode tmp) {
        this.compUnit = tmp;
        this.irLabelGenerator = new IRLabelManager();
        this.constArray = new HashMap<>();
        this.globalVars = new HashMap<>();
        this.funcs = new HashMap<>();
        this.strs = new HashMap<>();
        this.irchars = new ArrayList<>();
        this.calculator = new Calculator(tmp);
    }

    public HashMap<SymbolVar,IRSymbol> getConstArray() {
        return this.constArray;
    }

    public HashMap<SymbolVar,IRSymbol> getGlobalVars() {
        return this.globalVars;
    }

    public HashMap<IRSymbol,String> getStrs() {
        return this.strs;
    }


    public void genCompUnit() {
        ArrayList<ASTNode> sonNodes = this.compUnit.getSonsNodes();
        for (ASTNode astNode : sonNodes) {
            if (astNode.getType() == ASTNode.DECL) {
                genDecl(astNode,true);
            }
            else if (astNode.getType() == ASTNode.FUNCDEF) {
                genFuncDef(astNode);
            }
            else {
                genMainFuncDef(astNode);
                irchars.add(0,new IRChar(IRChar.BR,null,null,mainFunction));
            }
        }
    }

    public void genDecl(ASTNode tmp,boolean isGlobal) {
        ASTNode sonNode = tmp.getSonsNodes().get(0);
        if (tmp.getSonsNodes().size() != 1) {
            System.out.println("Decl有多个子结点");
        }
        if (sonNode.getType() == ASTNode.CONSTDECL) {
            genConstDecl(sonNode);
        }
        else {
            genVarDecl(sonNode,isGlobal);
        }
    }

    public void genConstDecl(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        for (int i = 1;i < sonNodes.size();i++) {
            genConstDef(sonNodes.get(i));
        }
    }

    public void genVarDecl(ASTNode tmp,boolean isGlobal) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        for(int i = 1;i < sonNodes.size();i++) {
            genVarDef(sonNodes.get(i),isGlobal);
        }
    }

    public void genConstDef(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        if (symbolVar.getDim() > 1) {
            IRLabel irLabel = irLabelGenerator.alloc();
            constArray.put(symbolVar,irLabel);
            tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
        }
    }

    public void genVarDef(ASTNode tmp,boolean isGlobal) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        //单变量
        if (symbolVar.getDim() == 1) {
            //有初始化
            if (sonNodes.size() > 1) {
                IRSymbol initVal = genOneInitVal(sonNodes.get(1));
                IRLabel irLabel = irLabelGenerator.alloc();
                tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
                if (isGlobal) {
                    irLabel.setGlobal(true);
                    globalVars.put(symbolVar,irLabel);
                }
                else {
                    irchars.add(new IRChar(IRChar.ASSIGN,initVal,null,irLabel));
                }
            }
            //无初始化，但是global
            else if(isGlobal) {
                IRLabel irLabel = irLabelGenerator.alloc();
                irLabel.setGlobal(true);
                tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
                globalVars.put(symbolVar,irLabel);
            }
            //局部变量，无初始化
            else {
                IRLabel irLabel = irLabelGenerator.alloc();
                tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
            }
        }
        //以下讨论的是数组的情况
        else {
            //TODO bug
            int memSize = symbolVar.getArraySize() * 4;
            IRLabel irLabel = irLabelGenerator.alloc();
            irLabel.setGlobal(isGlobal);
            tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
            if (isGlobal) {
                globalVars.put(symbolVar,irLabel);
            }
            else {
                irchars.add(new IRChar(IRChar.ALLOCA,new IRImm(memSize),null,irLabel));
            }
            //以下是非全局数组的初始求值
            if (!isGlobal) {

                //一维数组
                if (symbolVar.getDim() == 2 && sonNodes.size() > 2) {
                    if (sonNodes.size() != 3) {
                        System.out.println("varDef中，一维数组的孩子有问题");
                    }
                    //TODO initVal，已经解决
                    ArrayList<ASTNode> initVals = sonNodes.get(2).getSonsNodes();
                    for (int i = 0;i < symbolVar.getDimLen()[0];i++) {
                        IRSymbol initVal = genOneInitVal(initVals.get(i));
                        //TODO 这里的initval.get(i)一定是exp
                        IRChar irChar = new IRChar(IRChar.STORE,irLabel,new IRImm(i * 4),initVal);
                        irchars.add(irChar);
                    }
                }
                //二维数组
                else if (symbolVar.getDim() == 3 && sonNodes.size() > 3) {
                    if (sonNodes.size() != 4) {
                        System.out.println("varDef中，二维数组的孩子有问题");
                    }
                    ArrayList<ASTNode> initVals  = sonNodes.get(3).getSonsNodes();
                    for (int i = 0;i < symbolVar.getDimLen()[1];i++) {
                        ArrayList<ASTNode> initVals2 = initVals.get(i).getSonsNodes();
                        for (int j = 0;j < symbolVar.getDimLen()[0];j++) {
                            IRSymbol init = genOneInitVal(initVals2.get(j));
                            IRChar irChar = new IRChar(IRChar.STORE,irLabel,
                                    new IRImm((i * symbolVar.getDimLen()[0] + j) * 4),init);
                            irchars.add(irChar);
                        }
                    }
                }
            }
        }
    }

    public void genFuncDef(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(1).getIdValue();
        SymbolFunc symbolFunc = tmp.getCurSymbolTable().lookUpFuncGlobal(name);
        //函数引用
        IRLabel irLabel = irLabelGenerator.alloc();
        tmp.getCurSymbolTable().setSymbolFuncRef(symbolFunc,irLabel);
        //函数返回
        IRLabel retLabel = irLabelGenerator.alloc();
        symbolFunc.setRetAddr(retLabel);
        //函数形参
        ArrayList<IRSymbol> fParams;
        ASTNode block;
        //有形参
        if (sonNodes.size() == 4) {
            fParams = genFParams(sonNodes.get(2));
            block = sonNodes.get(3);
        }
        else {
            fParams = new ArrayList<>();
            block = sonNodes.get(2);
        }
        //把函数放进hashmap
        IRFunc irFunc = new IRFunc(name,retLabel,fParams);
        funcs.put(name,irFunc);
        //函数
        IRChar irChar = new IRChar(IRChar.FUNC,null,null,irFunc);
        irChar.setList(fParams);
        irchars.add(irChar);
        //函数定义
        irchars.add(new IRChar(IRChar.LABEL,null,null,irLabel));
        genBlock(block,false);
        //BLOCK
        //函数return
        irchars.add(new IRChar(IRChar.LABEL,null,null,retLabel));
        //void
        irchars.add(new IRChar(IRChar.RET,null,null,null));
    }

    public void genMainFuncDef(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = "main";
        SymbolFunc symbolFunc = tmp.getCurSymbolTable().lookUpFuncGlobal(name);
        //函数label
        IRLabel irLabel = irLabelGenerator.alloc();
        this.mainFunction = irLabel;
        //保存引用信息
        tmp.getCurSymbolTable().setSymbolFuncRef(symbolFunc,irLabel);
        //funcFPrams
        ArrayList<IRSymbol> fParams = new ArrayList<>();
        //函数标签
        IRFunc irFunc = new IRFunc(name,irLabel,fParams);
        funcs.put("main",irFunc);
        //增加irchars
        IRChar irChar = new IRChar(IRChar.FUNC,null,null,irFunc);
        irChar.setList(fParams);
        irchars.add(irChar);
        //增加函数定义标签
        irchars.add(new IRChar(IRChar.LABEL,null,null,irLabel));
        //申请函数出口标签
        IRLabel funcRet = irLabelGenerator.alloc();
        symbolFunc.setRetAddr(funcRet);
        //block
        genBlock(sonNodes.get(0),false);
        //returnLabel
        irchars.add(new IRChar(IRChar.LABEL,null,null,funcRet));
        //mainReturn
        irchars.add(new IRChar(IRChar.EXIT,null,null,null));
    }

    public void genBlock(ASTNode tmp,boolean bzd) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        for (int i = 0;i < sonNodes.size();i++) {
            ASTNode blockItemItem = sonNodes.get(i).getSonsNodes().get(0);
            if (blockItemItem.getType() == ASTNode.DECL) {
                genDecl(blockItemItem,false);
            }
            else {
                genStmt(blockItemItem,bzd);
            }
        }
    }

    public ArrayList<IRSymbol> genFParams(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ArrayList<IRSymbol> res = new ArrayList<>();
        for (int i = 0;i < sonNodes.size();i++) {
            res.add(genFParam(sonNodes.get(i)));
        }
        return res;
    }

    public IRSymbol genFParam(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        //0是bType
        String name = sonNodes.get(1).getIdValue();
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        IRLabel irLabel = irLabelGenerator.alloc();
        tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
        return irLabel;
    }

    public void genStmt(ASTNode tmp,boolean bzd) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 0) {
            //对应只有大括号的情况
            return;
        }
        ASTNode first = sonNodes.get(0);
        if (first.getType() == ASTNode.LVAL) {
            IRSymbol lvalSymbol = genLval(first);
            ASTNode lvalAssign = sonNodes.get(1);
            //LVAL = EXP
            if (lvalAssign.getType() == ASTNode.EXP) {
                IRSymbol expSymbol = genExp(lvalAssign);
                //LVAL单变量
                //TODO bzd是什么
                if (lvalSymbol instanceof IRLabel) {
                    if (bzd || ((IRLabel) lvalSymbol).getGlobal()) {
                        irchars.add(new IRChar(IRChar.ASSIGN,expSymbol,null,lvalSymbol));
                    }
                    else {
                        IRLabel irLabel = irLabelGenerator.alloc();
                        String name = first.getSonsNodes().get(0).getIdValue();
                        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
                        tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
                        irchars.add(new IRChar(IRChar.ASSIGN,expSymbol,null,irLabel));
                    }
                }
                //LVAL是数组
                else {
                    IRSymbol base = ((IRArray) lvalSymbol).getBaseAddr();
                    IRSymbol off = ((IRArray) lvalSymbol).getOffSet();
                    irchars.add(new IRChar(IRChar.STORE,base,off,expSymbol));
                }
            }
            //LVAL = getint
            else {
                if (lvalSymbol instanceof IRLabel) {
                    if (bzd) {
                        irchars.add(new IRChar(IRChar.GETINT,null,null,lvalSymbol));
                    }
                    else {
                        IRLabel irLabel = irLabelGenerator.alloc();
                        String name = first.getSonsNodes().get(0).getIdValue();
                        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
                        tmp.getCurSymbolTable().setSymbolVarRef(symbolVar,irLabel);
                        irchars.add(new IRChar(IRChar.GETINT,null,null,irLabel));
                    }
                }
                else {
                    IRSymbol base = ((IRArray) lvalSymbol).getBaseAddr();
                    IRSymbol off = ((IRArray) lvalSymbol).getOffSet();
                    IRLabel getint = irLabelGenerator.alloc();
                    irchars.add(new IRChar(IRChar.GETINT,null,null,getint));
                    irchars.add(new IRChar(IRChar.STORE,base,off,getint));
                }
            }
        }
        else if (first.getType() == ASTNode.BLOCK) {
            genBlock(first,true);
        }
        else if (first.getType() == ASTNode.EXP) {
            genExp(first);
        }
        else {
            if (first.getType() == ASTNode.IF) {
                IRSymbol cond = genCond(sonNodes.get(1));
                IRSymbol elseCond = irLabelGenerator.alloc();
                irchars.add(new IRChar(IRChar.BZ,cond,null,elseCond));
                ASTNode ifStmt = sonNodes.get(2);
                genStmt(ifStmt,true);
                //if cond stmt else stmt
                //首先是有else的情况
                if (sonNodes.size() > 3) {
                    IRLabel endIf = irLabelGenerator.alloc();
                    irchars.add(new IRChar(IRChar.BR,null,null, endIf));
                    irchars.add(new IRChar(IRChar.LABEL,null,null,elseCond));
                    ASTNode elseStmt = sonNodes.get(3);
                    genStmt(elseStmt,true);
                    irchars.add(new IRChar(IRChar.LABEL,null,null,endIf));
                }
                else {
                    irchars.add(new IRChar(IRChar.LABEL,null,null,elseCond));
                }
            }
            else if (first.getType() == ASTNode.BREAK) {
                IRSymbol endCycle = tmp.getCurSymbolTable().findCycleEnd();
                irchars.add(new IRChar(IRChar.BR,null,null,endCycle));
            }
            else if (first.getType() == ASTNode.CONTINUE) {
                IRSymbol startCycle = tmp.getCurSymbolTable().findCycleStart();
                ASTNode forStmt2 = ((IRLabel) startCycle).getForStmt2();
                if (forStmt2 != null) {
                    genForStmt(forStmt2,true);
                }
                irchars.add(new IRChar(IRChar.BR,null,null,startCycle));
            }
            else if (first.getType() == ASTNode.FOR) {
                IRLabel startFor = irLabelGenerator.alloc();
                IRSymbol startWith = irLabelGenerator.alloc();
                IRSymbol endFor = irLabelGenerator.alloc();

                //这里涉及多处省略
                //forStmt cond forStmt Stmt
                int count = 1;
                ASTNode forStmt1 = null,cond = null,forStmt2 = null,stmt = null;
                if (sonNodes.get(count).getType() == ASTNode.FORSTMT) {
                    forStmt1 = sonNodes.get(count);
                    count++;
                }
                if (sonNodes.get(count).getType() == ASTNode.COND) {
                    cond = sonNodes.get(count);
                    count++;
                }
                if (sonNodes.get(count).getType() == ASTNode.FORSTMT) {
                    forStmt2 = sonNodes.get(count);
                    count++;
                }
                //这里不可能再省略了
                stmt = sonNodes.get(count);
                stmt.getCurSymbolTable().setCycle(startFor,endFor);

                //forStmt1检验
                if (forStmt1 != null) {
                    genForStmt(forStmt1,bzd);
                }
                irchars.add(new IRChar(IRChar.LABEL,null,null,startFor));

                //cond检验
                if (cond != null) {
                    irchars.add(new IRChar(IRChar.BZ,genCond(cond),null,endFor));
                }
                irchars.add(new IRChar(IRChar.LABEL,null,null,startWith));
                startFor.setForStmt2(forStmt2);

                //Stmt
                genStmt(stmt,true);

                //forStmt2检验
                if (forStmt2 != null) {
                    genForStmt(forStmt2,true);
                }

                //重复cond
                if (cond != null) {
                    irchars.add(new IRChar(IRChar.BNZ, genCond(cond), null, startWith));
                }
                else {
                    irchars.add(new IRChar(IRChar.BR,null,null,startWith));
                }

                irchars.add(new IRChar(IRChar.LABEL,null,null,endFor));

            }
            else if (first.getType() == ASTNode.RETURN) {
                SymbolFunc symbolFunc = tmp.getCurSymbolTable().getNowFunction();
                IRSymbol ret = symbolFunc.getRetAddr();
                //return Exp
                if (sonNodes.size() == 2) {
                    ASTNode returnExp = sonNodes.get(1);
                    IRSymbol expIR = genExp(returnExp);
                    irchars.add(new IRChar(IRChar.SETRET,null,null,expIR));
                }
                irchars.add(new IRChar(IRChar.BR,null,null,ret));
            }
            //printf("%d",exp);
            else if (first.getType() == ASTNode.PRINTF) {
                StringWord stringWord = sonNodes.get(1).getFormatString();
                ArrayList<IRSymbol> params = new ArrayList<>();
                for (int i = 2;i < sonNodes.size();i++) {
                    params.add(genExp(sonNodes.get(i)));
                }
                int i;
                for (i = 0;i < stringWord.getCount();i++) {
                    IRLabel strLabel = irLabelGenerator.alloc();
                    strs.put(strLabel,stringWord.getList().get(i));
                    irchars.add(new IRChar(IRChar.PRINTS,null,null,strLabel));
                    irchars.add(new IRChar(IRChar.PRINTI,null,null,params.get(i)));
                }
                IRLabel strLabel = irLabelGenerator.alloc();
                strs.put(strLabel, stringWord.getList().get(i));
                irchars.add(new IRChar(IRChar.PRINTS,null,null,strLabel));
            }
        }
    }

    public void genForStmt(ASTNode tmp,boolean bzd) {
        IRSymbol lval = genLval(tmp.getSonsNodes().get(0));
        IRSymbol exp = genExp(tmp.getSonsNodes().get(1));
        //这真是个大bug
        String name = tmp.getSonsNodes().get(0).getSonsNodes().get(0).getIdValue();
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        if (lval instanceof IRLabel) {
            irchars.add(new IRChar(IRChar.ASSIGN,exp,null,lval));
        }
        else {
            IRSymbol base = ((IRArray) lval).getBaseAddr();
            IRSymbol off = ((IRArray) lval).getOffSet();
            irchars.add(new IRChar(IRChar.STORE,base,off,exp));
        }
    }


    //以下是生成各种IRSymbol
    public IRSymbol genCond(ASTNode tmp) {
        return genLOrExp(tmp.getSonsNodes().get(0));
    }

    public IRSymbol genLOrExp(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            return genLAndExp(sonNodes.get(0));
        }
        IRSymbol or = genLOrExp(sonNodes.get(0));
        //如果or存的值是常数
        if (or instanceof IRImm) {
            if (((IRImm) or).getValue() == 0) {
                return genLAndExp(sonNodes.get(1));
            }
            //TODO 1的话可以直接返回
            else {
                return or;
            }
        }
        IRSymbol end = irLabelGenerator.alloc();
        IRSymbol endAll = irLabelGenerator.alloc();

        //BNZ op3 op1 : 若op1不为0则跳转至op3
        //BZ op3 op1 : 若op1为0则跳转到op3

        // BNZ end1 lOrExp
        // BNZ end1 lAndExp
        // ans = 0
        // BR end
        // end1:
        // ans = 1
        // end:

        //否则执行下面的
        irchars.add(new IRChar(IRChar.BNZ,or,null,end));
        irchars.add(new IRChar(IRChar.BNZ,genLAndExp(sonNodes.get(1)),null,end));

        IRSymbol res = irLabelGenerator.alloc();
        irchars.add(new IRChar(IRChar.ASSIGN,new IRImm(0),null,res));
        irchars.add(new IRChar(IRChar.BR,null,null,endAll));
        irchars.add(new IRChar(IRChar.LABEL,null,null,end));
        irchars.add(new IRChar(IRChar.ASSIGN,new IRImm(1),null,res));
        irchars.add(new IRChar(IRChar.LABEL,null,null,endAll));

        return res;
    }

    public IRSymbol genLAndExp(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            return genEqExp(sonNodes.get(0));
        }
        IRSymbol and = genLAndExp(sonNodes.get(0));
        if (and instanceof IRImm) {
            if (((IRImm) and).getValue() != 0) {
                return genEqExp(sonNodes.get(1));
            }
            //如果是0可以直接return
            else {
                return and;
            }
        }
        IRSymbol end = irLabelGenerator.alloc();
        //IRSymbol debug = irLabelGenerator.alloc();
        IRSymbol endAll = irLabelGenerator.alloc();

        //BZ end0 lAndExp
        //BZ end0 eqExp
        //assignSymbol=1
        //BR end
        //end0:
        //assignSymbol=0
        //end:

        irchars.add(new IRChar(IRChar.BZ,and,null,end));
        irchars.add(new IRChar(IRChar.BZ,genEqExp(sonNodes.get(1)),null,end));

        IRSymbol res = irLabelGenerator.alloc();
        irchars.add(new IRChar(IRChar.ASSIGN,new IRImm(1),null,res));
        irchars.add(new IRChar(IRChar.BR,null,null,endAll));
        irchars.add(new IRChar(IRChar.LABEL,null,null,end));
        irchars.add(new IRChar(IRChar.ASSIGN,new IRImm(0),null,res));
        irchars.add(new IRChar(IRChar.LABEL,null,null,endAll));
        return res;
    }

    public IRSymbol genEqExp(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            return genRelExp(sonNodes.get(0));
        }
        IRSymbol eq = genEqExp(sonNodes.get(0));
        //注意这里有符号
        IRSymbol rel = genRelExp(sonNodes.get(2));
        IRSymbol res = irLabelGenerator.alloc();
        if (sonNodes.get(1).getOp().equals("==")) {
            irchars.add(new IRChar(IRChar.EQL,eq,rel,res));
        }
        else {
            irchars.add(new IRChar(IRChar.NEQ,eq,rel,res));
        }
        return res;
    }

    public IRSymbol genRelExp(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            return genAddExp(sonNodes.get(0));
        }
        IRSymbol rel = genRelExp(sonNodes.get(0));
        IRSymbol add = genAddExp(sonNodes.get(2));
        IRSymbol res = irLabelGenerator.alloc();
        if (sonNodes.get(1).getOp().equals("<")) {
            irchars.add(new IRChar(IRChar.LSS,rel,add,res));
        }
        else if (sonNodes.get(1).getOp().equals(">")) {
            irchars.add(new IRChar(IRChar.GRE,rel,add,res));
        }
        else if (sonNodes.get(1).getOp().equals("<=")) {
            irchars.add(new IRChar(IRChar.LEQ,rel,add,res));
        }
        else {
            irchars.add(new IRChar(IRChar.GEQ,rel,add,res));
        }
        return res;
    }


    public IRSymbol genOneInitVal(ASTNode tmp) {
        ASTNode expNode = tmp.getSonsNodes().get(0);
        if (expNode.getType() == ASTNode.CONSTEXP) {
            return genConstExp(tmp);
        }
        else {
            return genExp(tmp);
        }
    }
    public IRSymbol genConstExp(ASTNode tmp) {
        if (!tmp.getIsCalculated()) {
            this.calculator.constExpCalculate(tmp);
        }
        return new IRImm(tmp.getConstValue());
    }

    public IRSymbol genExp(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        else {
            return genAddExp(tmp.getSonsNodes().get(0));
        }
    }

    public IRSymbol genAddExp(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        IRSymbol irSymbol;
        ASTNode expNode = tmp.getSonsNodes().get(0);
        if (expNode.getType() == ASTNode.MULEXP) {
            irSymbol = genMulExp(expNode);
        }
        else {
            irSymbol = genAddExp(expNode);
        }
        if (tmp.getSonsNodes().size() > 1) {
            ASTNode expNode1 = tmp.getSonsNodes().get(2);
            IRSymbol irSymbol1;
            if (expNode1.getType() == ASTNode.MULEXP) {
                irSymbol1 = genMulExp(expNode1);
            }
            else {
                irSymbol1 = genAddExp(expNode1);
            }
            String op = tmp.getSonsNodes().get(1).getOp();
            IRLabel irLabel = irLabelGenerator.alloc();
            if (op.equals("+")) {
                irchars.add(new IRChar(IRChar.ADD, irSymbol, irSymbol1, irLabel));
            }
            else if (op.equals("-")) {
                irchars.add(new IRChar(IRChar.MINU, irSymbol, irSymbol1, irLabel));
            }
            return irLabel;
        }
        else {
            return irSymbol;
        }
    }

    public IRSymbol genMulExp(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        IRSymbol irSymbol;
        ASTNode expNode = tmp.getSonsNodes().get(0);
        if (expNode.getType() == ASTNode.UNARYEXP) {
            irSymbol = genUnaryExp(expNode);
        }
        else {
            irSymbol = genMulExp(expNode);
        }
        if (tmp.getSonsNodes().size() > 1) {
            ASTNode expNode1 = tmp.getSonsNodes().get(2);
            IRSymbol irSymbol1;
            if (expNode1.getType() == ASTNode.UNARYEXP) {
                irSymbol1 = genUnaryExp(expNode1);
            }
            else {
                irSymbol1 = genMulExp(expNode1);
            }
            String op = tmp.getSonsNodes().get(1).getOp();
            IRLabel irLabel = irLabelGenerator.alloc();
            if (op.equals("*")) {
                irchars.add(new IRChar(IRChar.MULT,irSymbol,irSymbol1,irLabel));
            }
            else if (op.equals("/")) {
                irchars.add(new IRChar(IRChar.DIV,irSymbol,irSymbol1,irLabel));
            }
            else if (op.equals("%")) {
                irchars.add(new IRChar(IRChar.MOD,irSymbol,irSymbol1,irLabel));
            }
            return irLabel;
        }
        else {
            return irSymbol;
        }
    }

    public IRSymbol genUnaryExp(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        //primaryExp
        if (sonNodes.size() == 1 && sonNodes.get(0).getType() == ASTNode.PRIMARYEXP) {
            return genPrimaryExp(sonNodes.get(0));
        }
        //UnaryOp UnaryExp
        else if (sonNodes.size() == 2 && sonNodes.get(0).getType() == ASTNode.UNARYOP) {
            String op = sonNodes.get(0).getOp();
            IRSymbol irSymbol = genUnaryExp(sonNodes.get(1));
            if (op.equals("-")) {
                IRLabel irLabel = irLabelGenerator.alloc();
                irchars.add(new IRChar(IRChar.MINU,new IRImm(0),irSymbol,irLabel));
                return irLabel;
            }
            else if (op.equals("+")) {
                return irSymbol;
            }
            //一定为"!"
            else {
                IRLabel irLabel = irLabelGenerator.alloc();
                irchars.add(new IRChar(IRChar.EQL,new IRImm(0),irSymbol,irLabel));
                return irLabel;
            }
        }
        //function
        else {
            return genFuncCall(tmp);
        }
    }

    public IRSymbol genPrimaryExp(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        ASTNode expNode = tmp.getSonsNodes().get(0);
        //number
        if (expNode.getType() == ASTNode.NUMBER) {
            return genNumber(expNode);
        }
        //LVAL
        else if (expNode.getType() == ASTNode.LVAL) {
            //System.out.println(expNode.getSonsNodes().get(0).getIdValue());
            IRSymbol irSymbol = genLval(expNode);
            if (irSymbol instanceof IRArray) {
                IRArray irArray = (IRArray) irSymbol;
                IRLabel irLabel = irLabelGenerator.alloc();
                irchars.add(new IRChar(IRChar.LOAD,irArray.getBaseAddr(),irArray.getOffSet(),irLabel));
                return irLabel;
            }
            else {
                return irSymbol;
            }
        }
        //primaryExp,我的没有括号，直接从第一个开始
        else {
            return genExp(expNode);
        }
    }

    public IRSymbol genFuncCall(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ASTNode funcRParams = null;
        if (sonNodes.size() != 1) {
            funcRParams = sonNodes.get(1);
        }
        ArrayList<IRSymbol> params = new ArrayList<>();
        //我的parser格式和那个不一样
        //就导致这里很容易出现这种问题
        if (funcRParams != null) {
            //实参
            ArrayList<ASTNode> real = funcRParams.getSonsNodes();
            for (int i = 0;i < real.size();i++) {
                params.add(genExp(real.get(i)));
            }
        }
        IRSymbol funcIR = funcs.get(sonNodes.get(0).getIdValue());
        IRSymbol ret = irLabelGenerator.alloc();
        IRChar irChar = new IRChar(IRChar.CALL,funcIR,null,ret);
        irChar.setList(params);
        irchars.add(irChar);
        return ret;
    }

    public IRSymbol genNumber(ASTNode tmp) {
        tmp.setIsCalculated(true);
        tmp.setConstValue(tmp.getNumber());
        return new IRImm(tmp.getNumber());
    }


    public IRSymbol genLval(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        SymbolVar symbolVar = tmp.getCurSymbolTable().lvalGlobal(name,tmp.getSymbolPos());
        if (symbolVar.getDim() == 1) {
            return genLval1(tmp);
        }
        else {
            return genLval2(tmp);
        }
    }

    public IRSymbol genLval1(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        SymbolVar symbolvar = tmp.getCurSymbolTable().lvalGlobal(name,tmp.getSymbolPos());
        if (!symbolvar.getIsConst()) {
            //TODO 写的是对的就是不太理解
            IRLabel irLabel = (IRLabel) tmp.getCurSymbolTable().getGlobalVarRef(symbolvar);
            if (irLabel == null) {
                irLabel = irLabelGenerator.alloc();
                tmp.getCurSymbolTable().setSymbolVarRef(symbolvar,irLabel);
                return irLabel;
            }
            else if (irLabel.getGlobal()) {
                return new IRArray(irLabel,new IRImm(0));
            }
            else {
                return irLabel;
            }
        }
        else {
            return new IRImm(symbolvar.getConstValue());
        }
    }


    public IRSymbol genLval2(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return new IRImm(tmp.getConstValue());
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        SymbolVar symbolVar = tmp.getCurSymbolTable().lvalGlobal(name,tmp.getSymbolPos());
        IRSymbol base = tmp.getCurSymbolTable().getGlobalVarRef(symbolVar);
        if (sonNodes.size() == 1) {
            return base;
        }
        ASTNode expNode1 = sonNodes.get(1);
        //irSymbol1
        IRSymbol irSymbol1 = genExp(expNode1);
        //一维数组
        if (symbolVar.getDim() == 2) {
            IRLabel irLabel = irLabelGenerator.alloc();
            IRChar irChar = new IRChar(IRChar.MULT,irSymbol1,new IRImm(4),irLabel);
            irchars.add(irChar);
            return new IRArray(base,irLabel);
        }
        //二维数组
        else {
            IRSymbol dim1;
            if (irSymbol1 instanceof IRImm) {
                int mem1 = ((IRImm) irSymbol1).getValue() * symbolVar.getDimLen()[0];
                dim1 = new IRImm(mem1);
            }
            else {
                dim1 = irLabelGenerator.alloc();
                irchars.add(new IRChar(IRChar.MULT,irSymbol1,new IRImm(symbolVar.getDimLen()[0]),dim1));
            }
            //二维数组的一维形式
            if (sonNodes.size() == 2) {
                IRSymbol off;
                if (dim1 instanceof IRImm) {
                    off = new IRImm((((IRImm) dim1).getValue()) * 4);
                }
                else {
                    off = irLabelGenerator.alloc();
                    irchars.add(new IRChar(IRChar.MULT,dim1,new IRImm(4),off));
                }
                IRSymbol retAddr = irLabelGenerator.alloc();
                irchars.add(new IRChar(IRChar.ADD,base,off,retAddr));
                return retAddr;
            }
            //二维数组的二维形式
            else {
                ASTNode expNode0 = sonNodes.get(2);
                IRSymbol dim0 = genExp(expNode0);
                IRSymbol off;
                if (dim1 instanceof IRImm && dim0 instanceof IRImm) {
                    off = new IRImm((((IRImm) dim1).getValue() + ((IRImm) dim0).getValue()) * 4);
                }
                else {
                    IRLabel irLabel = irLabelGenerator.alloc();
                    irchars.add(new IRChar(IRChar.ADD,dim1,dim0,irLabel));
                    off = irLabelGenerator.alloc();
                    //TODO
                    irchars.add(new IRChar(IRChar.MULT,irLabel,new IRImm(4),off));
                }
                return new IRArray(base,off);
            }
        }
    }

    public ArrayList<IRChar> getIrchars() {
        return this.irchars;
    }

    // TODO debug
    public void outputIR() {
        ArrayList<IRChar> iRList = this.irchars;
        StringBuilder outStr = new StringBuilder(".data\n");
        for (SymbolVar constVarSymbol : constArray.keySet()) {
            IRSymbol constSymbol = constArray.get(constVarSymbol);
            outStr.append(".align 2\n");
            outStr.append(constSymbol.toString()).append(":\n.word ");
            ArrayList<Integer> constArr = constVarSymbol.getValue();
            int i;
            for (i = 0; i < constArr.size() - 1; ++i) {
                outStr.append(constArr.get(i)).append(", ");
            }
            outStr.append(constArr.get(i)).append("\n");
        }
        for (SymbolVar globalVarSymbol : globalVars.keySet()) {
            IRSymbol constSymbol = globalVars.get(globalVarSymbol);
            outStr.append(".align 2\n");
            outStr.append(constSymbol.toString()).append(":\n.word ");
            ArrayList<Integer> constArr = globalVarSymbol.getValue();
            int i;
            for (i = 0; i < constArr.size() - 1; ++i) {
                outStr.append(constArr.get(i)).append(", ");
            }
            outStr.append(constArr.get(i)).append("\n");
        }
        for (IRSymbol strSymbol : strs.keySet()) {
            outStr.append(".align 2\n");
            outStr.append(strSymbol.toString()).append(":\n.asciiz ");
            String rawStr = strs.get(strSymbol);
            outStr.append("\"").append(rawStr).append("\"\n");
        }

        outStr.append("\n.text\n");
        for (IRChar irElem : iRList) {
            outStr.append(irElem.toString()).append("\n");
        }
        System.out.println(outStr);
    }
}
