package resterson.ast

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.builder.AstBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import java.util.regex.Pattern

/**
 * This class builds a given servlet method depending on the HTTP method available
 */
class HttpServletMethodBuilder {

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
        }?.find { it }

        return doGetMethodNode

    }


}
