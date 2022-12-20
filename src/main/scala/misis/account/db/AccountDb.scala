package misis.account.db

import misis.account.model.{Account, Category}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import java.util.UUID

object AccountDb {
    class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
        val id = column[UUID]("id", O.PrimaryKey)
        val balance = column[Int]("balance")

        def * = (id, balance) <> ((Account.apply _).tupled, Account.unapply)
    }
    class TransactionCategoryTable(tag: Tag) extends Table[Category](tag, "categories") {
        val id = column[UUID]("id", O.PrimaryKey)
        val name = column[String]("name")
        val cashback = column[Int]("cashback")

        def * = (id, name, cashback) <> ((Category.apply _).tupled, Category.unapply)
    }

    val accountTable = TableQuery[AccountTable]
    val categoryTable = TableQuery[TransactionCategoryTable]
}
