package com.dataA;
import Entity.MethodDef;
import Traverse.Java8Lexer;
import Traverse.Java8Parser;
import Traverse.Java8VisitorParser;
import Utils.SWSq2;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: YanMing
 * @Description: 测试类
 * @Date: 2017/11/17 21:56
 */
public class TestClass {
    public static void main(String args[]) {

        LinkedList<MethodDef> queue = new LinkedList<>();                                   //储存方法定义队列
        TestClass mainClass = new TestClass();
        String dataDirPath = System.getProperty("user.dir") + "\\data";                     //获取测试集路径
        File dataDir = new File(dataDirPath);
        File[] flist = dataDir.listFiles();                                                //获取测试集所有文件
        if (flist != null) {
            long startTime = System.currentTimeMillis();
            System.out.printf("Collecting method of %d files\n", flist.length);
            for (File file : flist) {

                String filename = dataDirPath + "\\" + file.getName();
                String codeA = mainClass.readContent(filename);
                ParseTree parseTreeA = mainClass.getParseTree(codeA).compilationUnit();

                Java8VisitorParser JParserA = new Java8VisitorParser();
                JParserA.setMethodDefs(queue);
                JParserA.visit(parseTreeA);

            }

            SWSq2 sq1 = new SWSq2();
            int t = 1, cnt = mainClass.getFactorial(queue.size());
            while (!queue.isEmpty()) {
                MethodDef methodDef = queue.pop();
                for (int i = 0; i < queue.size(); i++) {
                    System.out.printf("Finding %dth pair in total %d \n", t++, cnt);
                    MethodDef cmp = queue.get(i);
                    sq1.find(methodDef, cmp);
                    sq1.clear();
                }
            }

            List<String> res1 = sq1.getSubSq1();
            List<String> res2 = sq1.getSubSq2();
            System.out.println(res1.size() + " clone pairs");
            for (int i = 0; i < res1.size(); i++) {
                System.out.println(res1.get(i));
                System.out.println(res2.get(i));
                System.out.println();
            }

            long endTime = System.currentTimeMillis();
            double time = (double) (endTime - startTime) / 1000;
            System.out.printf("Finding %d code clone pairs in %f s", res1.size(), time);

        }

    }
    /**
     * 获取文件内容
     */
    public String readContent(String filename) {
        File file = new File(filename);
        String code;
        try {
            code = FileUtils.readFileToString(file, "UTF-8");
            return code;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成Java8词法分析器
     */
    public static Java8Parser getParseTree(String code) {
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(code);
        Java8Lexer lexer = new Java8Lexer(antlrInputStream);
        CommonTokenStream commonStream = new CommonTokenStream(lexer);
        Java8Parser parseTree = new Java8Parser(commonStream);
        return parseTree;
    }

    int getFactorial(int n) {
        int res = 1;
        for (int i = 2; i < n; i++) {
            res = res + i;
        }
        return res;
    }


}
