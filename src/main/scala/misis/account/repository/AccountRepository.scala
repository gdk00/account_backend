package misis.account.repository

import misis.account.db.AccountDb.categoryTable
import misis.account.model._

import java.util.UUID
import scala.concurrent.Future

trait AccountRepository {
    def list(): Future[Seq[Account]]
    def get(id: UUID): Future[Account]
    def create(item: CreateAccount): Future[Account]
    def changeBalance(item: ChangeBalance, isPositive: Boolean): Future[Either[String, Account]]
    def transfer(createTransaction: CreateTransaction, category_repository: CategoryRepository): Future[Either[String, Seq[Account]]]
    def delete(id: UUID): Future[Unit]
    def get_category(id: UUID): Future[Category]
    def external_transfer(item: ExternalTransferRequest): Future[Either[String, ExternalTransferResponse]]
}
