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
package core.filter

import groovy.util.logging.Log

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

import core.service.AuthenticationService

import com.google.inject.Inject
import com.google.inject.Singleton

/**
 * This filter checks whether the path should be protected and ask for
 * basic authentication if necesary. Secondly if the user has been
 * authenticated, the filter checks the credentials against an authentication service.
 *
 * This is just a review of how to implement Basic Auth using JEE and its filters
 *
 * @author marioggar
 */
@Log
@Singleton
class BasicAuthenticationFilter extends DefaultFilter {

    @Inject
    AuthenticationService authenticationService

    void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain) {

        def path = request.servletPath

        if (isProtectedPath(path)) {

            log.info "This resource is protected"
            log.info "Let's see whether your credentials are correct or not"

            def authenticationHeader = request.getHeader('Authorization')

            if (!authenticationHeader) {

                log.info "The user is not allowed to go to $path (Not authenticated)"

                reauthenticate(response)
                return

            }

            def credentials = getUsernameAndPassword(authenticationHeader)

            if (!areCredentialsValid(credentials)) {

                log.info "The user is not allowed to go to $path (Bad Credentials)"

                reauthenticate(response)
                return

            }

        }

        log.info "The user is allowed to go to $path"

        chain.doFilter(request, response)

    }

    boolean areCredentialsValid(Map credentials) {

        return authenticationService.checkCredentials(credentials.username, credentials.password)

    }

    boolean isProtectedPath(path) {

        return path == '/hello'

    }

    void reauthenticate(ServletResponse response) {

        response.setHeader('WWW-Authenticate','Basic realm="simple"')
        response.sendError(401)

    }

    Map getUsernameAndPassword(String authenticationHeader) {

        def rawUserPassword = authenticationHeader - "Basic "
        def decodedUserPassword = new String(rawUserPassword.decodeBase64())
        def (username, password) = decodedUserPassword.split(':')

        return [username: username, password: password]

    }

}
