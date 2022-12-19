package misis.account.db

import misis.account.model.Account
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import java.util.UUID

object AccountDb {
    class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
        val id = column[UUID]("id", O.PrimaryKey)
        val balance = column[Int]("balance")

        def * = (id, balance) <> ((Account.apply _).tupled, Account.unapply)
    }
//    class CategoryTable(tag: Tag) extends Table[Account](tag, "categories") {
//        val id = column[UUID]("id", O.PrimaryKey)
//        val balance = column[String]("category")
//
//        def * = (id, balance) <> ((.apply _).tupled, Account.unapply)
//    }

    val accountTable = TableQuery[AccountTable]
}
