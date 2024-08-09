package IR;

public class IRImm implements IRSymbol{
    private int value;

    public IRImm(int tmp) {
        this.value = tmp;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int getId() {
        return this.value;
    }

}
