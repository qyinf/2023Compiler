import Exceptions.LexicalException;
import Word.Word;
import Word.StringWord;
import java.util.ArrayList;

public class Lexer {

    private final Sheet sheet;

    private char[] src;

    //单词表
    private ArrayList<Word> words = new ArrayList<>();

    public Lexer(String source) throws LexicalException {
        sheet = new Sheet();
        Analysis(source);
    }

    public void Analysis(String source) throws LexicalException {
        preProcess(source.toCharArray());
        lexicalAnalysis();
    }

    public void preProcess(char[] content) {
        char[] tmp = new char[content.length + 1];
        int ptr = 0;
        for (int i = 0;i < content.length;i++) {
            //单行注释
            if (content[i] == '/' && content[i + 1] == '/') {    //可能越界？
                while(content[i] != '\n') i++;
            }
            //多行注释
            else if (content[i] == '/' && content[i + 1] == '*') {
                i += 2;
                while (!(content[i] == '*' && content[i + 1] == '/')) {
                    if (content[i] == '\n') tmp[ptr++] = '\n';
                    i++;
                }
                i++;
                tmp[ptr++] = ' ';
                continue; }
            //引号内的数据
            //引号内的\\不应该被特殊处理
            else if (content[i] == '"') {
                tmp[ptr++] = content[i++];
                while (content[i] != '"') tmp[ptr++] = content[i++];
            }

            if (content[i] != '\r') {
                tmp[ptr++] = content[i];
            }
        }

        tmp[ptr++] = '$';
        //截取处理完可用字符串部分
        this.src = new char[ptr];
        System.arraycopy(tmp, 0, this.src, 0, ptr);
//        for (int i = 0;i < ptr;i++) {
//            System.out.print(src[i]);
//        }
    }

    public void lexicalAnalysis() throws LexicalException {
        int ptr = 0;
        int line = 0;
        while (src[ptr] != '$') {
            //跳过空字符
            while (src[ptr] == ' ' || src[ptr] == '\t' || src[ptr] == '\n') {
                if (src[ptr] == '\n')
                    line++;
                ptr++;
            }
            //注意字符是否需要转义
            //TODO
            //可能的情况：系统函数名字，变量名字
            if (src[ptr] >= 'a' && src[ptr] <= 'z' || src[ptr] >= 'A' && src[ptr] <= 'Z' || src[ptr] == '_') {
                StringBuilder sb = new StringBuilder();
                while (Character.isLetterOrDigit(src[ptr]) || src[ptr] == '_') {
                    sb.append(src[ptr]);
                    ptr++;
                }
                words.add(new Word(sheet.getCode(sb.toString()),sb.toString(), line));
            }
            //Ident不可能数字开头
            else if (src[ptr] >='0' && src[ptr] <= '9') {
                StringBuilder sb = new StringBuilder();
                while (Character.isDigit(src[ptr]) || src[ptr] == '.') {
                    sb.append(src[ptr]);
                    ptr++;
                }
                words.add(new Word(sheet.getCode(sb.toString()),sb.toString(), line));
            }
            else if (src[ptr] == '"') {
                int count = 0;
                boolean flag = true;
                ptr++;
                StringBuilder sb = new StringBuilder();
                sb.append("\"");
                while (src[ptr] != '"') {
                    sb.append(src[ptr]);
                    if (src[ptr] == '\\') {
                        if (src[ptr + 1] != 'n')
                            flag = false;
                    } else if (src[ptr] == '%') {
                        if (src[ptr + 1] == 'd') {
                            count++;
                        } else {
                            flag = false;
                        }
                    }
                    //所有合法的字符
                    else if (src[ptr] == 32 || src[ptr] == 33 || src[ptr] >= 40 && src[ptr] <= 126) {
                        ptr++;
                        continue;
                    } else {
                        flag = false;
                    }
                    ptr++;
                }
                sb.append("\"");
                //这个很难和Ident，函数名字区分，所以直接手动赋type
                words.add(new StringWord("STRCON",sb.toString(), line, count, flag));
                ptr++;
            } else if (src[ptr] == '&') {
                if (src[ptr + 1] == '&') {
                    words.add(new Word(sheet.getCode("&&"), "&&", line));
                    ptr += 2;
                } else {
                    throw new LexicalException();
                }
            } else if (src[ptr] == '!') {
                if (src[ptr + 1] == '=') {
                    words.add(new Word(sheet.getCode("!="), "!=", line));
                    ptr += 2;
                } else {
                    words.add(new Word(sheet.getCode("!"), "!", line));
                    ptr++;
                }
            } else if (src[ptr] == '|') {
                if (src[ptr + 1] == '|') {
                    words.add(new Word(sheet.getCode("||"), "||", line));
                    ptr += 2;
                } else {
                    throw new LexicalException();
                }
            } else if (src[ptr] == '<') {
                if (src[ptr + 1] == '=') {
                    words.add(new Word(sheet.getCode("<="), "<=", line));
                    ptr += 2;
                } else {
                    words.add(new Word(sheet.getCode("<"), "<", line));
                    ptr++;
                }
            } else if (src[ptr] == '>') {
                if (src[ptr + 1] == '=') {
                    words.add(new Word(sheet.getCode(">="), ">=", line));
                    ptr += 2;
                } else {
                    words.add(new Word(sheet.getCode(">"), ">", line));
                    ptr++;
                }
            } else if (src[ptr] == '=') {
                if (src[ptr + 1] == '=') {
                    words.add(new Word(sheet.getCode("=="), "==", line));
                    ptr += 2;
                } else {
                    words.add(new Word(sheet.getCode("="), "=", line));
                    ptr++;
                }
            } else if (src[ptr] == '[' || src[ptr] == ']' || src[ptr] == '{' ||
                    src[ptr] == '}' || src[ptr] == '(' || src[ptr] == ')' ||
                    src[ptr] == ',' || src[ptr] == ';' || src[ptr] == '%' ||
                    src[ptr] == '+' || src[ptr] == '*' || src[ptr] == '/' ||
                    src[ptr] == '-'
            ) {
                words.add(new Word(sheet.getCode(String.valueOf(src[ptr])), String.valueOf(src[ptr]), line));
                ptr++;
            } else if (src[ptr] == '$') {
                break;
            } else {
                throw new LexicalException();
            }
        }
    }

    public void print() {
        for (Word word: this.words) {
            System.out.println(word.toString());
        }
    }

    public ArrayList<Word> getWords() {
        return this.words;
    }
}
