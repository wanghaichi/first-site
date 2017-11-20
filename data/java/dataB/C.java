
package com.data;
public class CanBoManager extends BaseHibernateDao<CanBo>{
    public int mmax1(int a, int b){

        InputStream is = new FileInputStream(System.getProperty("user.dir") + "/data/test.java");
        CharStream input = CharStreams.fromStream(is);
        Java8Lexer lexer = new Java8Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
//            System.out.println(tokens.toString());

        Java8Parser parser = new Java8Parser(tokens);
        ParseTree tree = parser.testMethodDeclaration();

        CustomVisitor eval = new CustomVisitor();
        // test
        // 哇这么大的注释你没看到吗！！！
        int c;
        c = a + b;
        c = a - b;
        if(a < b)
            return b;
        else
            return a;


    }
    public int mmax2(int c, int d){
        int a;
        a = c + d;
        a = c - d;
        if(c < d)
            return d;
        else
            return c;
        System.out.println("21233");
        System.out.println("wo za zhe me you xiu");
    }
}
