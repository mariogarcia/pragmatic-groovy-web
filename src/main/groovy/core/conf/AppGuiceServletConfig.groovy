package core.conf

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.servlet.GuiceServletContextListener

class AppGuiceServletConfig extends GuiceServletContextListener {

    Injector getInjector() {
        return Guice.createInjector(new AppGuiceModule())
    }

}
