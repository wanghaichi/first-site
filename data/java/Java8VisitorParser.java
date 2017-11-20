package Traverse;

import Entity.MethodDef;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * Created by YanMing on 2017/9/27.
 */
public class Java8VisitorParser extends Java8BaseVisitor {
    private LinkedList<MethodDef> methodDefs;
    private String packageName;

    public void setMethodDefs(LinkedList<MethodDef> methodDefs) {
        this.methodDefs = methodDefs;
    }

    /**
     * 编译单元入口
     */
    @Override
    public Object visitCompilationUnit(Java8Parser.CompilationUnitContext ctx) {
        packageName = ctx.packageDeclaration().packageName().getText();
        LinkedList<ParseTree> queue = new LinkedList<>();
        queue.addLast(ctx);
        while (!queue.isEmpty())
        {
            ParseTree tmpNode = queue.pop();
            for(int i = 0;i<tmpNode.getChildCount();i++)
            {
                queue.addLast(tmpNode.getChild(i));
            }
            if(tmpNode instanceof Java8Parser.NormalClassDeclarationContext)
            {
                visitNormalClass((Java8Parser.NormalClassDeclarationContext) tmpNode);
            }
        }
        return visitChildren(ctx);
    }



    public void visitNormalClass(Java8Parser.NormalClassDeclarationContext ctx) {
        /**
         * @param ctx： NormalClassDeclarationContext
         * @Description: 按照 NormalClassDeclaration规则访问AST
         */
        String className = packageName+"."+ctx.Identifier().getText();
        List<Java8Parser.ClassBodyDeclarationContext> bodyDeclarations = ctx.classBody().classBodyDeclaration();   //获取所有类body声明
        for (Java8Parser.ClassBodyDeclarationContext context : bodyDeclarations) {
            Java8Parser.ClassMemberDeclarationContext tmpMember = context.classMemberDeclaration();

            if (tmpMember!=null&&tmpMember.methodDeclaration()!=null) {                                            //如果是一个存在的方法声明
                Java8Parser.MethodDeclarationContext method = tmpMember.methodDeclaration();
                String methodName = method.methodHeader().methodDeclarator().Identifier().getText();               //获取方法名
                int startLine = method.methodBody().start.getLine();                                               //获取起始和终止行号
                int endLine =method.methodBody().stop.getLine();
                String header = className + ":" + methodName + ":" + startLine + ":" + endLine;

                ArrayList<Token> tokens = new ArrayList<>();
                Stack<ParseTree> queue = new Stack<>();
                queue.push(method.methodBody());

                while (!queue.isEmpty())
                {
                    ParseTree tmp = queue.pop();
                    if(tmp.getChildCount() == 0)                                                                    //如果是叶子结点
                    {
                        tokens.add(((TerminalNode) tmp).getSymbol());                                               //添加叶子结点Token
                    }
                    else
                    {
                        int cnt = tmp.getChildCount();
                        for(int i = cnt-1;i>=0;i--)
                        {
                            queue.push(tmp.getChild(i));
                        }
                    }
                }
                MethodDef methodDef = new MethodDef(header);
                methodDef.setbodyTokens(tokens);
                methodDefs.addLast(methodDef);
            }
        }
    }
}