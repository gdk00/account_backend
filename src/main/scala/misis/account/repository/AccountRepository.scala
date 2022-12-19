package misis.account.repository

import misis.account.model._

import java.util.UUID
import scala.concurrent.Future

trait AccountRepository {
    def list(): Future[Seq[Account]]
    def get(id: UUID): Future[Account]
    def create(item: CreateAccount): Future[Account]
    def changeBalance(item: ChangeBalance, isPositive: Boolean): Future[Either[String, Account]]
    def delete(id: UUID): Future[Unit]
}
