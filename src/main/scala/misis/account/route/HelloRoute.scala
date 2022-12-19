package misis.account.route

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._

class HelloRoute {
    def route =
        (path("hello") & get) {
            complete("Hello scala world!")
        }
}
