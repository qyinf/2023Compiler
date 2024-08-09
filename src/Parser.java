import Exceptions.ASTException;
import Symbols.Symbol;
import Symbols.SymbolFunc;
import Symbols.SymbolTable;
import AST.ASTNode;
import Symbols.SymbolVar;
import Word.Word;
import Word.StringWord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Parser {
    private ASTNode globalCompUnit;

    private int pos;

    private ArrayList<Error> errors;

    private ArrayList<Word> words;

    public Parser(ArrayList<Word> tmp) {
        this.globalCompUnit = null;
        this.pos = 0;
        errors = new ArrayList<>();
        this.words = tmp;
    }

    public ASTNode getGlobalCompUnit() {
        return this.globalCompUnit;
    }

    public void Analysis() throws ASTException {
        this.globalCompUnit = readCompUnit();
    }

    public void print() {
//        System.out.println(words.get(pos).getType() + " " +
//                words.get(pos).getValue());
        pos++;
    }

    public void printError() {
        Collections.sort(errors);
        for (int i = 0;i < errors.size();i++) {
            System.out.println(errors.get(i));
        }
    }

    public String getNow() {
        if (pos < words.size()) {
            return words.get(pos).getValue();
        }
        return null;
    }

    public String getNext() {
        if (pos + 1 < words.size()) {
            return words.get(pos + 1).getValue();
        }
        return null;
    }

    public String getNextNext() {
        if (pos + 2 < words.size()) {
            return words.get(pos + 2).getValue();
        }
        return null;
    }

    public ASTNode readCompUnit() throws ASTException {
        ASTNode compUnit = new ASTNode(ASTNode.COMPUNIT);
        SymbolTable globalSymbolTable = new SymbolTable();
        compUnit.setCurSymbolTable(globalSymbolTable);
        if (pos >= this.words.size()) {
            return null;
        }
        //往下递归捏
        ASTNode decl;
        ASTNode funcDef;
        ASTNode mainFuncDef;
        //decl
        while (!(getNext().equals("main") || getNextNext().equals("("))) {
            decl = readDecl(globalSymbolTable);
            compUnit.addSonNode(decl);
        }
        //funcDef
        while (!getNext().equals("main")) {
            funcDef = readFuncDef(globalSymbolTable);
            compUnit.addSonNode(funcDef);
        }
        //mainFuncDef
        mainFuncDef = readMainFuncDef(globalSymbolTable);
        compUnit.addSonNode(mainFuncDef);
        return compUnit;
    }

    public ASTNode readDecl(SymbolTable tmp) throws ASTException {
//        if (pos >= this.words.size()) {
//            return null;
//        }
        ASTNode decl = new ASTNode(ASTNode.DECL);
        decl.setCurSymbolTable(tmp);
        if (getNow().equals("const")) {
            decl.addSonNode(readConstDecl(tmp));
        } else {
            decl.addSonNode(readVarDecl(tmp));
        }
        return decl;
    }

    public ASTNode readConstDecl(SymbolTable tmp) throws ASTException {
//        if (pos >= this.words.size()) {
//            return null;
//        }
        ASTNode constDecl = new ASTNode(ASTNode.CONSTDECL);
        constDecl.setCurSymbolTable(tmp);
        //const
        print();
        //Btype int，没有double
        constDecl.addSonNode(readBType());
        //constDef
        constDecl.addSonNode(readConstDef(tmp));
        //还可能会继续，也可能不会继续
        while (getNow().equals(",")) {
            print();
            constDecl.addSonNode(readConstDef(tmp));
        }
        //;
        //TODO errorI:缺少分号
        if (!getNow().equals(";")) {
            errors.add(new Error("i",words.get(pos - 1).getLine()));
        } else {
            print();
        }
        return constDecl;
    }

    public ASTNode readConstDef(SymbolTable tmp) throws ASTException {
//        if (pos >= this.words.size()) {
//            return null;
//        }
        ASTNode constDef = new ASTNode(ASTNode.CONSTDEF);
        constDef.setCurSymbolTable(tmp);
        //Ident
        Word identWord = this.words.get(pos);
        ASTNode ident = new ASTNode(ASTNode.IDENT);
        ident.setIdValue(identWord.getValue());
        constDef.addSonNode(ident);
        print();
        //[
        int brackCount = 1;
        while (getNow().equals("[")) {
            brackCount++;
            print();//[
            ASTNode constExp = readConstExp(tmp);
            constDef.addSonNode(constExp);//constExp
            //TODO errorK:缺少右中括号
            if (!getNow().equals("]")) {
                //这里不需要抛出异常
                errors.add(new Error("k",words.get(pos - 1).getLine()));
            } else {
                print();
            }
        }
        //=
        print();
        //constInitVal
        ASTNode constInitVal = readConstInitVal(tmp);
        constDef.addSonNode(constInitVal);
        //TODO errorB:同一个符号表内名字重复定义
        SymbolVar symbolVar = new SymbolVar(ident.getIdValue(),true,brackCount);
        if (!tmp.addSymbol(symbolVar)) {
            errors.add(new Error("b",identWord.getLine()));
        }
        return constDef;
    }

    public ASTNode readFuncDef(SymbolTable tmp) throws ASTException {

        //symbolTable.0
        SymbolTable newSymbolTable = new SymbolTable();
        newSymbolTable.setParentInfo(tmp);
        //new Node
        ASTNode funcDef = new ASTNode(ASTNode.FUNCDEF);
        funcDef.setCurSymbolTable(tmp);
        //funcType
        ASTNode funcType = readFuncType();
        funcDef.addSonNode(funcType);
        //Ident
        Word identWord = words.get(pos);
        ASTNode ident = new ASTNode(ASTNode.IDENT);
        ident.setIdValue(identWord.getValue());
        funcDef.addSonNode(ident);
        print();
        //(
        print();
        //funcFParams
        //右括号
        ASTNode funcFParams = null;
        //TODO 如果这里一直出bug就考虑回溯吧
        //很好，如果没有形参不会进入
        if (!getNow().equals(")") && !getNow().equals("{")) {
            funcFParams = readFuncFParams(newSymbolTable);
            funcDef.addSonNode(funcFParams);
        }
        //TODO errorJ:缺少右小括号
        if (!getNow().equals(")")) {
            errors.add(new Error("j",words.get(pos - 1).getLine()));
        }
        else {
            print();
        }
        //TODO errorB:有关函数的名字重定义
        SymbolFunc symbolFunc = new SymbolFunc(ident.getIdValue(),funcType,funcFParams);
        newSymbolTable.setNowFunction(symbolFunc);
        if (!tmp.addSymbol(symbolFunc)) {
            errors.add(new Error("b",identWord.getLine()));
        }
        ASTNode block = readBlock(newSymbolTable);
        funcDef.addSonNode(block);

        //TODO errorG:
        return funcDef;
    }

    public ASTNode readMainFuncDef(SymbolTable tmp) throws ASTException {

        //符号表
        SymbolTable newSymbolTable = new SymbolTable();
        newSymbolTable.setParentInfo(tmp);
        //new Node
        ASTNode mainFuncDef = new ASTNode(ASTNode.MAINFUNCDEF);
        mainFuncDef.setCurSymbolTable(tmp);
        //int
        print();
        //main
        int errorLine = words.get(pos).getLine();
        print();
        //print();
        //(
        print();
        //)
        if (!getNow().equals(")")) {
            //TODO errorJ:缺少右中括号
            errors.add(new Error("j",words.get(pos - 1).getLine()));
        }
        else {
            print();
        }
        //也不可以和main重复哦
        SymbolFunc mainSymbolFunc = new SymbolFunc("main",null,null);
        newSymbolTable.setNowFunction(mainSymbolFunc);
        if (!tmp.addSymbol(mainSymbolFunc)) {
            errors.add(new Error("b",errorLine));
        }
        //block
        ASTNode block = readBlock(newSymbolTable);
        mainFuncDef.addSonNode(block);
        return mainFuncDef;
    }


    //
    public ASTNode readVarDecl(SymbolTable tmp) throws ASTException {
        if (pos >= this.words.size()) {
            return null;
        }
        ASTNode varDecl = new ASTNode(ASTNode.VARDECL);
        varDecl.setCurSymbolTable(tmp);
        //int or double
        varDecl.addSonNode(readBType());
        //varDef
        ASTNode varDef = readVarDef(tmp);
        varDecl.addSonNode(varDef);
        while(Objects.equals(getNow(), ",")) {
            print();
            varDecl.addSonNode(readVarDef(tmp));
        }
        //TODO errorI:缺少分号
        if (!getNow().equals(";")) {
            errors.add(new Error("i",words.get(pos - 1).getLine()));
        }
        else {
            print();
        }
        return varDecl;
    }

    //int or double
    public ASTNode readBType() throws ASTException {
        ASTNode bType = new ASTNode(ASTNode.BTYPE);
        print();
        return bType;
    }


    public ASTNode readConstExp(SymbolTable tmp) throws ASTException {
        if (pos >= this.words.size()) {
            return null;
        }
        //new Node
        ASTNode constExp = new ASTNode(ASTNode.CONSTEXP);
        constExp.setCurSymbolTable(tmp);
        //addExp
        ASTNode addExp = readAddExp(tmp);
        constExp.addSonNode(addExp);
        return constExp;
    }

    public ASTNode readConstInitVal(SymbolTable tmp) throws ASTException {
        ASTNode constInitVal = new ASTNode(ASTNode.CONSTINITVAL);
        constInitVal.setCurSymbolTable(tmp);
        if (getNow().equals("{")) {
            //{
            print();
            if (!getNow().equals("}")) {
                constInitVal.addSonNode(readConstInitVal(tmp));
                while (Objects.equals(",",getNow())) {
                    //,
                    print();
                    constInitVal.addSonNode(readConstInitVal(tmp));
                }
            }
            //}
            print();
        }
        else {
            constInitVal.addSonNode(readConstExp(tmp));
        }
        return constInitVal;
    }

    public ASTNode readVarDef(SymbolTable tmp) throws ASTException {
        if (pos >= this.words.size()) {
            return null;
        }
        ASTNode varDef = new ASTNode(ASTNode.VARDEF);
        varDef.setCurSymbolTable(tmp);
        //Ident
        Word identWord = words.get(pos);
        ASTNode ident = new ASTNode(ASTNode.IDENT);
        ident.setIdValue(identWord.getValue());
        varDef.addSonNode(ident);
        print();
        //[
        int brackCount = 1;
        while (getNow().equals("[")) {
            brackCount++;
            //[
            print();
            //constExp
            ASTNode constExp = readConstExp(tmp);
            varDef.addSonNode(constExp);
            //]
            if (!getNow().equals("]")) {
                //TODO errorK:缺少右中括号
                errors.add(new Error("k",words.get(pos - 1).getLine()));
            } else {
                print();
            }
        }
        if (getNow().equals("=")) {
            print();
            varDef.addSonNode(readInitVal(tmp));
        }
        //TODO errorB:var变量名字重复定义
        SymbolVar symbolVar = new SymbolVar(ident.getIdValue(),false,brackCount);
        if (!tmp.addSymbol(symbolVar)) {
            errors.add(new Error("b",identWord.getLine()));
        }
        return varDef;
    }

    public ASTNode readInitVal(SymbolTable tmp) throws ASTException {
        ASTNode initVal = new ASTNode(ASTNode.INITVAL);
        initVal.setCurSymbolTable(tmp);
        if (getNow().equals("{")) {
            print();
            if (!getNow().equals("}")) {
                initVal.addSonNode(readInitVal(tmp));
                while (Objects.equals(",",getNow())) {
                    print();
                    initVal.addSonNode(readInitVal(tmp));
                }
            }
            print();
        }
        else {
            initVal.addSonNode(readExp(tmp));
        }
        return initVal;
    }

    public ASTNode readExp(SymbolTable tmp) throws ASTException {

        ASTNode exp = new ASTNode(ASTNode.EXP);
        exp.setCurSymbolTable(tmp);
        ASTNode addExp = readAddExp(tmp);
        exp.addSonNode(addExp);
        return exp;
    }

    public ASTNode readFuncType() {
        //void or int
        ASTNode funcType = new ASTNode(ASTNode.FUNCTYPE);
        funcType.setFuncType(words.get(pos).getValue());
        print();
        return funcType;
    }

    public ASTNode readFuncFParams(SymbolTable tmp) throws ASTException {
        ASTNode funcFParams = new ASTNode(ASTNode.FUNCFPARAMS);
        funcFParams.setCurSymbolTable(tmp);
        //funcFParam
        ASTNode funcFParam = readFuncFParam(tmp);
        funcFParams.addSonNode(funcFParam);
        while (Objects.equals(",",getNow())) {
            print();
            funcFParams.addSonNode(readFuncFParam(tmp));
        }
        return funcFParams;
    }

    public ASTNode readBlock(SymbolTable tmp) throws ASTException {
        ASTNode block = new ASTNode(ASTNode.BLOCK);
        block.setCurSymbolTable(tmp);
        //{
        print();
        while(!getNow().equals("}")) {
            block.addSonNode(readBlockItem(tmp));
        }
        //}
        int errorLine = words.get(pos).getLine();
        print();
        //TODO errorG:检查有返回值的函数是否存在error语句
        SymbolFunc symbolFunc = tmp.getBlockFunction();
        if (symbolFunc != null) {
            if (symbolFunc.getHasReturnValue() && symbolFunc.getReturnValue() == null) {
                errors.add(new Error("g", errorLine));
            }
        }
        return block;
    }

    public ASTNode readFuncFParam(SymbolTable tmp) throws ASTException {
        ASTNode funcFParam = new ASTNode(ASTNode.FUNCFPARAM);
        funcFParam.setCurSymbolTable(tmp);
        //btype
        funcFParam.addSonNode(readBType());
        //Ident
        Word identWord = words.get(pos);
        ASTNode ident = new ASTNode(ASTNode.IDENT);
        ident.setIdValue(identWord.getValue());
        funcFParam.addSonNode(ident);
        print();
        //[
        int brackCount = 1;
        if (getNow().equals("[")) {
            brackCount++;
            print();
            //TODO errorK
            if (!getNow().equals("]")) {
                errors.add(new Error("k",words.get(pos - 1).getLine())); }
            else {
                print(); }
            while (getNow().equals("[")) {
                brackCount++;
                print();
                funcFParam.addSonNode(readConstExp(tmp));
                //TODO errorK:缺少右中括号
                if (!getNow().equals("]")) {
                    errors.add(new Error("k",words.get(pos - 1).getLine()));
                }
                else {
                    print();
                }
            }
        }
        //TODO errorB 函数块内部变量名字重复定义
        //TODO 是否要设置成const还没曾想好
        SymbolVar symbolVar = new SymbolVar(ident.getIdValue(),false,brackCount);
        if (!tmp.addSymbol(symbolVar)) {
            errors.add(new Error("b",identWord.getLine()));
        }
        funcFParam.setDim(brackCount);
        return funcFParam;
    }

    public ASTNode readBlockItem(SymbolTable tmp) throws ASTException {

        ASTNode blockItem = new ASTNode(ASTNode.BLOCKITEM);
        blockItem.setCurSymbolTable(tmp);
        if (getNow().equals("int") || getNow().equals("const") || getNow().equals("double")) {
            ASTNode decl = readDecl(tmp);
            blockItem.addSonNode(decl);
        }
        else {
            ASTNode stmt = readStmt(tmp);
            blockItem.addSonNode(stmt);
        }
        return blockItem;
    }

    public ASTNode readStmt(SymbolTable tmp) throws ASTException {
        ASTNode stmt = new ASTNode(ASTNode.STMT);
        stmt.setCurSymbolTable(tmp);
        //if
        if (getNow().equals("if")) {
            ASTNode ifNode = new ASTNode(ASTNode.IF);
            stmt.addSonNode(ifNode);
            //if
            print();
            //(
            print();
            //cond
            stmt.addSonNode(readCond(tmp));
            //)
            //TODO errorJ:缺少右小括号
            if (!getNow().equals(")")) {
                errors.add(new Error("j",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            //stmt
            stmt.addSonNode(readStmt(tmp));
            //else
            if (getNow().equals("else")) {
                print();
                stmt.addSonNode(readStmt(tmp));
            }
        }
        else if (getNow().equals("for")) {
            ASTNode forNode = new ASTNode(ASTNode.FOR);
            stmt.addSonNode(forNode);
            //for
            print();
            //(
            print();
            //forStmt
            if (!getNow().equals(";")) {
                stmt.addSonNode(readForStmt(tmp));
            }
            //;
            if (!getNow().equals(";")) {
                errors.add(new Error("i",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            if (!getNow().equals(";")) {
                stmt.addSonNode(readCond(tmp));
            }
            //cond
            //;
            if (!getNow().equals(";")) {
                errors.add(new Error("i",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            //forStmt
            if (!getNow().equals(")")) {
                stmt.addSonNode(readForStmt(tmp));
            }
            //)
            if (!getNow().equals(")")) {
                errors.add(new Error("j",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            //stmt
            SymbolTable newSymbolTable = new SymbolTable();
            newSymbolTable.setParentInfo(tmp);
            newSymbolTable.setInCycle(true);
            stmt.addSonNode(readStmt(newSymbolTable));
        }
//        else if (getNow().equals("do")) {
//            ASTNode doNode = new ASTNode(ASTNode.DO);
//            stmt.addSonNode(doNode);
//            //新符号表
//            SymbolTable newSymbolTable = new SymbolTable();
//            newSymbolTable.setParentInfo(tmp);
//            newSymbolTable.setInCycle(true);
//            //do
//            print();
//            //whileStmt
//            ASTNode whileStmt = readStmt(newSymbolTable);
//            stmt.addSonNode(whileStmt);
//            //while
//            print();
//            //(
//            print();
//            //cond
//            ASTNode cond = readCond(tmp);
//            stmt.addSonNode(cond);
//            //)
//            if (!getNow().equals(")")) {
//                errors.add(new Error("j",words.get(pos - 1).getLine()));
//            }
//            else {
//                print();
//            }
//            //;
//            print();
//        }
        else if (getNow().equals("break")) {
            //break
            //TODO 只有error的时候才会用到
            int errorLine = words.get(pos).getLine();
            print();
            ASTNode breakNode = new ASTNode(ASTNode.BREAK);
            stmt.addSonNode(breakNode);
            //;
            //TODO errorI:缺少分号
            if (!Objects.equals(";",getNow())) {
                errors.add(new Error("i",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            //TODO errorM:是否位于循环体内
            if (!tmp.getInCycle()) {
                errors.add(new Error("m",errorLine));
            }
        }
        else if (Objects.equals("continue",getNow())) {
            //continue
            //TODO 只有error需要用到
            int errorLine = words.get(pos).getLine();
            print();
            ASTNode continueNode = new ASTNode(ASTNode.CONTINUE);
            stmt.addSonNode(continueNode);
            //;
            //TODO errorI:缺少分号
            if (!Objects.equals(";",getNow())) {
                errors.add(new Error("i",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            //TODO errorM:是否位于循环体内
            if (!tmp.getInCycle()) {
                errors.add(new Error("m",errorLine));
            }
        }
        else if (Objects.equals("return",getNow())) {
            ASTNode returnNode = new ASTNode(ASTNode.RETURN);
            stmt.addSonNode(returnNode);
            //return
            //TODO 只有error的时候采用
            int errorLine = words.get(pos).getLine();
            print();
            //; or 其他
            boolean hasReturnValue = false;
            //;
            if (!Objects.equals(";",getNow())) {
                ASTNode returnExp = readExp(tmp);
                stmt.addSonNode(returnExp);
                tmp.getNowFunction().setReturnValue(returnExp);
                hasReturnValue = true;
            }
            else {
                tmp.getNowFunction().setReturnValue(null);
            }
            //TODO errorF:无返回值的函数存在不匹配的return
            //TODO bug1这里修改了以后会返回两次g
            boolean funcReturnValue = tmp.getNowFunction().getHasReturnValue();
            if (!funcReturnValue && hasReturnValue) {
                errors.add(new Error("f",errorLine));
            }
            else if (funcReturnValue && !hasReturnValue){
                errors.add(new Error("g",errorLine));
            }
            //TODO errorI:缺少分号
            if (!Objects.equals(";",getNow())) {
                errors.add(new Error("i",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
        }
        else if (Objects.equals("printf",getNow())) {
            //printf
            //TODO 只在error的时候使用
            int errorLine = words.get(pos).getLine();
            print();
            ASTNode printfNode = new ASTNode(ASTNode.PRINTF);
            stmt.addSonNode(printfNode);
            //(
            print();
            //FormatString
            Word word = words.get(pos);
            print();
            int stringCount = 0;
            if (word instanceof StringWord) {
                stringCount = ((StringWord) word).getCount();
                if (!((StringWord) word).isCorrect()) {
                    errors.add(new Error("a",word.getLine()));
                }
            }
            else {
                System.out.println("FormatString错误!!!");
            }
            ASTNode formatString = new ASTNode(ASTNode.FORMATSTRING);
            formatString.setFormatstring((StringWord) word);
            stmt.addSonNode(formatString);
            //,
            int count = 0;
            while (Objects.equals(",",getNow())) {
                print();
                stmt.addSonNode(readExp(tmp));
                count++;
            }
            //)
            //TODO errorJ:没有右括号
            if (!Objects.equals(")",getNow())) {
                errors.add(new Error("j",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            //;
            //TODO errorI:缺少分号
            if (!Objects.equals(";",getNow())) {
                errors.add(new Error("i",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
            //TODO errorL:printf参数个数不一致
            if (count != stringCount) {
                errors.add(new Error("l",errorLine));
            }
        }
        else if (Objects.equals("{",getNow())) {
            SymbolTable newSymbolTable = new SymbolTable();
            newSymbolTable.setParentInfo(tmp);
            stmt.addSonNode(readBlock(newSymbolTable));
        }
        //TODO BUG 不知道这样修改对不对
        else if (!check()) {
            int errorLine = words.get(pos).getLine();
            if (!Objects.equals(getNow(), ";") &&
                    words.get(pos + 1).getLine() == errorLine) {
                stmt.addSonNode(readExp(tmp));
            }
            if (Objects.equals(getNow(), ";")) {
                print();
            }
            else {
                //TODO errorI
                errors.add(new Error("i",words.get(pos - 1).getLine()));
            }
        }
        else {
            /* LVal=Exp,LVal=getint()，存在回溯可能*/
            //[EXP]
            int errorLine = words.get(pos).getLine();
            ASTNode lval = readLVal(tmp);
            String lvalName = lval.getSonsNodes().get(0).getIdValue();
            Symbol check = tmp.lookUpInGlobal(lvalName);
            //TODO errorC:未定义的名字
            if (check == null) {
                //System.out.println("stmt中lval的错误 " + lvalName);
                errors.add(new Error("c",errorLine));
            }
            //TODO errorH:不能给const赋值
            else {
                if (check instanceof SymbolVar) {
                    boolean isConst = ((SymbolVar) check).getIsConst();
                    if (isConst) {
                        errors.add(new Error("h", errorLine));
                    }
                }
            }
            stmt.addSonNode(lval);
            //=
            print();
            if (Objects.equals("getint",getNow())) {
                print();//getint
                print();//(
                //)
                //TODO errorJ:缺少右括号
                if (!Objects.equals(")",getNow())) {
                    errors.add(new Error("j",words.get(pos - 1).getLine()));
                }
                else {
                    print();
                }
                //;
                //TODO errorI:缺少分号
                if (!Objects.equals(";",getNow())) {
                    errors.add(new Error("i",words.get(pos - 1).getLine()));
                }
                else {
                    print();
                }
                ASTNode getInt = new ASTNode(ASTNode.GETINT);
                stmt.addSonNode(getInt);
            }
            else {
                errorLine = words.get(pos).getLine();
                stmt.addSonNode(readExp(tmp));
                //TODO errorI:缺少分号
                if (!Objects.equals(";",getNow())) {
                    errors.add(new Error("i",words.get(pos - 1).getLine()));
                }
                else {
                    print();
                }
            }
        }
        return stmt;
    }

    public ASTNode readCond(SymbolTable tmp) throws ASTException {

        ASTNode cond = new ASTNode(ASTNode.COND);
        cond.setCurSymbolTable(tmp);
        ASTNode lOrExp = readLOrExp(tmp);
        cond.addSonNode(lOrExp);
        return cond;
    }

    public ASTNode readLVal(SymbolTable tmp) throws ASTException {
        ASTNode lVal = new ASTNode(ASTNode.LVAL);
        lVal.setCurSymbolTable(tmp);
        //ident
        int errorLine = words.get(pos).getLine();
        ASTNode ident = new ASTNode(ASTNode.IDENT);
        ident.setIdValue(words.get(pos).getValue());
        lVal.addSonNode(ident);
        print();
        //[
        int brackCount = 1;
        while (Objects.equals("[",getNow())) {
            brackCount++;
            print();
            if (!Objects.equals(getNow(), "]")) {
                lVal.addSonNode(readExp(tmp));
                brackCount++;
            }
            if (!Objects.equals("]", getNow())) {
                //TODO errorK:没有中括号
                errors.add(new Error("k",words.get(pos - 1).getLine()));
            } else {
                print();
            }
        }
        return lVal;
    }

    public ASTNode readAddExp(SymbolTable tmp) throws ASTException {

        ASTNode addExp = new ASTNode(ASTNode.ADDEXP);
        addExp.setCurSymbolTable(tmp);
        ASTNode mulExp = readMulExp(tmp);
        addExp.addSonNode(mulExp);
        while (Objects.equals("+",getNow()) || Objects.equals("-",getNow())) {
            //TODO 这里需要存储符号了捏,和unaryOp混合使用\
            ASTNode tmpAdd = new ASTNode(ASTNode.ADDEXP);
            tmpAdd.addSonNode(addExp);
            addExp = tmpAdd;
            ASTNode expOp = new ASTNode(ASTNode.UNARYOP);
            expOp.setOp(getNow());
            addExp.addSonNode(expOp);
            print();
            addExp.addSonNode(readMulExp(tmp));
        }
        return addExp;
    }

    public ASTNode readLOrExp(SymbolTable tmp) throws ASTException {
        ASTNode lOrExp = new ASTNode(ASTNode.LOREXP);
        lOrExp.setCurSymbolTable(tmp);
        ASTNode lAndExp = readLAndExp(tmp);
        lOrExp.addSonNode(lAndExp);
        while (Objects.equals("||",getNow())) {
            //TODO 符号,这里没有添加
            ASTNode tmpLOr = new ASTNode(ASTNode.LOREXP);
            tmpLOr.addSonNode(lOrExp);
            lOrExp = tmpLOr;
            print();
            lOrExp.addSonNode(readLAndExp(tmp));
        }
        return lOrExp;
    }

    public ASTNode readLAndExp(SymbolTable tmp) throws ASTException {

        ASTNode lAndExp = new ASTNode(ASTNode.LANDEXP);
        lAndExp.setCurSymbolTable(tmp);

        ASTNode eqExp = readEqExp(tmp);
        lAndExp.addSonNode(eqExp);
        while (Objects.equals("&&",getNow())) {
            ASTNode tmpAnd = new ASTNode(ASTNode.LANDEXP);
            tmpAnd.addSonNode(lAndExp);
            lAndExp = tmpAnd;
            //TODO 符号
            print();
            lAndExp.addSonNode(readEqExp(tmp));
        }
        return lAndExp;
    }

    public ASTNode readEqExp(SymbolTable tmp) throws ASTException {

        ASTNode eqExp = new ASTNode(ASTNode.EQEXP);
        eqExp.setCurSymbolTable(tmp);
        ASTNode relExp = readRelExp(tmp);
        eqExp.addSonNode(relExp);
        while (Objects.equals("==",getNow()) || Objects.equals("!=",getNow())) {
            //TODO 符号
            ASTNode tmpEq = new ASTNode(ASTNode.EQEXP);
            tmpEq.addSonNode(eqExp);
            eqExp = tmpEq;
            ASTNode expOp = new ASTNode(ASTNode.UNARYOP);
            expOp.setOp(getNow());
            eqExp.addSonNode(expOp);
            print();
            eqExp.addSonNode(readRelExp(tmp));
        }
        return eqExp;
    }

    public ASTNode readRelExp(SymbolTable tmp) throws ASTException {

        ASTNode relExp = new ASTNode(ASTNode.RELEXP);
        relExp.setCurSymbolTable(tmp);
        ASTNode addExp = readAddExp(tmp);
        relExp.addSonNode(addExp);
        while (Objects.equals("<",getNow()) || Objects.equals("<=",getNow()) ||
                Objects.equals(">",getNow()) || Objects.equals(">=",getNow())) {
            ASTNode tmpRel = new ASTNode(ASTNode.RELEXP);
            tmpRel.addSonNode(relExp);
            relExp = tmpRel;
            ASTNode op = new ASTNode(ASTNode.UNARYOP);
            op.setOp(getNow());
            relExp.addSonNode(op);
            //TODO 符号
            print();
            relExp.addSonNode(readAddExp(tmp));
        }
        return relExp;
    }

    public ASTNode readMulExp(SymbolTable tmp) throws ASTException {

        ASTNode mulExp = new ASTNode(ASTNode.MULEXP);
        mulExp.setCurSymbolTable(tmp);
        ASTNode unaryExp = readUnaryExp(tmp);
        mulExp.addSonNode(unaryExp);
        while (Objects.equals("*",getNow()) || Objects.equals("/",getNow()) ||
                Objects.equals("%",getNow())) {
            //封装自己
            ASTNode tmpMul = new ASTNode(ASTNode.MULEXP);
            tmpMul.addSonNode(mulExp);
            mulExp = tmpMul;
            //TODO 符号
            ASTNode expOp = new ASTNode(ASTNode.UNARYOP);
            expOp.setOp(getNow());
            mulExp.addSonNode(expOp);
            print();
            mulExp.addSonNode(readUnaryExp(tmp));
        }
        return mulExp;
    }

    public ASTNode readPrimaryExp(SymbolTable tmp) throws ASTException {
        boolean isNumber = true;
        //ADD 首先readNumber的条件变化了
        for (int i = 0;i < getNow().length();i++) {
            if (!(getNow().charAt(i) >= '0' && getNow().charAt(i) <= '9')) {
                if (getNow().charAt(i) != '.') {
                    isNumber = false;
                }
            }
        }
        ASTNode primaryExp = new ASTNode(ASTNode.PRIMARYEXP);
        primaryExp.setCurSymbolTable(tmp);
        //(EXP)
        if (Objects.equals("(",getNow())) {
            print();//(
            ASTNode exp = readExp(tmp);
            primaryExp.addSonNode(exp);
            //)
            //TODO errorj:缺少右小括号
            if (!Objects.equals(")",getNow())) {
                errors.add(new Error("j",words.get(pos - 1).getLine()));
            }
            else {
                print();
            }
        }
        //number
        //TODO
        else if (isNumber) {
            ASTNode number = readNumber();
            primaryExp.addSonNode(number);
        }
        //lval
        else {
            int errorLine = words.get(pos).getLine();
            ASTNode lval = readLVal(tmp);
            //TODO errorC 标识符未定义,这里可能会re
            //TODO bug 直接放在lval那里检测
            String lvalName = lval.getSonsNodes().get(0).getIdValue();
            if (tmp.lookUpInGlobal(lvalName) == null) {
                //System.out.println("primary中lval的错误 " + lvalName);
                errors.add(new Error("c",errorLine));
            }
            primaryExp.addSonNode(lval);
        }
        return primaryExp;
    }

    public ASTNode readNumber() {
        ASTNode number = new ASTNode(ASTNode.NUMBER);
        number.setNumber(words.get(pos).getValue());
        print();
        return number;
    }

    public ASTNode readUnaryExp(SymbolTable tmp) throws ASTException {
        ASTNode unaryExp = new ASTNode(ASTNode.UNARYEXP);
        unaryExp.setCurSymbolTable(tmp);
        // + - !
        if (Objects.equals("+",getNow()) || Objects.equals("-",getNow()) ||
                Objects.equals("!",getNow())) {
            unaryExp.addSonNode(readUnaryOp());
            unaryExp.addSonNode(readUnaryExp(tmp));
        }
        //函数调用
        else if (getNext().equals("(") && !getNow().equals("(")) {
            //Ident
            int errorLine = words.get(pos).getLine();
            ASTNode ident = new ASTNode(ASTNode.IDENT);
            ident.setIdValue(words.get(pos).getValue());
            unaryExp.addSonNode(ident);
            print();
            if (tmp.lookUpInGlobal(ident.getIdValue()) == null) {
                errors.add(new Error("c",errorLine));
                //(
                print();
                if (!Objects.equals(getNow(), ")") && !Objects.equals(getNow(), ";")) {
                    unaryExp.addSonNode(readFuncRParams(tmp));
                }
                print();
            }
            else {
                //(
                print();
                String lvalName = ident.getIdValue();
                Symbol check = tmp.lookUpInGlobal(lvalName);
                //TODO 检查函数内参数的类型和个数是否匹配
                ArrayList<Integer> dims = ((SymbolFunc) check).getDims();
                if (!Objects.equals(getNow(), ")") && !Objects.equals(getNow(), ";")) {
                    unaryExp.addSonNode(readFuncRParams(tmp));
                }
                SymbolFunc callFunc = new SymbolFunc(unaryExp);
                ArrayList<Integer> funcRDims = callFunc.getDims();
                boolean valid = true;
                for (int i = 0;i < funcRDims.size();i++) {
                    if (funcRDims.get(i) == 233) {
                        valid = false;
                    }
                }
                if (valid) {
                    //TODO errorD:个数不匹配
                    if (dims.size() != funcRDims.size()) {
                        errors.add(new Error("d", errorLine));
                    }
                    //TODO errorE:类型不匹配
                    else {
                        for (int i = 0; i < dims.size(); i++) {
                            if (dims.get(i) != funcRDims.get(i)) {
                                errors.add(new Error("e", errorLine));
                                break;
                            }
                        }
                    }
                }
                if (Objects.equals(getNow(), ")")) {
                    print();
                }
                else {
                    //TODO errorJ 右括号
                    errors.add(new Error("j",words.get(pos - 1).getLine()));
                }
            }
        }
        //递归
        else {
            ASTNode primaryExp = readPrimaryExp(tmp);
            unaryExp.addSonNode(primaryExp);
        }
        return unaryExp;
    }

    public ASTNode readUnaryOp() {

        ASTNode unaryOp = new ASTNode(ASTNode.UNARYOP);
        unaryOp.setOp(words.get(pos).getValue());
        print();
        return unaryOp;
    }

    public ASTNode readForStmt(SymbolTable tmp) throws ASTException {

        ASTNode forStmt = new ASTNode(ASTNode.FORSTMT);
        forStmt.setCurSymbolTable(tmp);
        //lval
        int errorLine = words.get(pos).getLine();
        ASTNode lval = readLVal(tmp);
        forStmt.addSonNode(lval);
        String lvalName = lval.getSonsNodes().get(0).getIdValue();
        //TODO errorH:对常量赋值
        //TODO bug 这里已经检测过了…
        Symbol check = tmp.lookUpInGlobal(lvalName);
        if (check instanceof SymbolVar) {
            boolean isConst = ((SymbolVar) check).getIsConst();
            if (isConst) {
                errors.add(new Error("h",errorLine));
            }
        }
        //=
        print();
        //exp
        forStmt.addSonNode(readExp(tmp));
        return forStmt;
    }

    public ASTNode readFuncRParams(SymbolTable tmp) throws ASTException {
        ASTNode funcRParams = new ASTNode(ASTNode.FUNCRPARAMS);
        funcRParams.setCurSymbolTable(tmp);
        funcRParams.addSonNode(readExp(tmp));
        while(getNow().equals(",")) {
            print();
            funcRParams.addSonNode(readExp(tmp));
        }
        return funcRParams;
    }

    private boolean check() {
        int l = words.get(pos).getLine();
        for (int i = pos;i < words.size();i++) {
            if (words.get(i).getLine() != l) {
                break;
            }
            if (Objects.equals(words.get(i).getValue(), "=")) {
                return true;
            }
            if (Objects.equals(words.get(i).getValue(), ";")) {
                return false;
            }
        }
        return false;
    }

    public ArrayList<Error> getErrors() {
        return this.errors;
    }

}
