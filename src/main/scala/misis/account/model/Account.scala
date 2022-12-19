package misis.account.model

import java.util.UUID

case class Account(id: UUID = UUID.randomUUID(), balance: Int = 0)

case class CreateAccount()
case class ChangeBalance(account_id: UUID, value: Int)