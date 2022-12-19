package misis.account

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import misis.account.db.InitDb
import misis.account.repository._
import misis.account.route._
import slick.jdbc.PostgresProfile.api._

object AccountDbApp extends App {
    implicit val system: ActorSystem = ActorSystem("CartApp")
    implicit val ec = system.dispatcher
    implicit val db = Database.forConfig("database.postgres")

    new InitDb().prepare()
    val accounts = new AccountRepositoryDb()
    
    val helloRoute = new HelloRoute().route
    val accountRoute = new AccountRoute(accounts).route

    Http().newServerAt("0.0.0.0", 8080).bind(helloRoute ~ accountRoute)
}
