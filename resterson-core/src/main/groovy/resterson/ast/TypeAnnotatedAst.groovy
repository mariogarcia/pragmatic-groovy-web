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

    class TypeAnnotatedStep {
        ClassNode classNode
        AnnotationNode annotationNode
        SourceUnit sourceUnit
    }

    final Class annotationClazz

    TypeAnnotatedAst(Class annotationClazz) {
        this.annotationClazz = annotationClazz
    }

    void visit(final ASTNode[] nodes, final SourceUnit sourceUnit) {
        checkNodes(nodes)
        visitClassNode(
            new TypeAnnotatedStep(
                classNode: nodes.first(),
                annotationNode: nodes.last(),
                sourceUnit: sourceUnit
            )
        )
    }

    def checkNodes(ASTNode[] nodes) {
        nodes[0] && nodes[1] &&
        nodes[1].isInstance(ClassNode) &&
        nodes[0].classNode.declatedClass.isInstance(annotationClazz)
    }

    abstract void visitClassNode(TypeAnnotatedStep info)

}

