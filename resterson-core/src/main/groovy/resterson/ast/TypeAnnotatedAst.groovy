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

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.AnnotationNode

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilePhase

import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation

/**
 * This class is the base for building any transformation using
 * an annotation used upon any other class type
 *
 * @author Mario Garcia
 */
abstract class TypeAnnotatedAst implements ASTTransformation {

    final Class annotationClazz

    TypeAnnotatedAst(Class annotationClazz) {
        this.annotationClazz = annotationClazz
    }

    void visit(final ASTNode[] nodes, final SourceUnit sourceUnit) {

        if (!checkNodes(nodes)) return

        def info = new TypeAnnotatedAstStep(
            sourceUnit: sourceUnit,
            classNode: nodes[1],
            annotationNode: nodes[0]
        )

        visitClassNode(info)

    }

    /**
     * All classes inheriting from TypeAnnotatedAst should
     * check nodes involved in the transformation should
     * be either the annotated class or the annotation used
     * to mark the transformation
     *
     * @param nodes Nodes involved in the transformation
     * @return true if the validation passes, false otherwise
     */
    def checkNodes(ASTNode[] nodes) {

        nodes[0] &&
        nodes[1] &&
        nodes[1].class.isAssignableFrom(ClassNode) &&
        nodes[0].classNode.typeClass.name == this.annotationClazz.name

    }

    /**
     * This method should be implemented by child classes
     *
     * @param info all data needed to do the transformation
     */
    abstract void visitClassNode(TypeAnnotatedAstStep info)

}

