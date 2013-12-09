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
package resterson.quickstart

import resterson.ast.Resterson
import groovy.util.logging.Log

/**
 * This is a simple endpoint. Each enpoint has as its name the URL where is
 * going to be exposed.
 *
 * Within the method there will be some implicit variables such as
 *
 * - request: HttpServletRequest
 * - response: HttpServletResponse
 * - out : an instance of the response's java.io.PrintWriter
 * - params : a map containing parameters passed to the URL
 *
 */
//@Log
@Resterson
class GreetingsController {

    void "GET/hi"() {
        response.writer << "Hi ${params.name?.first()}"
    }

    void "/bye"() {
        out << "Bye ${request.parameterMap.name?.first()}"
    }

}
