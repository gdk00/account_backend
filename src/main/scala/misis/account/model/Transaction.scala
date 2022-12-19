package misis.account.model

import java.util.UUID

case class Transaction(account1: Account, account2: Account, value: Int)

case class CreateTransaction(accountId1: UUID, accountId2: UUID, value: Int)
