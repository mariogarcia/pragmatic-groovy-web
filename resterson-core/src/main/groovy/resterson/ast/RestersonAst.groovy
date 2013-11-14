/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package resterson.ast

import javax.servlet.annotation.WebServlet

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * This transfomation visits all classes annotated
 * with @Resterson and transform them into servlets
 *
 * @author Mario Garcia
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class RestersonAst extends TypeAnnotatedAst {

    RestersonAst() {
        super(Resterson)
    }

    void visitClassNode(final TypeAnnotatedStep info){

        info.classNode.with {
            innerClasses = declaredMethods
                .collect{ methodNode -> createWebServletClassNode(methodNode) }
                .findAll{ it }
        }

        println info.classNode.innerClasses

    }

    /**
     * This method creates an HttpServlet ClassNode per each method inside
     * the annotated class.
     *
     * @param methodNode The node the servlet is going to be extracted from
     * @return ClassNode
     */
    ClassNode createWebServletClassNode(final MethodNode methodNode) {
        AstBuilder.buildFromSpec {
            ClassHelper.make(HttpServlet) {
                method 'doGet', ACC_PUBLIC, Void.class, {
                    parameters {
                        parameter 'request': HttpServletRequest
                        parameter 'response': HttpServletResponse
                    }

                }
                annotations {
                    annotation(WebServlet) {
                        member 'value', { constant methodNode.name }
                        member 'asyncSupported', { constant true }
                    }
                }
            }
        }?.find { it }
    }

}
