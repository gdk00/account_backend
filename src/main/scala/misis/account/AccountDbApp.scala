package misis.account

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import misis.account.db.InitDb
import misis.account.repository._
import misis.account.route._
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContextExecutor

object AccountDbApp extends App {
    implicit val system: ActorSystem = ActorSystem("CartApp")
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    implicit val db: PostgresProfile.backend.Database = Database.forConfig("database.postgres")
    val port = ConfigFactory.load().getInt("port")

    new InitDb().prepare()
    val client = new ExternalTransferClient()
    val accounts = new AccountRepositoryDb(client)
    val categories = new CategoryRepositoryDb()

    val categoryRoute = new CategoryRoute(categories).route
    val helloRoute = new HelloRoute().route
    val accountRoute = new AccountRoute(accounts, categories).route

    Http().newServerAt("0.0.0.0", port).bind(helloRoute ~ accountRoute ~ categoryRoute)
}
