package misis.account.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import misis.account.model.{CreateCategory, UpdateCashback}
import misis.account.repository.CategoryRepository

import scala.concurrent.ExecutionContext

class CategoryRoute(repository: CategoryRepository)(implicit ec: ExecutionContext) extends FailFastCirceSupport {
    def route =
        (path("categories") & get) {
            val list = repository.list()
            complete(list)
        } ~
          path("category") {
            (post & entity(as[CreateCategory])) { createCategory =>
              complete(repository.create(createCategory))
            }
        } ~
        path("category" / "update_cashback") {
          (put & entity(as[UpdateCashback])) { updateCashback =>
            complete(repository.updateCashback(updateCashback))
          }
        } ~
          path("category" / JavaUUID) { id =>
              get {
                  complete(repository.get(id))
              }
          } ~
          path("category" / JavaUUID) { id =>
              delete {
                  complete(repository.delete(id))
              }
          }
}
