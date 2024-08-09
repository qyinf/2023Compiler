package MIPS;

import IR.IRChar;
import IR.IRFunc;
import IR.IRGenerator;
import IR.IRImm;
import IR.IRSymbol;
import Symbols.SymbolVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MIPSGenerator {
    //继承的部分
    private ArrayList<IRChar> IRChars;

    private HashMap<SymbolVar,IRSymbol> constMap;

    private HashMap<SymbolVar,IRSymbol> globalMap;

    private HashMap<IRSymbol,String> strMap;

    //函数
    private HashMap<String,Function> funcMap;

    private Function nowFunc;

    //全局标签
    private HashSet<IRSymbol> labelSet;

    //最后的结果
    private StringBuilder mips;


    public MIPSGenerator(IRGenerator irGenerator) {
        //继承中间代码的部分
        this.IRChars = irGenerator.getIrchars();
        this.constMap = irGenerator.getConstArray();
        this.globalMap = irGenerator.getGlobalVars();
        this.strMap = irGenerator.getStrs();
        //自己的部分
        this.labelSet = new HashSet<>();
        this.funcMap = new HashMap<>();
        //为什么是null
        this.nowFunc = null;
        //一些其他的操作
        this.labelSet.addAll(strMap.keySet());
        this.labelSet.addAll(constMap.values());
        this.labelSet.addAll(globalMap.values());
    }

    public void genMips() {
        addIRInst();
        StringBuilder sb = new StringBuilder();
        sb.append(this.printData());
        sb.append(this.genInst());
        this.mips = sb;
    }

    public void printMips() {
        String bug = this.mips.toString();
        System.out.println(bug);
    }

    public void addIRInst() {
        for (IRChar tmp : IRChars) {
            if (tmp.getType() == IRChar.FUNC) {
                Function newFunction = new Function((IRFunc) tmp.getOp3());
                funcMap.put(((IRFunc) tmp.getOp3()).getName(),newFunction);
                this.nowFunc = newFunction;
            }
            else if (tmp.getType() == IRChar.RET || tmp.getType() == IRChar.EXIT) {
                this.nowFunc = null;
            }
            else if (isOp(tmp)) {
                addIRSymbol(tmp.getOp3());
                addIRSymbol(tmp.getOp1());
                addIRSymbol(tmp.getOp2());
            }
            else if (tmp.getType() == IRChar.ASSIGN) {
                addIRSymbol(tmp.getOp3());
                addIRSymbol(tmp.getOp1());
            }
            else if (tmp.getType() == IRChar.ALLOCA) {
                addIRSymbol(tmp.getOp3());
                int size = ((IRImm) tmp.getOp1()).getValue();
                nowFunc.alloc(round(size));
            }
            else if (tmp.getType() == IRChar.BZ || tmp.getType() == IRChar.BNZ) {
                addIRSymbol(tmp.getOp1());
            }
            else if (tmp.getType() == IRChar.SETRET) {
                addIRSymbol(tmp.getOp3());
            }
            else if (tmp.getType() == IRChar.CALL) {
                addIRSymbol(tmp.getOp3());
                for (int i = 0;i < tmp.getList().size();i++) {
                    addIRSymbol(tmp.getList().get(i));
                }
            }
            else if (tmp.getType() ==IRChar.GETINT || tmp.getType() == IRChar.PRINTI) {
                addIRSymbol(tmp.getOp3());
            }
        }
    }

    private boolean isOp(IRChar tmp) {
        if (tmp.getType() == IRChar.ADD || tmp.getType() == IRChar.MINU ||
                tmp.getType() == IRChar.MULT || tmp.getType() == IRChar.DIV ||
                tmp.getType() == IRChar.MOD || tmp.getType() == IRChar.AND ||
                tmp.getType() == IRChar.LSHIFT || tmp.getType() == IRChar.RSHIFT ||
                tmp.getType() == IRChar.RASHIFT ||
                tmp.getType() == IRChar.GRE || tmp.getType() == IRChar.GEQ ||
                tmp.getType() == IRChar.LSS || tmp.getType() == IRChar.LEQ ||
                tmp.getType() == IRChar.EQL || tmp.getType() == IRChar.NEQ ||
                tmp.getType() == IRChar.LOAD || tmp.getType() == IRChar.STORE) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isArithmetic(IRChar tmp) {
        if (tmp.getType() == IRChar.ADD || tmp.getType() == IRChar.MINU ||
                tmp.getType() == IRChar.MULT || tmp.getType() == IRChar.DIV ||
                tmp.getType() == IRChar.MOD || tmp.getType() == IRChar.LSHIFT ||
                tmp.getType() == IRChar.RSHIFT || tmp.getType() == IRChar.RASHIFT ||
                tmp.getType() == IRChar.AND) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isControl(IRChar tmp) {
        if ((tmp.getType() == IRChar.BR || tmp.getType() == IRChar.BZ ||
                tmp.getType() == IRChar.BNZ || tmp.getType() == IRChar.SETRET ||
                tmp.getType() == IRChar.RET || tmp.getType() == IRChar.CALL ||
                tmp.getType() == IRChar.EXIT)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isLogical(IRChar tmp) {
        if (tmp.getType() == IRChar.GRE || tmp.getType() == IRChar.GEQ ||
                tmp.getType() == IRChar.LSS || tmp.getType() == IRChar.LEQ ||
                tmp.getType() == IRChar.EQL || tmp.getType() == IRChar.NEQ) {
            return true;
        }
        else {
            return false;
        }
    }

    public void addIRSymbol(IRSymbol tmp) {
        if (tmp instanceof IRImm) {
            return;
        }
        if (!labelSet.contains(tmp) && !nowFunc.getLocal(tmp)) {
            this.nowFunc.addLocal(tmp);
        }
    }


    //首先把前面的输出
    public StringBuilder printData() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        //const类
        for (SymbolVar symbolVar : constMap.keySet()) {
            IRSymbol tmp = constMap.get(symbolVar);
            sb.append(".align 2\n");
            sb.append("L");
            sb.append(tmp.getId());
            sb.append(":\n.word ");
            ArrayList<Integer> constValue = symbolVar.getValue();
            for (int i = 0;i < constValue.size();i++) {
                if (i != constValue.size() - 1) {
                    sb.append(constValue.get(i)).append(", ");
                }
                else {
                    sb.append(constValue.get(i));
                }
            }
            sb.append("\n");
        }
        //global类
        for (SymbolVar symbolVar : globalMap.keySet()) {
            IRSymbol tmp = globalMap.get(symbolVar);
            sb.append(".align 2\n");
            sb.append("L");
            sb.append(tmp.getId());
            sb.append(":\n.word ");
            ArrayList<Integer> constValue = symbolVar.getValue();
            for (int i = 0;i < constValue.size();i++) {
                if (i != constValue.size() - 1) {
                    sb.append(constValue.get(i)).append(", ");
                }
                else {
                    sb.append(constValue.get(i));
                }
            }
            sb.append("\n");
        }
        //formatString
        for (IRSymbol irSymbol : strMap.keySet()) {
            sb.append(".align 2\n");
            sb.append("L");
            sb.append(irSymbol.getId());
            sb.append(":\n.asciiz ");
            sb.append("\"");
            sb.append(strMap.get(irSymbol));
            sb.append("\"\n");
        }
        return sb;
    }


    //接着生成mips指令
    public StringBuilder genInst() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n.text\n");
        for (IRChar tmp : IRChars) {
            if (tmp.getType() == IRChar.FUNC) {
                String name = ((IRFunc) tmp.getOp3()).getName();
                this.nowFunc = this.funcMap.get(name);
                sb.append("Func_");
                sb.append(name);
                sb.append(":\n");
                sb.append("sw $ra, 0($sp)\n");
            }
            //生成算术指令
            else if (isArithmetic(tmp)) {
                sb.append(genArithmetic(tmp));
            }
            //生成逻辑指令
            else if (isLogical(tmp)) {
                sb.append(genLogical(tmp));
            }
            //生成赋值指令
            else if (tmp.getType() == IRChar.ASSIGN) {
                sb.append(genAssign(tmp));
            }
            //生成跳转指令
            else if (isControl(tmp)) {
                sb.append(genControl(tmp));
            }
            //nowFunc是null
            else if (tmp.getType() == IRChar.RET || tmp.getType() == IRChar.EXIT) {
                nowFunc = null;
            }
            //生成内存指令
            else if (tmp.getType() == IRChar.LOAD || tmp.getType() == IRChar.STORE ||
                    tmp.getType() == IRChar.ALLOCA) {
                sb.append(genMem(tmp));
            }
            //生成IO指令
            else if (tmp.getType() == IRChar.GETINT || tmp.getType() == IRChar.PRINTI ||
                    tmp.getType() == IRChar.PRINTS) {
                sb.append(genIO(tmp));
            }
            //输出
            else if (tmp.getType() == IRChar.LABEL) {
                sb.append("L");
                sb.append(tmp.getOp3().getId());
                sb.append(":\n");
            }
        }
        return sb;
    }

    public StringBuilder genArithmetic(IRChar tmp) {
        StringBuilder sb = new StringBuilder();
        sb.append(loadToRegister(tmp.getOp1(),"t1"));
        sb.append(loadToRegister(tmp.getOp2(),"t2"));
        if (tmp.getType() == IRChar.ADD) {
            sb.append("addu $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.MINU) {
            sb.append("subu $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.MULT) {
            sb.append("mult $t1, $t2");
            sb.append("\n");
            sb.append("mflo $t0");
        }
        else if (tmp.getType() == IRChar.DIV) {
            sb.append("div $t1, $t2");
            sb.append("\n");
            sb.append("mflo $t0");
        }
        else if (tmp.getType() == IRChar.MOD) {
            sb.append("div $t1, $t2");
            sb.append("\n");
            sb.append("mfhi $t0");
        }
        else if (tmp.getType() == IRChar.LSHIFT) {
            sb.append("sllv $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.RSHIFT) {
            sb.append("srlv $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.RASHIFT) {
            sb.append("srav $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.AND) {
            sb.append("and $t0, $t1, $t2");
        }
        sb.append("\n");
        sb.append(storeRegisterTo(tmp.getOp3(),"t0"));
        return sb;
    }

    public StringBuilder genAssign(IRChar tmp) {
        StringBuilder sb = new StringBuilder();
        IRSymbol op1 = tmp.getOp1();
        if (op1 instanceof IRImm) {
            sb.append("li $t0, ");
            sb.append(((IRImm) op1).getValue());
        }
        else {
            sb.append(loadToRegister(op1,"t1"));
            sb.append("move $t0, $t1");
        }
        sb.append("\n");
        sb.append(storeRegisterTo(tmp.getOp3(),"t0"));
        return sb;
    }

    public StringBuilder genLogical(IRChar tmp) {
        StringBuilder sb = new StringBuilder();
        sb.append(loadToRegister(tmp.getOp1(),"t1"));
        sb.append(loadToRegister(tmp.getOp2(),"t2"));
        if (tmp.getType() == IRChar.GRE) {
            sb.append("sgt $t0, $t1 ,$t2");
        }
        else if (tmp.getType() == IRChar.GEQ) {
            sb.append("sge $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.LSS) {
            sb.append("slt $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.LEQ) {
            sb.append("sle $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.EQL) {
            sb.append("seq $t0, $t1, $t2");
        }
        else if (tmp.getType() == IRChar.NEQ) {
            sb.append("sne $t0, $t1, $t2");
        }
        sb.append("\n");
        sb.append(storeRegisterTo(tmp.getOp3(),"t0"));
        return sb;
    }

    public StringBuilder genControl(IRChar tmp) {
        StringBuilder sb = new StringBuilder();
        if (tmp.getType() == IRChar.BR) {
            sb.append("j L");
            sb.append(tmp.getOp3().getId());
            sb.append("\n");
        }
        else if (tmp.getType() == IRChar.BZ) {
            sb.append(loadToRegister(tmp.getOp1(),"t1"));
            sb.append("beqz $t1, L");
            sb.append(tmp.getOp3().getId());
            sb.append("\n");
         }
        else if (tmp.getType() == IRChar.BNZ) {
            sb.append(loadToRegister(tmp.getOp1(),"t1"));
            sb.append("bnez $t1, L");
            sb.append(tmp.getOp3().getId());
            sb.append("\n");
        }
        else if (tmp.getType() == IRChar.SETRET) {
            sb.append(loadToRegister(tmp.getOp3(),"t1"));
            sb.append("move $v0, $t1");
            sb.append("\n");
        }
        else if (tmp.getType() == IRChar.RET) {
            sb.append("lw $ra, 0($sp)\n");
            sb.append("lw $sp, ");
            sb.append(-4);
            sb.append("($sp)\n");
            sb.append("jr $ra\n\n");
        }
        else if (tmp.getType() == IRChar.EXIT) {
            sb.append("li $v0, 10");
            sb.append("\n");
            sb.append("syscall");
            sb.append("\n");
        }
        else if (tmp.getType() == IRChar.CALL) {
            sb.append("li $t0, ");
            sb.append(nowFunc.getPos());
            sb.append("\n");
            sb.append("subu $t4, $sp, $t0\n");
            sb.append("sw $sp, ");
            sb.append(-4);
            sb.append("($t4)\n");
            sb.append(loadRParams(tmp.getList(),"t4"));
            sb.append("move $sp, $t4\n");
            sb.append("jal Func_");
            sb.append(((IRFunc) tmp.getOp1()).getName());
            sb.append("\n");
            sb.append("move $t0, $v0\n");
            sb.append(storeRegisterTo(tmp.getOp3(),"t0"));
        }
        return sb;
    }

    public StringBuilder genMem(IRChar tmp) {
        StringBuilder sb = new StringBuilder();
        if (tmp.getType() == IRChar.LOAD) {
            sb.append(loadToRegister(tmp.getOp1(),"t1"));
            if (tmp.getOp2() instanceof IRImm) {
                sb.append("lw $t0, ");
                //maybe
                sb.append(((IRImm) tmp.getOp2()).getValue());
                sb.append("($t1)\n");
            }
            else {
                sb.append(loadToRegister(tmp.getOp2(),"t2"));
                sb.append("addu $t1, $t1, $t2\n");
                sb.append("lw $t0, 0($t1)\n");
            }
            sb.append(storeRegisterTo(tmp.getOp3(),"t0"));
        }
        else if (tmp.getType() == IRChar.STORE) {
            sb.append(loadToRegister(tmp.getOp1(),"t1"));
            sb.append(loadToRegister(tmp.getOp3(),"t0"));
            if (tmp.getOp2() instanceof IRImm) {
                sb.append("sw $t0, ");
                sb.append(((IRImm) tmp.getOp2()).getValue());
                sb.append("($t1)\n");
            }
            else {
                sb.append(loadToRegister(tmp.getOp2(),"t2"));
                sb.append("addu $t1, $t1, $t2\n");
                sb.append("sw $t0, 0($t1)\n");
            }
        }
        else if (tmp.getType() == IRChar.ALLOCA) {
            int off = nowFunc.getOff(tmp.getOp3());
            int size = ((IRImm) tmp.getOp1()).getValue();
            sb.append("li $t0, ");
            sb.append(off + round(size));
            sb.append("\n");
            sb.append("subu $t0, $sp, $t0");
            sb.append("\n");
            sb.append(storeRegisterTo(tmp.getOp3(),"t0"));
        }
        return sb;
    }

    public StringBuilder genIO(IRChar tmp) {
        StringBuilder sb = new StringBuilder();
        if (tmp.getType() == IRChar.GETINT) {
            sb.append("li $v0, 5\n");
            sb.append("syscall\n");
            sb.append("move $t0, $v0\n");
            sb.append(storeRegisterTo(tmp.getOp3(),"t0"));
        }
        else if (tmp.getType() == IRChar.PRINTS) {
            sb.append(loadToRegister(tmp.getOp3(),"t1"));
            sb.append("li $v0, 4\n");
            sb.append("move $a0, $t1\n");
            sb.append("syscall\n");
        }
        else if (tmp.getType() == IRChar.PRINTI) {
            sb.append(loadToRegister(tmp.getOp3(),"t1"));
            sb.append("li $v0, 1\n");
            sb.append("move $a0, $t1\n");
            sb.append("syscall\n");
        }
        return sb;
    }

    public StringBuilder loadToRegister(IRSymbol tmp,String regName) {
        StringBuilder sb = new StringBuilder();
        if (labelSet.contains(tmp)) {
            sb.append("la $");
            sb.append(regName);
            sb.append(", L");
            sb.append(tmp.getId());
            sb.append("\n");
        }
        else if (tmp instanceof IRImm) {
            sb.append("li $");
            sb.append(regName);
            sb.append(", ");
            sb.append(((IRImm) tmp).getValue());
            sb.append("\n");
        }
        else {
            int addr = nowFunc.getOff(tmp);
            sb.append("lw $");
            sb.append(regName);
            sb.append(", ");
            sb.append(-addr);
            sb.append("($sp)\n");
        }
        return sb;
    }

    public StringBuilder storeRegisterTo(IRSymbol tmp,String regName) {
        StringBuilder sb = new StringBuilder();
        if (labelSet.contains(tmp)) {
            sb.append("sw $");
            sb.append(regName);
            sb.append(", L");
            sb.append(tmp.getId());
            sb.append("\n");
        }
        else if (tmp instanceof IRImm) {
            sb.append("sw $");
            sb.append(regName);
            sb.append(", ");
            sb.append(((IRImm) tmp).getValue());
            sb.append("\n");
        }
        else {
            int addr = nowFunc.getOff(tmp);
            sb.append("sw $");
            sb.append(regName);
            sb.append(", ");
            sb.append(-addr);
            sb.append("($sp)\n");
        }
        return sb;
    }

    public StringBuilder loadRParams(ArrayList<IRSymbol> rParams,String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < rParams.size();i++) {
            sb.append(loadToRegister(rParams.get(i),"t1"));
            if (i <= 3) {
                sb.append("move $a");
                sb.append(i);
                sb.append(", $t1\n");
            }
            sb.append("sw $t1, ");
            sb.append(-(8 + i * 4));
            sb.append("($");
            sb.append(name);
            sb.append(")\n");
        }
        return sb;
    }

    public int round(int num) {
        int k = num % 4;
        if (k == 0) {
            return num;
        } else {
            return num + 4 - k;
        }
    }
}
