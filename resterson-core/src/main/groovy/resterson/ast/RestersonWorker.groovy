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

import javax.servlet.AsyncContext
/**
 * This class is a worker for processing asynchronous requests. This type
 * should be used with asynchronous servlets 3.0+
 *
 * @author Mario Garcia
 */
class RestersonWorker implements Runnable {

    AsyncContext context
    Closure closure

    RestersonWorker(AsyncContext context, Closure closure) {
        this.context = context
        this.closure = closure
    }

    void run() {
        context.with(closure)
        context.complete()
    }

}
