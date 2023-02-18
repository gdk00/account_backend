package misis.account.model

import akka.http.scaladsl.model.StatusCode

import java.util.UUID

case class Account(id: UUID = UUID.randomUUID(), balance: Int = 0)

case class CreateAccount()
case class ChangeBalance(account_id: UUID, value: Int)

case class ExternalTransferRequest(src_account_id: UUID, dst_account_id: UUID, value: Int)
case class ExternalTransferResponse(src_account_id: UUID, dst_account_id: UUID, src_value: Int)
case class ExternalAccountsListResponse(accounts_ids: List[Account], status: StatusCode)
