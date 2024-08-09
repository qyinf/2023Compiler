import java.util.HashMap;

public class Sheet {
    private HashMap<String,String> map;

    public Sheet() {
        this.map = new HashMap<>();
        this.map.put("!","NOT");
        this.map.put("*","MULT");
        this.map.put("=","ASSIGN");
        this.map.put("&&","AND");
        this.map.put("/","DIV");
        this.map.put(";","SEMICN");
        this.map.put("||","OR");
        this.map.put("%","MOD");
        this.map.put(",","COMMA");
        this.map.put("<","LSS");
        this.map.put("(","LPARENT");
        this.map.put("<=","LEQ");
        this.map.put(")","RPARENT");
        this.map.put(">","GRE");
        this.map.put("[","LBRACK");
        this.map.put(">=","GEQ");
        this.map.put("]","RBRACK");
        this.map.put("+","PLUS");
        this.map.put("==","EQL");
        this.map.put("{","LBRACE");
        this.map.put("-","MINU");
        this.map.put("!=","NEQ");
        this.map.put("}","RBRACE");
        this.map.put("Ident","IDENFR");
        this.map.put("IntConst","INTCON");
        this.map.put("DoubleConst","DOUBLECON");
        this.map.put("FormatString","STRCON");
        this.map.put("else","ELSETK");
        this.map.put("void","VOIDTK");
        this.map.put("main","MAINTK");
        this.map.put("const","CONSTTK");
        this.map.put("getint","GETINTTK");
        this.map.put("int","INTTK");
        this.map.put("printf","PRINTFTK");
        this.map.put("break","BREAKTK");
        this.map.put("return","RETURNTK");
        this.map.put("continue","CONTINUETK");
        this.map.put("if","IFTK");
        this.map.put("for","FORTK");
        this.map.put("double","DOUBLETK");
        //ADD 在这里补充了两个系统函数的名字
        this.map.put("do","DOTK");
        this.map.put("while","WHILETK");
    }

    public String getCode(String tmp) {
        if(this.map.containsKey(tmp)) {
            return this.map.get(tmp);
        }
        else if (isNumeric(tmp)) {
            return "INTCON";
        }
        else if (isDouble(tmp)) {
            return "DOUBLECON";
        }
        else {
            return "IDENFR";
        }
    }

    private boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    private boolean isDouble(String str) {
        boolean hasPoint = false;
        for (int i = 0;i < str.length();i++) {
            if (!Character.isDigit(str.charAt(i))) {
                if (str.charAt(i) == '.') {
                    hasPoint = true;
                }
                else {
                    return false;
                }
            }
        }
        return hasPoint;
    }
}
