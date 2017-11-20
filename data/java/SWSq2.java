package Utils;



import Entity.CloneCode;
import Entity.ClonePair;
import Entity.MethodDef;
import org.antlr.v4.runtime.Token;
import org.omg.CosNaming.NamingContextPackage.InvalidNameHolder;
import sun.security.provider.MD2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
//abcdefghijklmnopqrsjtuv
//abcxdefghiymzjlukpqsjtuv
/**
 * @Author: YanMing
 * @Description: 使用标记已匹配字符串方式回溯
 * @Date: Created in 15:58 2017/11/7
 */
public class SWSq2 {
    private int[][] M;
    private int[][] hm;
    private int[] TravelA;
    private int[] TravelB;
    private static int SPACE;                           //空格匹配的得分
    private static int MATCH;                           //两个字母相同的得分
    private static int DISMACH;                         //两个字母不同的得分
    private final int THRESHOLD = 30;


    private ArrayList<Loc> Locs;                        //得分达到阈值的侯选位置
    private ArrayList<ClonePair> clonePairs;            //所有检测出的代码克隆位置信息列表，在匹配串中的位置

    public List<String> subSq1, subSq2;                 //克隆对具体信息储存容器具体匹配行号

    public SWSq2() {

        subSq1 = new ArrayList<>();
        subSq2 = new ArrayList<>();
        Locs = new ArrayList<>();
        clonePairs = new ArrayList<>();
        SPACE = -1;
        MATCH = 1;
        DISMACH = -1;
    }

    /**
     * 清空位置和克隆对信息，下次使用
     */
    public void clear() {
        Locs.clear();
        clonePairs.clear();
    }

    private int max(int a, int b, int c) {
        int maxN;
        if (a >= b)
            maxN = a;
        else
            maxN = b;
        if (maxN < c)
            maxN = c;
        if (maxN < 0)
            maxN = 0;
        return maxN;
    }

    /**
     * 计算矩阵值s1为行，s2为列
     */
    private void calculateMatrix(ArrayList<Token> s1, ArrayList<Token> s2, int m, int n) {//计算得分矩阵
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                // 计算M矩阵
                if (s1.get(i - 1).getType() == s2.get(j - 1).getType())
                    M[i][j] = max(M[i - 1][j - 1] + MATCH, M[i][j - 1] + SPACE, M[i - 1][j] + SPACE);
                else
                    M[i][j] = max(M[i - 1][j - 1] + DISMACH, M[i][j - 1] + SPACE, M[i - 1][j] + SPACE);


                // 初始化计算 hm矩阵
                if (M[i][j] == 0) {
                    hm[i][j] = 0;
                } else if (s1.get(i - 1).getType() == s2.get(j - 1).getType()) {
                    hm[i][j] = Math.max(M[i - 1][j - 1], hm[i - 1][j - 1]);
                } else {
                    // 找到 (i,j)的父节点
                    if (M[i][j] == M[i - 1][j] + SPACE) {
                        hm[i][j] = Math.max(M[i - 1][j], hm[i - 1][j]);
                    } else if (M[i][j] == M[i][j - 1] + SPACE) {
                        hm[i][j] = Math.max(M[i][j - 1], hm[i][j - 1]);
                    } else {
                        hm[i][j] = Math.max(M[i - 1][j - 1], hm[i - 1][j - 1]);
                    }
                }
                //计算辅助矩阵
                if (hm[i][j] - M[i][j] >= THRESHOLD) {
                    hm[i][j] = 0;
                    M[i][j] = 0;
                }
                //记录得分超过阈值的侯选位置
                if (M[i][j] > THRESHOLD && M[i][j] > hm[i][j]) {
                    Locs.add(new Loc(i, j, M[i][j]));
                }
            }
        }
        LocComparator locComparator = new LocComparator();
        Locs.sort(locComparator);
    }

    /**
     * 回溯 寻找最相似子序列
     */
    private void traceBack(MethodDef s1, MethodDef s2, int m, int n) {
        for (Loc loc : Locs) {
            int tmpM = loc.getRow();
            int tmpN = loc.getCol();
            // 判断当前取出的位置是否被访问过
            if (TravelA[tmpM]==0&&TravelB[tmpN] == 0) {
                //记录回溯路径
                Stack<GenericPair> trace = new Stack<>();
                boolean traveled = false;
                // 直到当前矩阵值为1，不断寻找前驱
                while (M[tmpM][tmpN] != 0) {
                    //如果中途发现被访问过
                    if (TravelA[tmpM]==1&&TravelB[tmpN] == 1) {
                        //清空回溯路径并退出
                        while (!trace.empty()) {
                            GenericPair pair = trace.pop();
                            int mindex = (Integer) pair.getFirst();
                            int nindex = (Integer) pair.getSecond();
                            TravelA[mindex] = 0;
                            TravelB[nindex] = 0;
                        }
                        traveled = true;
                        break;
                    } else {
                        //标记当前位置，记录回溯路径，同时寻找前驱
                        TravelA[tmpM] = 1;
                        TravelB[tmpN] = 1;
                        trace.push(new GenericPair(new Integer(tmpM), new Integer(tmpN)));
                        if (M[tmpM][tmpN] == M[tmpM - 1][tmpN] + SPACE) {
                            tmpM = tmpM - 1;
                        } else if (M[tmpM][tmpN] == M[tmpM][tmpN - 1] + SPACE) {
                            tmpN = tmpN - 1;
                        } else {
                            tmpM = tmpM - 1;
                            tmpN = tmpN - 1;
                        }
                    }
                }
                //如果整个过程中都没有进入已标记区
                //存储首尾信息
                if (!traveled) {
                    CloneCode codeA = new CloneCode(tmpM, loc.getRow() - 1);
                    codeA.setMethodHeader(s1.getMethodHeader());
                    CloneCode codeB = new CloneCode(tmpN, loc.getCol() - 1);
                    codeB.setMethodHeader(s2.getMethodHeader());
                    ClonePair clonePair = new ClonePair(codeA, codeB);
                    clonePairs.add(clonePair);
                }
            }
        }
    }

    /**
     * 打印矩阵
     */
    public void printMatrix(int[][] H) {
        for (int i = 0; i < H.length; i++) {
            for (int j = 0; j < H[i].length; j++) {
                System.out.print(H[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * 算法进入函数
     */
    public void find(MethodDef s1, MethodDef s2) {
        //计算矩阵规格
        //初始化矩阵
        int ROW_NUM = s1.getbodyTokens().size() + 1;
        int COL_NUM = s2.getbodyTokens().size() + 1;
        M = new int[ROW_NUM][COL_NUM];
        hm = new int[ROW_NUM][COL_NUM];
        TravelA = new int[ROW_NUM];
        TravelB = new int[COL_NUM];
        for (int i = 0; i < ROW_NUM; i++) {
            M[i][0] = hm[i][0] = 0;
            TravelA[i] = 0;
        }
        for (int j = 0; j < COL_NUM; j++) {
            M[0][j] = hm[0][j] = 0;
            TravelB[j] = 0;
        }
        //计算矩阵
        calculateMatrix(s1.getbodyTokens(), s2.getbodyTokens(), ROW_NUM, COL_NUM);
        //回溯矩阵
        traceBack(s1, s2, ROW_NUM, COL_NUM);
        //储存最终克隆信息
        for (ClonePair pair : clonePairs) {
            CloneCode codeA = pair.getCode1();
            CloneCode codeB = pair.getCode2();
            int aS = codeA.getSindex();
            int aE = codeA.getEindex();
            int bS = codeB.getSindex();
            int bE = codeB.getEindex();
            StringBuffer sbA = new StringBuffer();
            sbA.append(s1.getMethodHeader() + ":");
            StringBuffer sbB = new StringBuffer();
            sbB.append(s2.getMethodHeader() + ":");
            sbA.append(s1.getbodyTokens().get(aS).getLine() + "<->" + s1.getbodyTokens().get(aE).getLine());
            sbB.append(s2.getbodyTokens().get(bS).getLine() + "<->" + s2.getbodyTokens().get(bE).getLine());
            subSq1.add(sbA.toString());
            subSq2.add(sbB.toString());
        }
    }

    /**
     * @Author: YanMing
     * @Description: 存储矩阵中单元格位置信息和得分值
     * @Date: 2017/11/17 21:47
     *
     */
    class Loc {
        private int row;
        private int col;
        private int matrixVal;

        public Loc(int m, int n, int matrixVal) {
            this.row = m;
            this.col = n;
            this.matrixVal = matrixVal;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getMatrixVal() {
            return matrixVal;
        }
    }

    /**
     * @Author: YanMing
     * @Description: 矩阵位置比较器，矩阵值越大，或者越靠近右下角优先级越高
     * @Date: 2017/10/28 22:05
     */
    class LocComparator implements Comparator<SWSq2.Loc> {
        @Override
        public int compare(SWSq2.Loc o1, SWSq2.Loc o2) {
            if (o1.getMatrixVal() > o2.getMatrixVal()) {
                return -1;
            } else if (o1.getMatrixVal() < o2.getMatrixVal()) {
                return 1;
            } else {
                if (o1.getRow() > o2.getRow())
                    return -1;
                else if (o1.getRow() < o2.getRow())
                    return 1;
                else
                    return 0;
            }
        }
    }

    public List<String> getSubSq1() {
        return subSq1;
    }
    public List<String> getSubSq2() {
        return subSq2;
    }
}