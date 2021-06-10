package com.github.octavelarose.codegenerator;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.CTParserProgramBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;

import java.util.HashMap;

/**
 * Main class for the code generator program.
 */
public class CodeGenerator {
    /**
     * Main function to generate a codebase.
     * @param args Unused args.
     */
    public static void main(String[] args) {
        HashMap<String, ClassBuilder> builders;

        try {
//            builders = new TestProgramBuilder().build();
            builders = new CTParserProgramBuilder().build();
        } catch (BuildFailedException e) {
            e.printStackTrace();
        }

//        new ProgramExporter().export(builders);
    }
}