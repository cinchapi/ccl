/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cinchapi.ccl.generator;

import com.cinchapi.common.base.CheckedExceptions;
import org.javacc.jjtree.JJTree;

/**
 * A command line interface to generate the JavaCC grammar files
 */
public class ParserGenerator {
    public static void main(String... args) {
        String[] jjtree_args = {
                "-OUTPUT_DIRECTORY=src/main/java/com/cinchapi/ccl/generator",
                "src/main/java/com/cinchapi/ccl/generator/grammar.jjt" };
        String[] jj_args = {
                "-OUTPUT_DIRECTORY=src/main/java/com/cinchapi/ccl/generator",
                "src/main/java/com/cinchapi/ccl/generator/grammar.jj" };

        if(args.length > 0) {
            String command = args[0];
            if(command.equalsIgnoreCase("generate")) {

                // Run JJTree generation
                JJTree jjtree = new JJTree();
                int result = jjtree.main(jjtree_args);

                // Run JJ generation
                try {
                    int errorcode = org.javacc.parser.Main.mainProgram(jj_args);
                }
                catch (Exception exception) {
                    throw CheckedExceptions.throwAsRuntimeException(exception);
                }
            }
            else {
                System.err.println("Illegal command " + command);
                System.exit(1);
            }
        }
        else {
            System.err.println("Please specify a command");
            System.exit(1);
        }
    }

}
