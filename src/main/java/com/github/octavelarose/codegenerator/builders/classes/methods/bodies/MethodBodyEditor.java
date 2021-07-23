package com.github.octavelarose.codegenerator.builders.classes.methods.bodies;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.codegenerator.builders.classes.methods.DummyValueCreator;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

import java.util.List;

/**
 * Creates and manages a method body, i.e a BlockStmt object.
 */
public abstract class MethodBodyEditor {
    // We're assuming methods are divided into three parts: the instantiation of local variables, various calculations, and end return statements.
    protected final BlockStmt varsInsnBlock = new BlockStmt();
    protected final BlockStmt regularInstrsBlock = new BlockStmt();
    protected final BlockStmt returnStmtBlock = new BlockStmt();

    /**
     * Default constructor, creates a BlockStmt instance.
     */
    public MethodBodyEditor() {}

    /**
     * Generates the method body. Not meant to be called by itself, and rather wrapped in another method by a subclass.
     * @return The method body.
     */
    protected BlockStmt generateMethodBody() {
        NodeList<Statement> concatStmts = new NodeList<>();

        for (Statement stmt: this.varsInsnBlock.getStatements())
            concatStmts.add(stmt);
        for (Statement stmt: this.regularInstrsBlock.getStatements())
            concatStmts.add(stmt);
        for (Statement stmt: this.returnStmtBlock.getStatements())
            concatStmts.add(stmt);

        return new BlockStmt(concatStmts);
    }

    /**
     * Adds a variable instantiation statement.
     * @param expr The variable instantiation statement/expression. TODO make statement
     * @return A this instance.
     */
    public MethodBodyEditor addVarInsnStatement(Expression expr) {
        this.varsInsnBlock.addStatement(expr);
        return this;
    }

    /**
     * Adds a regular operation statement.
     * @param expr The regular operation statement/expression. TODO make statement
     * @return A this instance.
     */
    public MethodBodyEditor addRegularStatement(Expression expr) {
        this.regularInstrsBlock.addStatement(expr);
        return this;
    }

    /**
     * Adds a return statement to the end of a method, unless the type fed is void in which case none is necessary.
     * @param returnType The return type of the method.
     * @return A this instance.
     */
    public MethodBodyEditor addReturnStatement(Type returnType) {
        if (!returnType.isVoidType())
            this.returnStmtBlock.addStatement(new ReturnStmt(DummyValueCreator.getDummyParamValueFromType(returnType)));
        return this;
    }

    /**
     * Processes a list of arithmetic operations (ADD, SUB, etc...) and adds them to the method body.
     * @param methodOps The operations list
     */
    public void processOperationStatements(List<String> methodOps) {
        for (String op: methodOps) {
            this.addVarInsnStatement(new VariableDeclarationExpr(
                    new VariableDeclarator(PrimitiveType.doubleType(), RandomUtils.generateRandomName(5),
                            new BinaryExpr(new NameExpr(String.valueOf(RandomUtils.generateRandomFloat())),
                                    new NameExpr(String.valueOf(RandomUtils.generateRandomFloat())),
                                    this.getOperatorFromStr(op.substring(1)))))
            );
//            this.regularInstrsBlock.addStatement(new NameExpr("System.out.println(\"" + "Op.: " + op + "\")"));
        }
    }

    private BinaryExpr.Operator getOperatorFromStr(String opStr) {
        switch (opStr) {
            case "ADD":
                return BinaryExpr.Operator.PLUS;
            case "SUB":
                return BinaryExpr.Operator.MINUS;
            case "MUL":
                return BinaryExpr.Operator.MULTIPLY;
            case "DIV":
                return BinaryExpr.Operator.DIVIDE;
            default:
                return BinaryExpr.Operator.AND; // TODO throw
        }
    }
}
