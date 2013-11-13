package core.conf

import com.google.inject.servlet.ServletModule

import core.filter.BasicAuthenticationFilter
import core.servlet.HelloWorldServlet
import core.service.AuthenticationService
import core.service.DummyAuthenticationService

class AppGuiceModule extends ServletModule {


    void configureServlets() {

        super.configureServlets();

        serve("/hello").with(HelloWorldServlet)
        filter("/*").through(BasicAuthenticationFilter)

        bind(AuthenticationService).to(DummyAuthenticationService)

    }

}
