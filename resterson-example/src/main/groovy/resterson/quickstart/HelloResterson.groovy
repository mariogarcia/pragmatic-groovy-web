package resterson.quickstart

import resterson.ast.Resterson

@Resterson
class HelloResterson {

    def "/hello"() {
        response.writer.out("hello ${request.id}")
    }

}
