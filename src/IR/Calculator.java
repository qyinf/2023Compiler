package IR;

import AST.ASTNode;
import Symbols.SymbolVar;

import java.util.ArrayList;

public class Calculator {
    private ASTNode compUnit;

    public Calculator(ASTNode tmp) {
        this.compUnit = tmp;
    }

    public void simplify() {
        ArrayList<ASTNode> sonNodes = this.compUnit.getSonsNodes();
        for (ASTNode astNode : sonNodes) {
            if (astNode.getType() == ASTNode.DECL) {
                globalDeclCalculate(astNode);
            }
            else {
                functionCalculate(astNode);
            }
        }
    }

    public void globalDeclCalculate(ASTNode tmp) {
        ASTNode decl = tmp.getSonsNodes().get(0);
        ArrayList<ASTNode> sonNodes = decl.getSonsNodes();
        if (decl.getType() == ASTNode.CONSTDECL) {
            //一堆constDef
            for (int i = 1;i < sonNodes.size();i++) {
                constDefCalculate(sonNodes.get(i));
            }
        }
        else {
            //一堆varDef
            for (int i = 1;i < sonNodes.size();i++) {
                globalVarDefCalculate(sonNodes.get(i));
            }
        }
    }


    //这个不知道是什么
    public void functionCalculate(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        for (ASTNode astNode : sonNodes) {
            if (astNode.getType() == ASTNode.CONSTDEF) {
                constDefCalculate(astNode);
            }
            else if (astNode.getType() == ASTNode.CONSTEXP) {
                constExpCalculate(astNode);
            }
            else if (astNode.getType() == ASTNode.VARDEF) {
                varDefCalculate(astNode);
            }
            else if (astNode.getType() == ASTNode.FUNCFPARAM) {
                fParamCalculate(astNode);
            }
            else if (astNode.getType() == ASTNode.ADDEXP) {
                addCalculate(astNode);
            }
            else if (astNode.getType() == ASTNode.EXP) {
                expCalculate(astNode);
            }
            else {
                functionCalculate(astNode);
            }
        }
    }


    //const的
    public void constDefCalculate(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        //Ident
        String name = sonNodes.get(0).getIdValue();
        //TODO 查找
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        //没有constExp
        if (symbolVar.getDim() == 1) {
            ASTNode constInitVal = sonNodes.get(1);
            ASTNode constExp = constInitVal.getSonsNodes().get(0);
            symbolVar.setConstValue(constExpCalculate(constExp));
        }
        //以下的情况都是数组
        else {
            //有一个or两个constExp
            int dim1 = constExpCalculate(sonNodes.get(1));
            //一维数组
            if (symbolVar.getDim() == 2) {
                symbolVar.setDimLen(0,dim1);
                ASTNode constInitVal = sonNodes.get(2);
                ArrayList<Integer> ans = constInitCalculate(constInitVal);
                symbolVar.setOneDimConstArrayValue(dim1,ans);
            }
            //二维数组
            else {
                int dim0 = constExpCalculate(sonNodes.get(2));
                symbolVar.setDimLen(1,dim1);
                symbolVar.setDimLen(0,dim0);
                //TODO 二维数组的constInitVal
                ArrayList<ASTNode> constInitVals = sonNodes.get(3).getSonsNodes();
                ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
                for (int i = 0;i < constInitVals.size();i++) {
                    ans.add(constInitCalculate(constInitVals.get(i)));
                }
                symbolVar.setTwoDimConstArrayValue(ans,dim1,dim0);
            }
        }
    }

    //全局变量的var的
    //没有initval要赋值为0
    public void globalVarDefCalculate(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        //TODO 查找
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        if (symbolVar.getDim() == 1) {
            //没有初始化的全局变量为0
            if (sonNodes.size() == 1) {
                symbolVar.setVarValue(0);
            }
            else {
                ASTNode constInitVal = sonNodes.get(1);
                ASTNode constExp = constInitVal.getSonsNodes().get(0);
                symbolVar.setVarValue(constExpCalculate(constExp));
            }
        }
        //如果是数组
        else {
            int dim1 = constExpCalculate(sonNodes.get(1));
            //一维数组
            if (symbolVar.getDim() == 2) {
                symbolVar.setDimLen(0,dim1);
                ArrayList<Integer> ans = new ArrayList<>();
                //只有ident,constExp,没有后面的constInitVal
                if (sonNodes.size() == 2) {
                    for (int i = 0;i < dim1;i++) {
                        ans.add(0);
                    }
                }
                else {
                    ASTNode constInitVal = sonNodes.get(2);
                    ans = constInitCalculate(constInitVal);
                }
                symbolVar.setOneVarArrayValue(dim1,ans);
            }
            //二维数组
            //结构是这样的:Ident constExp constExp (initVal)
            else {
                int dim0 = constExpCalculate(sonNodes.get(2));
                symbolVar.setDimLen(0,dim0);
                symbolVar.setDimLen(1,dim1);
                ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
                //没有初始化
                if (sonNodes.size() == 3) {
                    for (int i = 0;i < dim1;i++) {
                        ArrayList<Integer> tmpAns = new ArrayList<>();
                        for (int j = 0;j < dim0;j++) {
                            tmpAns.add(0);
                        }
                        ans.add(tmpAns);
                    }
                }
                else {
                    ArrayList<ASTNode> initVals = sonNodes.get(3).getSonsNodes();
                    for (int i = 0;i < initVals.size();i++) {
                        ans.add(constInitCalculate(initVals.get(i)));
                    }
                }
                symbolVar.setTwoVarArrayValue(ans,dim1,dim0);
            }
        }
    }

    //普通var的
    //Ident constExp
    public void varDefCalculate(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        //TODO 查找
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        if (symbolVar.getDim() > 1) {
            int dim1 = constExpCalculate(sonNodes.get(1));
            //一维数组
            if (symbolVar.getDim() == 2) {
                symbolVar.setDimLen(0,dim1);
            }
            else {
                int dim0 = constExpCalculate(sonNodes.get(2));
                symbolVar.setDimLen(0,dim0);
                symbolVar.setDimLen(1,dim1);
            }
        }
        //TODO 疑问
        for (ASTNode astNode : sonNodes) {
            if (astNode.getType() == ASTNode.INITVAL) {
                functionCalculate(astNode);
            }
        }
    }

    //形参长度设置
    //Btype Ident
    public void fParamCalculate(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(1).getIdValue();
        //TODO 查找
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        //TODO fparam疑问
        //Btype Ident [][Exp]
        if (symbolVar.getDim() == 3) {
            int dim0 = constExpCalculate(sonNodes.get(2));
            symbolVar.setDimLen(0,dim0);
        }
    }



    //以下是具体的计算
    //TODO 不是很懂
    public ArrayList<Integer> constInitCalculate(ASTNode tmp) {
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ArrayList<Integer> ans = new ArrayList<>();
        for (int i = 0;i < sonNodes.size();i++) {
            ASTNode initVal = sonNodes.get(i);
            ASTNode constExp = initVal.getSonsNodes().get(0);
            ans.add(constExpCalculate(constExp));
        }
        return ans;
    }


    //const类型
    public int constExpCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return tmp.getConstValue();
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ASTNode expNode = sonNodes.get(0);
        constAddCalculate(expNode);
        tmp.setConstValue(expNode.getConstValue());
        tmp.setIsCalculated(true);
        return tmp.getConstValue();
    }

    public void constAddCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            ASTNode mulExp = sonNodes.get(0);
            constMulCalculate(mulExp);
            tmp.setConstValue(mulExp.getConstValue());
        }
        else {
            ASTNode addExp = sonNodes.get(0);
            constAddCalculate(addExp);
            ASTNode mulExp = sonNodes.get(2);
            constMulCalculate(mulExp);
            String op = sonNodes.get(1).getOp();
            if (op.equals("+")) {
                tmp.setConstValue(addExp.getConstValue() + mulExp.getConstValue());
            }
            else if (op.equals("-")) {
                tmp.setConstValue(addExp.getConstValue() - mulExp.getConstValue());
            }
        }
        tmp.setIsCalculated(true);
    }

    public void constMulCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            ASTNode unaryExp = sonNodes.get(0);
            constUnaryCalculate(unaryExp);
            tmp.setConstValue(unaryExp.getConstValue());
        }
        else {
            ASTNode mulExp = sonNodes.get(0);
            constMulCalculate(mulExp);
            ASTNode unaryExp = sonNodes.get(2);
            constUnaryCalculate(unaryExp);
            String op = sonNodes.get(1).getOp();
            if (op.equals("*")) {
                tmp.setConstValue(mulExp.getConstValue() * unaryExp.getConstValue());
            }
            else if (op.equals("/")) {
                tmp.setConstValue(mulExp.getConstValue() / unaryExp.getConstValue());
            }
            else if (op.equals("%")) {
                tmp.setConstValue(mulExp.getConstValue() % unaryExp.getConstValue());
            }
        }
        tmp.setIsCalculated(true);
    }

    public void constUnaryCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ASTNode expNode = sonNodes.get(0);
        if (expNode.getType() == ASTNode.PRIMARYEXP) {
            constPrimaryCalculate(expNode);
            tmp.setConstValue(expNode.getConstValue());
        }
        else {
            String op = expNode.getOp();
            expNode = sonNodes.get(1);
            constUnaryCalculate(expNode);
            if (op.equals("-")) {
                tmp.setConstValue(-expNode.getConstValue());
            }
            else if (op.equals("+")) {
                tmp.setConstValue(expNode.getConstValue());
            }
            else {
                tmp.setConstValue((expNode.getConstValue() == 0) ? 1 : 0);
            }
        }
        tmp.setIsCalculated(true);
    }

    public void constPrimaryCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ASTNode expNode = sonNodes.get(0);
        if (expNode.getType() == ASTNode.NUMBER) {
            numberCalculate(expNode);
            tmp.setConstValue(expNode.getConstValue());
        }
        else if (expNode.getType() == ASTNode.LVAL) {
            constLvalCalculate(expNode);
            tmp.setConstValue(expNode.getConstValue());
        }
        else {
            constExpCalculate(expNode);
            tmp.setConstValue(expNode.getConstValue());
        }
        tmp.setIsCalculated(true);
    }

    public void constLvalCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        //TODO 查找
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        //TODO BUG
        if (!symbolVar.hasConstValue()) {
            symbolVar = tmp.getCurSymbolTable().lvalGlobal(name,tmp.getSymbolPos());
        }
        //引用常数
        if (sonNodes.size() == 1) {
            tmp.setConstValue(symbolVar.getConstValue());
        }
        else {
            //引用数组
            ASTNode exp1 = sonNodes.get(1);
            constExpCalculate(exp1);
            int dim1 = exp1.getConstValue();
            //一维数组
            if (sonNodes.size() == 2) {
                if (symbolVar.getDim() != 2) {
                    System.out.println("constLval的一维数组有错");
                }
                tmp.setConstValue(symbolVar.getConstValue(dim1));
            }
            else {
                if (symbolVar.getDim() != 3) {
                    System.out.println("constLval的二维数组有错");
                }
                ASTNode exp0 = sonNodes.get(2);
                constExpCalculate(exp0);
                int dim0 = exp0.getConstValue();
                tmp.setConstValue(symbolVar.getConstValue(dim1,dim0));
            }
        }
        tmp.setIsCalculated(true);
    }


    //非const
    public int addCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return tmp.getConstValue();
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            ASTNode mulExp = sonNodes.get(0);
            if (mulExp.getType() == ASTNode.MULEXP) {
                mulCalculate(mulExp);
            }
            else {
                addCalculate(mulExp);
            }
            if (mulExp.getIsCalculated()) {
                tmp.setIsCalculated(true);
                tmp.setConstValue(mulExp.getConstValue());
            }
        }
        else {
            ASTNode addExp = sonNodes.get(0);
            addCalculate(addExp);
            ASTNode mulExp = sonNodes.get(2);
            if (mulExp.getType() == ASTNode.MULEXP) {
                mulCalculate(mulExp);
            }
            else {
                addCalculate(mulExp);
            }
            if (addExp.getIsCalculated() && mulExp.getIsCalculated()) {
                String op = sonNodes.get(1).getOp();
                if (op.equals("+")) {
                    tmp.setConstValue(addExp.getConstValue() + mulExp.getConstValue());
                }
                else if (op.equals("-")) {
                    tmp.setConstValue(addExp.getConstValue() - mulExp.getConstValue());
                }
                tmp.setIsCalculated(true);
            }
        }
        return tmp.getConstValue();
    }

    public void expCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ASTNode addExp = sonNodes.get(0);
        int ans = addCalculate(addExp);
        if (addExp.getIsCalculated()) {
            tmp.setIsCalculated(true);
            tmp.setConstValue(ans);
        }
    }

    public void mulCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        if (sonNodes.size() == 1) {
            ASTNode unaryExp = sonNodes.get(0);
            unaryCalculate(unaryExp);
            if (unaryExp.getIsCalculated()) {
                tmp.setIsCalculated(true);
                tmp.setConstValue(unaryExp.getConstValue());
            }
        }
        else {
            ASTNode mulExp = sonNodes.get(0);
            mulCalculate(mulExp);
            ASTNode unaryExp = sonNodes.get(2);
            unaryCalculate(unaryExp);
            if (mulExp.getIsCalculated() && unaryExp.getIsCalculated()) {
                String op = sonNodes.get(1).getOp();
                if (op.equals("*")) {
                    tmp.setConstValue(mulExp.getConstValue() * unaryExp.getConstValue());
                }
                else if (op.equals("/")) {
                    tmp.setConstValue(mulExp.getConstValue() / unaryExp.getConstValue());
                }
                else if (op.equals("%")) {
                    tmp.setConstValue(mulExp.getConstValue() % unaryExp.getConstValue());
                }
                tmp.setIsCalculated(true);
            }
        }
    }

    public void unaryCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ASTNode expNode = sonNodes.get(0);
        if (expNode.getType() == ASTNode.PRIMARYEXP) {
            primaryCalculate(expNode);
            if (expNode.getIsCalculated()) {
                tmp.setIsCalculated(true);
                tmp.setConstValue(expNode.getConstValue());
            }
        }
        else if (expNode.getType() == ASTNode.UNARYOP) {
            String op = expNode.getOp();
            ASTNode expNode2 = sonNodes.get(1);
            unaryCalculate(expNode2);

            if (expNode2.getIsCalculated()) {
                if (op.equals("-")) {
                    tmp.setConstValue(-expNode2.getConstValue());
                }
                else if (op.equals("+")) {
                    tmp.setConstValue(expNode2.getConstValue());
                }
                else if (op.equals("!")) {
                    tmp.setConstValue((expNode2.getConstValue()) == 0 ? 1 : 0);
                }
                tmp.setIsCalculated(true);
            }
        }
    }

    public void primaryCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        ASTNode expNode = sonNodes.get(0);
        if (expNode.getType() == ASTNode.EXP) {
            expCalculate(expNode);
            if (expNode.getIsCalculated()) {
                tmp.setConstValue(expNode.getConstValue());
                tmp.setIsCalculated(true);
            }
        }
        else {
            if (expNode.getType() == ASTNode.NUMBER) {
                numberCalculate(expNode);
                tmp.setConstValue(expNode.getConstValue());
                tmp.setIsCalculated(true);
            } else if (expNode.getType() == ASTNode.LVAL) {
                lvalCalculate(expNode);
                if (expNode.getIsCalculated()) {
                    tmp.setConstValue(expNode.getConstValue());
                    tmp.setIsCalculated(true);
                }
            }
            tmp.setConstValue(expNode.getConstValue());
        }
    }

    public void numberCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        tmp.setConstValue(tmp.getNumber());
        tmp.setIsCalculated(true);
    }


    //Ident exp
    public void lvalCalculate(ASTNode tmp) {
        if (tmp.getIsCalculated()) {
            return;
        }
        ArrayList<ASTNode> sonNodes = tmp.getSonsNodes();
        String name = sonNodes.get(0).getIdValue();
        //TODO 查找
        SymbolVar symbolVar = tmp.getCurSymbolTable().lookUpVarGlobal(name);
        if (!symbolVar.hasConstValue()) {
            //TODO ERROR
            symbolVar = tmp.getCurSymbolTable().lvalGlobal(name,tmp.getSymbolPos());
        }
        if (symbolVar == null || !symbolVar.hasConstValue()) {
            return;
        }
        //单个常数
        if (sonNodes.size() == 1) {
            tmp.setConstValue(symbolVar.getConstValue());
        }
        //数组
        else {
            ASTNode exp1 = sonNodes.get(1);
            expCalculate(exp1);
            if (!exp1.getIsCalculated()) {
                return;
            }
            int dim1 = exp1.getConstValue();
            if (sonNodes.size() == 2) {
                tmp.setConstValue(symbolVar.getConstValue(dim1));
            }
            else {
                ASTNode exp2 = sonNodes.get(2);
                expCalculate(exp2);
                if (!exp2.getIsCalculated()) {
                    return;
                }
                int dim0 = exp2.getConstValue();
                tmp.setConstValue(symbolVar.getConstValue(dim1,dim0));
            }
        }
        tmp.setIsCalculated(true);
    }

    public static StringBuilder printUnit(ASTNode curUnit) {
        if (curUnit.getIsCalculated()) {
            System.out.println("type=" + curUnit.getType());
            return new StringBuilder(Integer.toString(curUnit.getConstValue()));
        }
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<ASTNode> sonList = curUnit.getSonsNodes();
        for (ASTNode unit : sonList) {
            stringBuilder.append(printUnit(unit));
//            if (unit.getType() == ASTNode.BLOCK) {
//                stringBuilder.append(" ");
//            }
//            stringBuilder.append(" ");
        }
        System.out.println(stringBuilder);
        return stringBuilder;
    }
}
