package Symbols;

import AST.ASTNode;
import IR.IRSymbol;

import java.util.ArrayList;

public class SymbolFunc extends Symbol{
    //false:void true:int
    private boolean hasReturnValue;
    //记录函数各个参数的维度
    //其size代表参数数量
    //除了id，包含了funcFPrams的所有信息
    private ArrayList<Integer> dims;

    //return后面的值
    public ASTNode returnValue;

    //函数返回的标签
    private IRSymbol retAddr;


    //函数在定义的时候建立的symbolFunc
    //这个是需要添加到符号表里的！！！！
    public SymbolFunc(String tmp1, ASTNode tmp2,ASTNode tmp3) {
        //tmp1是name
        super(tmp1);
        //tmp2是funcType
        if (tmp2 == null) {
            hasReturnValue = true; //对应int main的情况
        }
        else if (tmp2.getFuncType().equals("int")) {
            hasReturnValue = true;
        }
        else {
            hasReturnValue = false;
        }
        //tmp3是funcFParams
        //这个函数有可能没有funcFParams
        if (tmp3 == null) {
            dims = new ArrayList<>();
        }
        else {
            dims = new ArrayList<>();
            ArrayList<ASTNode> params = tmp3.getSonsNodes();
            for (int i = 0; i < params.size(); i++) {
                dims.add(params.get(i).getDim());
            }
        }
    }


    //TODO 这个可能会出现的错误较多
    //通过funcRParams建立的symbolFunc
    public SymbolFunc(ASTNode unaryExp) {
        super(unaryExp.getSonsNodes().get(0).getIdValue());
        SymbolTable symbolTable = unaryExp.getCurSymbolTable();
        ArrayList<ASTNode> sonNodes = unaryExp.getSonsNodes();
        if (sonNodes.size() == 1) {
            this.dims = new ArrayList<>();
            //有可能没有funcRParams
        }
        else if (sonNodes.get(1).getType() == ASTNode.FUNCRPARAMS) {
            this.dims = new ArrayList<>();
            ArrayList<ASTNode> funcRPrams = sonNodes.get(1).getSonsNodes();
            for (int i = 0; i < funcRPrams.size(); i++) {
                ASTNode exp = funcRPrams.get(i);
                ASTNode addExp = exp.getSonsNodes().get(0);
                //如果addExp多余两个子数组那么一定是int类型
                if (addExp.getSonsNodes().size() > 1) {
                    this.dims.add(1);
                    continue;
                }
                ASTNode mulExp = addExp.getSonsNodes().get(0);
                if (mulExp.getSonsNodes().size() > 1) {
                    this.dims.add(1);
                    continue;
                }
                ASTNode unaryExp2 = mulExp.getSonsNodes().get(0);
                ASTNode primary = unaryExp2.getSonsNodes().get(0);
                // + - !
                if (primary.getType() == ASTNode.UNARYOP) {
                    this.dims.add(1);
                    continue;
                }
                // function
                else if (primary.getType() == ASTNode.IDENT) {
                    String name = primary.getIdValue();
                    SymbolFunc func = (SymbolFunc) symbolTable.lookUpInGlobal(name);
                    //void类型函数
                    if (!func.hasReturnValue) {
                        this.dims.add(-1);
                    } else {
                        this.dims.add(1);
                    }
                    continue;
                }
                ASTNode lval = primary.getSonsNodes().get(0);
                if (lval.getType() != ASTNode.LVAL) {
                    this.dims.add(1);
                    continue;
                }
                String lvalName = lval.getSonsNodes().get(0).getIdValue();
                SymbolVar symbolVar = (SymbolVar) symbolTable.lookUpInGlobal(lvalName);
                if (symbolVar == null) {
                    this.dims.add(233);
                } else {
                    int dim = symbolVar.getDim();
                    for (int j = 0; j < lval.getSonsNodes().size(); j++) {
                        if (lval.getSonsNodes().get(j).getType() == ASTNode.EXP) {
                            dim--;
                        }
                    }
                    this.dims.add(dim);
                }
            }
        }
    }


    public boolean getHasReturnValue() {
        return this.hasReturnValue;
    }

    public ArrayList getDims() {
        return this.dims;
    }

    public void setReturnValue(ASTNode tmp) {
        this.returnValue = tmp;
    }

    public ASTNode getReturnValue() {
        return this.returnValue;
    }

    public void setRetAddr(IRSymbol tmp) {
        this.retAddr = tmp;
    }

    public IRSymbol getRetAddr() {
        return this.retAddr;
    }
}
