package misis.account.repository

import misis.account.db.AccountDb._
import misis.account.model.{CreateAccount, ChangeBalance, Account}
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AccountRepositoryDb(implicit val ec: ExecutionContext, db: Database) extends AccountRepository {
    override def list(): Future[Seq[Account]] = {
        db.run(accountTable.result)
    }

    override def get(id: UUID): Future[Account] = {
        db.run(accountTable.filter(i => i.id === id).result.head)
    }

    def find(id: UUID): Future[Option[Account]] = {
        db.run(accountTable.filter(i => i.id === id).result.headOption)
    }

    override def create(createItem: CreateAccount): Future[Account] = {
        val item = Account()
        for {
            _ <- db.run(accountTable += item)
            res <- get(item.id)
        } yield res
    }

    override def changeBalance(item: ChangeBalance, isPositive: Boolean): Future[Either[String, Account]] = {
        val query = accountTable
            .filter(_.id === item.account_id)
            .map(_.balance)

        val ops = Map(
            true -> ((x: Int, y: Int) => x + y),
            false -> ((x: Int, y: Int) => x - y),
        )

        for {
            oldBalanceOpt <- db.run(query.result.headOption)
            balanceDifference = item.value

            updateBalance = oldBalanceOpt.map { oldBalance =>
              val newBalance = ops(isPositive)(oldBalance, balanceDifference)
                if (balanceDifference < 0)
                    Left("Нельзя пополнять / снимать отрицательное число")
                else if (newBalance < 0)
                    Left("Недостаточно денег на счете")
                else Right(newBalance)
            }.getOrElse(Left("Не найден счет"))

            future = updateBalance.map(balance => db.run {
                query.update(balance)
            }) match {
                case Right(future) => future.map(Right(_))
                case Left(s) => Future.successful(Left(s))
            }
            updated <- future
            res <- find(item.account_id)
        } yield updated.map(_ => res.get)
    }

    override def delete(id: UUID): Future[Unit] = {
        db.run(accountTable.filter(_.id === id).delete).map(_ => ())
    }
}
