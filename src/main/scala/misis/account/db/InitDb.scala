package misis.account.db

import misis.account.db.AccountDb.accountTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}


class InitDb(implicit val ec: ExecutionContext, db: Database) {
    def prepare(): Future[_] = {
        db.run(accountTable.schema.createIfNotExists)
    }
}
