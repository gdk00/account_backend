package misis.account.model

import java.util.UUID

case class Category(id: UUID = UUID.randomUUID(), name: String, cashback: Int)

case class CreateCategory(name: String, cashback: Int)

case class UpdateCashback(category_id: UUID, value: Int)
