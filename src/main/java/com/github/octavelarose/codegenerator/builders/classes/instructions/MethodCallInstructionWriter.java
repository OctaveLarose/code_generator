package com.github.octavelarose.codegenerator.builders.classes.instructions;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Writes a call to one method in another, i.e "methodName(3, "hello", 1.2);"
 * TODO: needs to take classes into account, currently only method names and no classes are mentioned/instantiated.
 */
public class MethodCallInstructionWriter {
    ClassBuilder callerClass;
    ClassBuilder calleeClass;
    CallableDeclaration<?> callerMethod;
    CallableDeclaration<?> calleeMethod;

//    public MethodCallInstructionWriter(CallableDeclaration<?> callerMethod, CallableDeclaration<?> calleeMethod) {
//        this.callerMethod = callerMethod;
//        this.calleeMethod = calleeMethod;
//    }

    public MethodCallInstructionWriter(ClassBuilder callerClass, CallableDeclaration.Signature callerMethodSignature,
                                       ClassBuilder calleeClass, CallableDeclaration.Signature calleeMethodSignature) {
        this.callerMethod = callerClass.getMethodFromSignature(callerMethodSignature); // TODO use Signature object
        this.calleeMethod = calleeClass.getMethodFromSignature(calleeMethodSignature);
        if (this.callerMethod == null || this.calleeMethod == null)
            System.out.println("olala keskisspass " + callerMethodSignature.asString() + ", " + calleeMethodSignature.asString());
        this.callerClass = callerClass;
        this.calleeClass = calleeClass;
    }

    public void writeMethodCallInCaller() throws BuildFailedException {
        BlockStmt methodBody;

        // System.out.println(callerMethod.getName() + " calls " + calleeMethod.getName());

        if (callerMethod instanceof MethodDeclaration) {
            MethodDeclaration md = ((MethodDeclaration) callerMethod);
            if (md.getBody().isEmpty()) { // Should never happen since methods are always instantiated with an empty block
                methodBody = new BlockStmt();
                md.setBody(methodBody);
            } else {
                methodBody = md.getBody().get();
            }
        } else if (callerMethod instanceof ConstructorDeclaration)
            methodBody = ((ConstructorDeclaration) callerMethod).getBody();
        else
            throw new BuildFailedException("Method is neither a classic method nor a constructor");

        String methodCallStatementStr = getMethodSignatureWithValues(calleeMethod.getSignature().asString());
//        System.out.println(methodCallStatementStr);
//        methodBody.addStatement(methodCallStatementStr);

        methodBody.addStatement(new MethodCallExpr(new NameExpr(callerClass.getName()), calleeMethod.getName()));
    }

    private String getMethodSignatureWithValues(String methodCallSigStr) {
        String paramTypes = methodCallSigStr.substring(
                methodCallSigStr.indexOf("(") + 1,
                methodCallSigStr.length() - 1);
        List<String> splitParamTypes = Arrays.asList(paramTypes.split(", "));
        List<String> paramVals = new ArrayList<>();

        if (splitParamTypes.isEmpty())
            return "";

        for (String paramType: splitParamTypes)
            paramVals.add(getParamValueStrFromType(paramType));

        return methodCallSigStr.substring(0, methodCallSigStr.indexOf("("))
                + "("
                + String.join(", ", paramVals)
                + ")"
                + ";";
    }

    private String getParamValueStrFromType(String typeStr) {
        if (typeStr.endsWith("[]"))
            return "new " + typeStr.substring(0, typeStr.length() - 2) + "[]{}";

        switch (typeStr) {
            case "":
                return "";
            case "boolean":
                return String.valueOf(RandomUtils.generateRandomBool());
            case "int":
            case "long":
            case "short":
                return String.valueOf(RandomUtils.generateRandomInt(10000));
            default: // We assume it's an object
                return "null";
        }
    }
}
