package misis.account.repository

import misis.account.db.AccountDb._
import misis.account.model._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class CategoryRepositoryDb(implicit val ec: ExecutionContext, db: Database)
    extends CategoryRepository {
    override def list(): Future[Seq[Category]] = {
        db.run(categoryTable.result)
    }

    override def get(id: UUID): Future[Category] = {
        db.run(categoryTable.filter(i => i.id === id).result.head)
    }

    def find(id: UUID): Future[Option[Category]] = {
        db.run(categoryTable.filter(i => i.id === id).result.headOption)
    }

    override def create(createCategory: CreateCategory): Future[Category] = {
        val item = Category(name = createCategory.name, cashback = createCategory.cashback)
        for {
            _ <- db.run(categoryTable += item)
            res <- get(item.id)
        } yield res
    }

    override def delete(id: UUID): Future[Unit] = {
        db.run(categoryTable.filter(_.id === id).delete).map(_ => ())
    }
    override def updateCashback(updateCashback: UpdateCashback): Future[Either[String, Category]] = {
        val query = categoryTable
            .filter(_.id === updateCashback.category_id)
            .map(_.cashback)

        for {
            oldPriceOpt <- db.run(query.result.headOption)
            newPrice = updateCashback.value

            updatePrice = oldPriceOpt
                .map { oldPrice =>
                    if (updateCashback.value > 100)
                        Left("Больше 100")
                    else if (updateCashback.value < 0)
                        Left("Меньше 100")
                    else Right(newPrice)
                }
                .getOrElse(Left("Не найден элемент"))

            future = updatePrice.map(price =>
                db.run {
                    query.update(price)
                }
            ) match {
                case Right(future) => future.map(Right(_))
                case Left(s) => Future.successful(Left(s))
            }
            updated <- future
            res <- find(updateCashback.category_id)
        } yield updated.map(_ => res.get)
    }
}
