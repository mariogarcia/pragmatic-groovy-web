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

import javax.servlet.http.HttpServlet
import javax.servlet.annotation.WebServlet

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.builder.AstBuilder

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation

import resterson.ast.HttpMethodBuilder as MB

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

    RestersonAst() {
        super(Resterson)
    }

    void visitClassNode(final TypeAnnotatedAstStep info){

        info.classNode.with {
            methods.findAll { it.name.startsWith(SLASH) }.eachWithIndex { methodNode, index ->
                module.addClass(
                    createWebServletClassNode(methodNode, index)
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
    ClassNode createWebServletClassNode(final MethodNode methodNode, final Integer index) {

        def innerClassNode = buildHttpServletInnerClass(methodNode, index)
        def doGetMethodNode = MB.buildDoGetMethodFrom(methodNode)
        def webServletAnnotation = buildWebServletAnnotationFrom(methodNode)

        innerClassNode.addMethod(doGetMethodNode)
        innerClassNode.addAnnotation(webServletAnnotation)

        return innerClassNode

    }

    /**
     * This method builds an inner class with the information taken from a given
     * method.
     *
     * The name of the inner class should be different from their siblings, that's
     * why we're using an index
     *
     * @param methodNode The method we are taking information from
     * @param index We use an index to make each inner class unique
     * @return An inner class node extending HttpServlet
     */
    InnerClassNode buildHttpServletInnerClass(MethodNode methodNode,Integer index) {

        String innerClassName = methodNode.declaringClass.name + DOLLAR + "Inner$index"

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

        return innerClassNode

    }

    /**
     * This method builds the @WebServlet annotation
     */
    AnnotationNode buildWebServletAnnotationFrom(MethodNode methodNode) {

        def annotation = new AnnotationNode(ClassHelper.make(WebServlet, false))
        annotation.setMember('value', new ConstantExpression(methodNode.name))

        return annotation

    }

}

