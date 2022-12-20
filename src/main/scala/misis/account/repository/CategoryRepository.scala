package misis.account.repository

import misis.account.model._

import java.util.UUID
import scala.concurrent.Future

trait CategoryRepository {
    def list(): Future[Seq[Category]]
    def get(id: UUID): Future[Category]
    def find(id: UUID): Future[Option[Category]]
    def create(item: CreateCategory): Future[Category]
    def updateCashback(item: UpdateCashback): Future[Either[String, Category]]
    def delete(id: UUID): Future[Unit]
}
