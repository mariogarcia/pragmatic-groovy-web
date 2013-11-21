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
package core.test

import spock.lang.Specification
import org.codehaus.groovy.tools.ast.TransformTestHelper

import static org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION

/**
*
* @author marioggar
**/
class AstBaseSpec extends Specification{

	static final BASE = "./src/test/groovy/"

	/**
	 * This method helps to create a new class instance to be able to
	 * test the class that uses the transformation
	 *
	 * @param transformationClass The name of the AST transformation we want to test
	 * @return a class of the class that contains the transformation
	**/
	def getClassToTest(transformationClass){
        loadExampleWithSuffix("groovy")
	}

    def loadExampleWithSuffix(String suffix) {
        def invoker = new TransformTestHelper(
			transformationClass.newInstance(),
			INSTRUCTION_SELECTION
		)
		def qualifiedName = getClass().name.replaceAll("\\.","\\/")
		def file = new File("${BASE}${qualifiedName}Example.${suffix}")
	 /* The class we want to test */
		invoker.parse(file)

    }

    /**
     * This method asserts that a given example should failed because
     * some compilation failure
     */
    def throwFailureCompilation(transformationClass) {
        loadExampleWithSuffix("txt")
    }

}
