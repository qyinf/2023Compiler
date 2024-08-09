package IR;

public class IRArray implements IRSymbol {
    //数组起始地址,用imm立即数表示
    private IRSymbol baseAddr;

    //数组偏移地址
    private IRSymbol offSet;

    public IRArray(IRSymbol tmp1,IRSymbol tmp2) {
        this.baseAddr = tmp1;
        this.offSet = tmp2;
    }

    public IRSymbol getBaseAddr() {
        return this.baseAddr;
    }

    public IRSymbol getOffSet() {
        return this.offSet;
    }

    @Override
    public int getId() {
        return -1;
    }

}
