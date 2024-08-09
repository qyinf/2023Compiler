package Symbols;

import java.util.ArrayList;

public class SymbolVar extends Symbol{
    private boolean isConst;

    //维度,和每个维度对应的长度
    private int dim;

    private int[] dimLen;

    private int[][] constValue;

    private boolean hasConstValue;

    public SymbolVar(String tmp1,boolean tmp2,int tmp3) {
        super(tmp1);
        this.isConst = tmp2;
        this.dim = tmp3;
        this.dimLen = new int[]{0,0,0};
        //TODO 初始化成什么还不确定
        this.hasConstValue = false;
    }

    public boolean getIsConst() {
        return this.isConst;
    }

    public int getDim() {
        return this.dim;
    }

    //设置const常数的值
    public void setConstValue(int tmp) {
        this.constValue = new int[1][1];
        this.constValue[0][0] = tmp;
        this.hasConstValue = true;
    }

    //设置const一维数组的值
    public void setOneDimConstArrayValue(int tmp1, ArrayList<Integer> tmp2) {
        this.constValue = new int[1][tmp1];
        for (int i = 0;i < tmp1;i++) {
            constValue[0][i] = tmp2.get(i);
        }
        this.hasConstValue = true;
    }

    //设置const二维数组的值
    public void setTwoDimConstArrayValue(ArrayList<ArrayList<Integer>> tmp1,int tmp2,int tmp3) {
        this.constValue = new int[tmp2][tmp3];
        for (int i = 0;i < tmp2;i++) {
            ArrayList<Integer> now = tmp1.get(i);
            for (int j = 0;j < now.size();j++) {
                this.constValue[i][j] = now.get(j);
            }
        }
        this.hasConstValue = true;
    }

    //设置普通常数的值
    public void setVarValue(int tmp) {
        this.constValue = new int[1][1];
        this.constValue[0][0] = tmp;
    }

    //设置普通一维数组
    public void setOneVarArrayValue(int tmp1,ArrayList<Integer> tmp2) {
        this.constValue = new int[1][tmp1];
        for (int i = 0;i < tmp1;i++) {
            this.constValue[0][i] = tmp2.get(i);
        }
    }

    //设置普通二维数组
    public void setTwoVarArrayValue(ArrayList<ArrayList<Integer>> tmp1,int tmp2,int tmp3) {
        this.constValue = new int[tmp2][tmp3];
        for (int i = 0;i < tmp2;i++) {
            ArrayList<Integer> now = tmp1.get(i);
            for (int j = 0;j < now.size();j++) {
                this.constValue[i][j] = now.get(j);
            }
        }
    }
    //设置某个维数的长度
    public void setDimLen(int tmp1,int tmp2) {
        this.dimLen[tmp1] = tmp2;
    }


    //判断是否是const并且有值
    public boolean hasConstValue() {
        return this.hasConstValue;
    }

    //取出常量的值，分为几种不同的情况
    public int getConstValue() {
        return this.constValue[0][0];
    }

    public int getConstValue(int dim) {
        return this.constValue[0][dim];
    }

    public int getConstValue(int dim1,int dim0) {
        return this.constValue[dim1][dim0];
    }

    //直接在这里算出分配空间
    public int getArraySize() {
        if (this.dim == 2) {
            return this.dimLen[0];
        }
        else if (this.dim == 3) {
            return this.dimLen[1] * this.dimLen[0];
        }
        else {
            return -233;
        }
    }

    //得到数组每个维度的长度,二维数组赋值那里用的
    public int[] getDimLen() {
        return this.dimLen;
    }

    //如果是常数，就返回常数
    //如果是数组，就以此返回数组的值
    public ArrayList<Integer> getValue() {
        ArrayList<Integer> res = new ArrayList<>();
        if (this.dim == 1) {
            res.add(this.constValue[0][0]);
        }
        else if (this.dim == 2) {
            for (int i = 0;i < dimLen[0];i++) {
                res.add(constValue[0][i]);
            }
        }
        else {
            for (int i = 0;i < dimLen[1];i++) {
                for (int j = 0;j < dimLen[0];j++) {
                    res.add(constValue[i][j]);
                }
            }
        }
        return res;
    }
}
