package Word;

public class Word {
    private final String type;

    private final String value;

    private final int line;

    public Word(String tmp1,String tmp2,int tmp3) {
        this.type = tmp1;
        this.value = tmp2;
        this.line = tmp3;
    }

    public String getValue() {
        return this.value;
    }

    public String getType() {
        return this.type;
    }

    public int getLine() {
        return this.line;
    }

    @Override
    public String toString() {
        return this.type + " " + this.value;
    }
}
