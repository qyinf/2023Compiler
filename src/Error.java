//该类仅用于错误处理作业
public class Error implements Comparable{
    private String type; //a~m不同类型的错误

    private int line; //错误的行号
    public Error(String type,int line) {
        this.type = type;
        this.line = line + 1;
    }

    public int getLine() {
        return this.line;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.line + " " + this.type;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Error) {
            if (this.line != ((Error) o).getLine()) {
                return this.line - ((Error) o).getLine();
            } else {
                return this.type.compareTo(((Error) o).getType());
            }
        }
        return -1;
    }
}
