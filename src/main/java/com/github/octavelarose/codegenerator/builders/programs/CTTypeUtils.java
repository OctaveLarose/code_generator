package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.type.*;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CTTypeUtils {
    /**
     * Returns a JavaParser Type object from an input string with an ASM format.
     * https://asm.ow2.io/faq.html#Q7
     * @param typeStr The string defining the type.
     * @return A corresponding Type object.
     * @throws BuildFailedException If the input string isn't correct.
     */
    static Type getTypeFromStr(String typeStr) throws BuildFailedException {
        Type returnType;
        char typeChar = typeStr.charAt(0);
        boolean isArrayType = false;

        // System.out.println(PrimitiveType.Primitive.valueOf(typeStr) + ", " + typeStr);

        if (typeChar == '[') {
            typeChar = typeStr.charAt(1);
            isArrayType = true;
        }

        if (typeChar == 'L') {
            returnType = getClassTypeFromStr(typeStr);
        } else {
            returnType = getPrimitiveTypeFromChar(typeChar);
        }

        if (isArrayType)
            returnType = new ArrayType(returnType);

        return returnType;
    }

    /**
     * Returns a type from a char that defines a primitive type in the ASM specification.
     * @param typeChar The char corresponding to a primitive type.
     * @return The type associated with the input char.
     * @throws BuildFailedException If the char doesn't correspond to any primitive type.
     */
    static Type getPrimitiveTypeFromChar(char typeChar) throws BuildFailedException {
        switch (typeChar) {
            case 'B':
                return PrimitiveType.byteType();
            case 'C':
                return PrimitiveType.charType();
            case 'D':
                return PrimitiveType.doubleType();
            case 'F':
                return PrimitiveType.floatType();
            case 'I':
                return PrimitiveType.intType();
            case 'J':
                return PrimitiveType.longType();
            case 'S':
                return PrimitiveType.shortType();
            case 'V':
                return new VoidType();
            case 'Z':
                return new PrimitiveType(PrimitiveType.Primitive.BOOLEAN);
            default:
                throw new BuildFailedException("Unknown type: " + typeChar);
        }
    }

    /**
     * Parses a string that defines a class/object, like "Ljava/lang/String".
     * @param typeStr The string that defines the class.
     * @return The type of the class.
     * @throws BuildFailedException If the class is unknown
     */
    static private Type getClassTypeFromStr(String typeStr) throws BuildFailedException {
        String className = typeStr.substring(1, typeStr.length() - 1); // Removing the L and the final ;

        // Note: for <Class extends XXX>, I don't get the info about the XXX class, so it's just Class for now...

        // TODO: needs to only show the class name, not path, and add an import (how though)...
        // If it's a class definition, it contains slashes, but JavaParser prefers dots.
        if (className.contains("/"))
            className = className.replace("/", ".");

        Optional<ClassOrInterfaceType> classWithName = new JavaParser()
                .parseClassOrInterfaceType(className)
                .getResult();

        if (classWithName.isEmpty())
            throw new BuildFailedException("Unknown class: " + typeStr.substring(1));

        return classWithName.get();
    }

    /**
     * Parses the parameters descriptor part of a method descriptor, like "ILjava/lang/String;I[DF"
     * @param paramsDescriptor The parameters descriptor string.
     * @return A list of all the types.
     * @throws BuildFailedException If the parsing fails.
     */
    static public List<Type> getTypesFromParametersStr(String paramsDescriptor) throws BuildFailedException {
        List<Type> typeArr = new ArrayList<>();
        char[] argsBuf = paramsDescriptor.toCharArray();
        boolean isArrayType;
        String PRIMITIVE_REPRES = "VZCBSIFJD";

        for (int i = 0; i < paramsDescriptor.length(); i++) {
            Type newParam;

            if (argsBuf[i] == '[') {
                isArrayType = true;
                i++;
            } else {
                isArrayType = false;
            }

            if (PRIMITIVE_REPRES.indexOf(argsBuf[i]) != -1) {
                newParam = getPrimitiveTypeFromChar(argsBuf[i]);
            } else if (argsBuf[i] == 'L') {
                String objectSubStr = paramsDescriptor.substring(i, paramsDescriptor.indexOf(";", i) + 1);
                newParam = getTypeFromStr(objectSubStr);
                i += objectSubStr.length();
            } else {
                // TODO: don't know where to put this but the parsing needs its own exception class
                throw new BuildFailedException("Parsing of parameters data failed for character " + argsBuf[i]);
            }

            if (isArrayType)
                newParam = new ArrayType(newParam);

            typeArr.add(newParam);
        }
        return typeArr;
    }

    // --- Some example from the ASM lib ---
/*    public static Type[] getArgumentTypes(final String methodDescriptor) throws BuildFailedException {
        char[] buf = methodDescriptor.toCharArray();
        int off = 1;
        int size = 0;
        while (true) {
            char car = buf[off++];
            if (car == ')') {
                break;
            } else if (car == 'L') {
                while (buf[off++] != ';') {
                }
                ++size;
            } else if (car != '[') {
                ++size;
            }
        }
        Type[] args = new Type[size];
        off = 1;
        size = 0;
        while (buf[off] != ')') {
            args[size] = getTypeFromStr(methodDescriptor.substring(off));
            off += 1;
            size += 1;
        }
        return args;
    }*/
}
