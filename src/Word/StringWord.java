package Word;

import java.util.ArrayList;
import java.util.Arrays;

public class StringWord extends Word{
    private final int count;

    private final boolean correct;

    private ArrayList<String> list;

    public StringWord(String tmp1,String tmp2,int tmp3,int tmp4,boolean tmp5) {
        super(tmp1,tmp2,tmp3);
        this.count = tmp4;
        this.correct = tmp5;
        this.list = new ArrayList<>();
        this.stringPartition();
    }

    public int getCount() {
        return this.count;
    }

    public boolean isCorrect() {
        return this.correct;
    }

    private int stringPartition() {
        String strContext = this.getValue();
        String[] strList = strContext.split("%d");
        strList[0] = strList[0].substring(1);
        strList[strList.length - 1] = strList[strList.length - 1].substring(0, strList[strList.length - 1].length() - 1);
        list.addAll(Arrays.asList(strList));
        return strList.length - 1;
    }

    public ArrayList<String> getList() {
//        for (int i = 0;i < list.size();i++) {
//            System.out.println(list.get(i));
//        }
        return this.list;
    }
}
