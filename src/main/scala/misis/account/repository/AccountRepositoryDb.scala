package misis.account.repository

import misis.account.db.AccountDb._
import misis.account.model.{
    Account,
    Category,
    ChangeBalance,
    CreateAccount,
    CreateTransaction,
    ExternalTransferRequest,
    ExternalTransferResponse
}
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AccountRepositoryDb(client: ExternalTransferClient)(implicit val ec: ExecutionContext, db: Database)
    extends AccountRepository {
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
            false -> ((x: Int, y: Int) => x - y)
        )

        for {
            oldBalanceOpt <- db.run(query.result.headOption)
            balanceDifference = item.value

            updateBalance = oldBalanceOpt
                .map { oldBalance =>
                    val newBalance = ops(isPositive)(oldBalance, balanceDifference)
                    if (balanceDifference < 0)
                        Left("Нельзя пополнять / снимать отрицательное число")
                    else if (newBalance < 0)
                        Left("Недостаточно денег на счете")
                    else Right(newBalance)
                }
                .getOrElse(Left("Не найден счет"))

            future = updateBalance.map(balance =>
                db.run {
                    query.update(balance)
                }
            ) match {
                case Right(future) => future.map(Right(_))
                case Left(s) => Future.successful(Left(s))
            }
            updated <- future
            res <- find(item.account_id)
        } yield updated.map(_ => res.get)
    }

    def transfer(
        createAccount: CreateTransaction,
        category_repository: CategoryRepository
    ): Future[Either[String, Seq[Account]]] = {
        changeBalance(ChangeBalance(createAccount.accountId1, createAccount.value), isPositive = false).flatMap {
            case Right(acc1) =>
                changeBalance(ChangeBalance(createAccount.accountId2, createAccount.value), isPositive = true).flatMap {
                    case Right(acc2) =>
                        category_repository.find(createAccount.categoryId).flatMap { category =>
                            {
                                if (category.get != null)
                                    changeBalance(
                                        ChangeBalance(
                                            createAccount.accountId1,
                                            category.get.cashback * createAccount.value / 100
                                        ),
                                        isPositive = true
                                    )
                                else
                                    Future.successful(Left("Cannot find category"))
                            }
                            Future.successful(Right(Seq(acc1, acc2)))
                        }
                    case Left(s) =>
                        changeBalance(ChangeBalance(createAccount.accountId1, createAccount.value), isPositive = true)
                        Future.successful(Left(s))
                }
            case Left(s) => Future.successful(Left(s))
        }
    }

    override def get_category(id: UUID): Future[Category] = {
        db.run(categoryTable.filter(i => i.id === id).result.head)
    }

    override def delete(id: UUID): Future[Unit] = {
        db.run(accountTable.filter(_.id === id).delete).map(_ => ())
    }

    override def external_transfer(item: ExternalTransferRequest): Future[Either[String, ExternalTransferResponse]] = {
        val external_accounts = client.getAccounts
        external_accounts.flatMap {
            case Left(s) => Future.successful(Left(s))
            case Right(accounts) =>
                if (accounts.exists(_.id == item.src_account_id)) {
                    client
                        .external_increase(ChangeBalance(item.dst_account_id, item.value))
                        .flatMap(status =>
                            if (status.isSuccess()) {
                                changeBalance(ChangeBalance(item.src_account_id, item.value), isPositive = false)
                                    .flatMap {
                                        case Right(acc) =>
                                            Future.successful(
                                                Right(
                                                    ExternalTransferResponse(
                                                        item.src_account_id,
                                                        item.dst_account_id,
                                                        acc.balance
                                                    )
                                                )
                                            )
                                        case Left(s) => Future.successful(Left(s))
                                    }
                            } else
                                Future.successful(Left("Cannot check account existence"))
                        )

                } else
                    Future.successful(Left("No such account"))
        }
    }
}
