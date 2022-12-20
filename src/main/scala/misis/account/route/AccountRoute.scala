package misis.account.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import misis.account.model.{CreateAccount, ChangeBalance}
import misis.account.repository.{AccountRepository, CategoryRepository}
import misis.account.model.CreateTransaction

import scala.concurrent.ExecutionContext

class AccountRoute(repository: AccountRepository, category_repository: CategoryRepository)(implicit ec: ExecutionContext) extends FailFastCirceSupport {
    def route =
        (path("accounts") & get) {
            val list = repository.list()
            complete(list)
        } ~
            path("account") {
                (post & entity(as[CreateAccount])) { CreateAccount =>
                    complete(repository.create(CreateAccount))
                }
            } ~
            path("account" / JavaUUID) { id =>
                get {
                    complete(repository.get(id))
                }
            } ~
          path("account" / "increase") {
            (put & entity(as[ChangeBalance])) { changeBalance =>
              onSuccess(repository.changeBalance(changeBalance, isPositive = true)) {
                case Right(value) => complete(value)
                case Left(s) => complete(StatusCodes.NotAcceptable, s)
              }
            }
          } ~
          path("account" / "decrease") {
            (put & entity(as[ChangeBalance])) { changeBalance =>
              onSuccess(repository.changeBalance(changeBalance, isPositive = false)) {
                case Right(value) => complete(value)
                case Left(s) => complete(StatusCodes.NotAcceptable, s)
              }
            }
          } ~
          path("transaction") {
            (put & entity(as[CreateTransaction])) { createTransaction =>
              onSuccess(repository.transfer(createTransaction, category_repository)) {
                case Right(value) => complete(value)
                case Left(s) => complete(StatusCodes.NotAcceptable, s)
              }
            }
          } ~
            path("account" / JavaUUID) { id =>
                delete {
                    complete(repository.delete(id))
                }
            }
}
