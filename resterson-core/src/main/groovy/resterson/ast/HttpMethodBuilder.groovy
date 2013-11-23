package resterson.ast

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.builder.AstBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpMethodBuilder {

    /**
     * This method builds the servlet doGet method
     */
    static MethodNode buildDoGetMethodFrom(final MethodNode methodNode) {
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

        return doGetMethodNode

    }


}
