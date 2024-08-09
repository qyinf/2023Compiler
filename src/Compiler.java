import Exceptions.ASTException;
import Exceptions.LexicalException;
import IR.Calculator;
import IR.IRGenerator;
import MIPS.MIPSGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {
    public static void readFile(StringBuilder sb,String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Scanner scanner = new Scanner(path);
        while (scanner.hasNext()) {
            String tmp = scanner.nextLine();
            sb.append(tmp);
            sb.append("\n");
        }
        scanner.close();
    }

    public static void main(String[] args) throws FileNotFoundException, LexicalException, ASTException {
        String inputPath = "testfile.txt";
        String outputPath = "mips.txt";
        String errorPath = "error.txt";
        StringBuilder sb = new StringBuilder();
        try {
            readFile(sb,inputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrintStream printStream = new PrintStream(outputPath);
        PrintStream errorStream = new PrintStream(errorPath);
        //提交的时候不要注释掉
        System.setOut(printStream);
        //1.词法分析程序,生成单词列表
        Lexer lexer = new Lexer(sb.toString());
        //lexer.print();
        //2.语法分析程序，生成语法树
        Parser parser = new Parser(lexer.getWords());
        parser.Analysis();
        //3.错误处理程序，输出错误
        ArrayList<Error> errors = parser.getErrors();
        if (errors.size() != 0) {
            System.setOut(errorStream);
            parser.printError();
            return;
        }
        //parser.printError();
        //4.优化1,先把所有的常量计算出来
        Calculator calculator = new Calculator(parser.getGlobalCompUnit());
        calculator.simplify();
        //calculator.printUnit(parser.getGlobalCompUnit());
        //5.中间代码生成
        IRGenerator irGenerator = new IRGenerator(parser.getGlobalCompUnit());
        irGenerator.genCompUnit();
        //irGenerator.outputIR();
        //6.MIPS代码生成
        MIPSGenerator mipsGenerator = new MIPSGenerator(irGenerator);
        mipsGenerator.genMips();
        mipsGenerator.printMips();
//        }
    }
}
