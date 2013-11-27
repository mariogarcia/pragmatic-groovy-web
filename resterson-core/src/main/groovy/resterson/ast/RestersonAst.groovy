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

import java.util.regex.Pattern

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

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.classgen.VariableScopeVisitor

/**
 * This transfomation visits all classes annotated
 * with @Resterson and transform them into servlets
 *
 * @author Mario Garcia
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class RestersonAst extends TypeAnnotatedAst {

    static final String SLASH = '/'
    static final String DOLLAR = '$'
    static final List<String> HTTP_METHODS = ['GET','POST','UPDATE','DELETE','PUT','OPTIONS']
    static final String URL_MAPPINGS_REGEX = "(${HTTP_METHODS.join('|')}){0,1}?(\\/.{0,})"
    static final String RESTERSON_PACKAGE = 'resterson.ast'

    RestersonAst() {
        super(Resterson)
    }

    void visitClassNode(final TypeAnnotatedAstStep info){

        def possibleMethods = { methodNode ->
            return methodNode.name.matches(URL_MAPPINGS_REGEX)
        }

        info.classNode.with {
            methods.findAll(possibleMethods).eachWithIndex { methodNode, index ->
                def innerClass = createWebServletClassNode(methodNode, index)
                new VariableScopeVisitor(info.sourceUnit).visitClass(innerClass)
                module.addClass(innerClass)
            }
        }

    }

    /**
     * This method creates an HttpServlet ClassNode per each method inside
     * the annotated class.
     *
     * @param methodNode The node the servlet is going to be extracted from
     * @param index Used to make each inner class unique
     * @return ClassNode
     */
    ClassNode createWebServletClassNode(final MethodNode methodNode, final Integer index) {

        def innerClassNode = buildHttpServletInnerClass(methodNode, index)
        def functionalMethodNode = buildDoMethodFrom(methodNode)
        def webServletAnnotation = buildWebServletAnnotationFrom(methodNode)
        def inheritedAnnotations = buildInheritedAnnotationsFrom(methodNode)

        innerClassNode.addMethod(functionalMethodNode)
        innerClassNode.addAnnotation(webServletAnnotation)
        innerClassNode.addAnnotations(inheritedAnnotations)



        return innerClassNode

    }

    /**
     * This method builds the servlet method
     */
    MethodNode buildDoMethodFrom(final MethodNode methodNode) {

        Pattern regex = (~RestersonAst.URL_MAPPINGS_REGEX)
        String methodName = regex.matcher(methodNode.name)[0][1]
        String servletMethod = "do${methodName.toLowerCase().capitalize()}".toString()

        MethodNode doGetMethodNode = new AstBuilder().buildFromSpec {
            method(servletMethod, ClassNode.ACC_PUBLIC, Void.TYPE) {
                parameters {
                    parameter 'request': HttpServletRequest
                    parameter 'response': HttpServletResponse
                }
                exceptions { }
                block {
                    expression {
                        declaration {
                            variable 'executionContent'
                            token '='
                            closure {
                                parameters {}
                                block {
                                    expression {
                                        declaration {
                                            variable "out"
                                            token '='
                                            methodCall {
                                                variable 'response'
                                                constant "getWriter"
                                                argumentList {}
                                            }
                                        }
                                    }
                                    expression {
                                        declaration {
                                            variable "params"
                                            token '='
                                            methodCall {
                                                variable 'request'
                                                constant "getParameterMap"
                                                argumentList {}
                                            }
                                        }
                                    }
                                    expression.add methodNode.getCode()
                                }
                            }
                        }
                    }
                    expression {
                        declaration {
                            variable "asynContext"
                            token "="
                            methodCall {
                                variable 'request'
                                constant 'startAsync'
                                argumentList {
                                    variable "request"
                                    variable "response"
                                }
                            }
                        }
                    }
                    expression {
                        methodCall {
                            variable 'asyncContext'
                            constant 'start'
                            argumentList {
                                constructorCall(RestersonWorker) {
                                    argumentList {
                                        variable 'context'
                                        variable 'executionContent'
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }?.find { it }

        return doGetMethodNode

    }

    /**
     * This method builds an inner class with the information taken from a given
     * method.
     *
     * The name of the inner class should be different from their siblings, that's
     * why we're using an index
     *
     * @param methodNode MethodNode instance used to build an HttpServlet instance
     * @param index Used to make each inner class unique
     * @return An inner class node extending HttpServlet
     */
    InnerClassNode buildHttpServletInnerClass(MethodNode methodNode, Integer index) {

        def declaringClass = methodNode.declaringClass
        def declaringClassName = declaringClass.name
        def innerClassName = declaringClass.name + DOLLAR + "Inner$index"

        InnerClassNode innerClassNode = new AstBuilder().buildFromSpec {
            innerClass(innerClassName, ClassNode.ACC_PUBLIC) {
                classNode(declaringClassName, ClassNode.ACC_PUBLIC) {
                    classNode Object
                    interfaces { classNode GroovyObject }
                    mixins { }
                }
                classNode HttpServlet
                interfaces { classNode GroovyObject }
                mixins { }
            }
        }?.find { it }

        return innerClassNode

    }

    /**
     * This method builds the @WebServlet annotation
     */
    AnnotationNode buildWebServletAnnotationFrom(MethodNode methodNode) {

        def regex = ~URL_MAPPINGS_REGEX
        def urlMapping = regex.matcher(methodNode.name)[0].last()
        def annotation = new AnnotationNode(ClassHelper.make(WebServlet, false))

        annotation.setMember('value', new ConstantExpression(urlMapping))

        return annotation

    }

    List<AnnotationNode> buildInheritedAnnotationsFrom(MethodNode methodNode) {

        return methodNode.declaringClass.annotations.findAll{ annotationNode ->
            annotationNode.classNode.packageName != RESTERSON_PACKAGE
        }

    }

}

