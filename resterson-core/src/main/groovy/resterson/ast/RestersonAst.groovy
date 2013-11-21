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
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.builder.AstBuilder

import org.codehaus.groovy.ast.stmt.BlockStatement

import org.codehaus.groovy.ast.expr.ConstantExpression
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

    void visitClassNode(final TypeAnnotatedAstStep info){

        info.classNode.with {
            methods.findAll { it.name.startsWith('/') }.eachWithIndex { methodNode, index ->
                module.addClass(
                    createWebServletClassNode(index, methodNode)
                )
            }
        }

    }

    /**
     * This method creates an HttpServlet ClassNode per each method inside
     * the annotated class.
     *
     * @param methodNode The node the servlet is going to be extracted from
     * @return ClassNode
     */
    ClassNode createWebServletClassNode(final Integer index, final MethodNode methodNode) {

        String innerClassName = methodNode.declaringClass.name + '$' + "Inner$index"
        InnerClassNode innerClassNode = new AstBuilder().buildFromSpec {
            innerClass(innerClassName, ClassNode.ACC_PUBLIC) {
                classNode(methodNode.declaringClass.name, ClassNode.ACC_PUBLIC) {
                    classNode Object
                    interfaces { classNode GroovyObject }
                    mixins { }
                }
                classNode HttpServlet
                interfaces { classNode GroovyObject }
                mixins { }

            }
        }?.find { it }

        MethodNode doGetMethodNode = new AstBuilder().buildFromSpec {
            method('doGet', ClassNode.ACC_PUBLIC, Void.TYPE) {
                parameters {
                    parameter 'request' : HttpServletRequest
                    parameter 'response' : HttpServletResponse
                }
                exceptions { }
                block {
                    expression {
                        declaration {
                            variable "out"
                            token "="
                            methodCall {
                                variable "response"
                                constant "getWriter"
                                argumentList {}
                            }
                        }
                    }
                    expression {
                        declaration {
                            variable "params"
                            token "="
                            methodCall {
                                variable "request"
                                constant "getParameterMap"
                                argumentList {}
                            }
                        }
                    }
                    expression.add methodNode.getCode()
                }
            }
        }?.find { it }

        innerClassNode.addMethod(doGetMethodNode)
        def annotation = new AnnotationNode(ClassHelper.make(WebServlet, false))
        annotation.setMember('value', new ConstantExpression(methodNode.name))

        innerClassNode.addAnnotation(annotation)
        innerClassNode

    }

}
