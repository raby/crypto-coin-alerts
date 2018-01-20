package com.alexitc.coinalerts.data.anorm

import com.alexitc.coinalerts.commons.{DataHelper, PostgresDataHandlerSpec}
import com.alexitc.coinalerts.data.anorm.dao.NewCurrencyAlertPostgresDAO
import com.alexitc.coinalerts.errors.{NewCurrencyAlertNotFoundError, RepeatedExchangeError, VerifiedUserNotFound}
import com.alexitc.coinalerts.models.{Exchange, NewCurrencyAlertId, UserId}
import org.scalactic.{Bad, Good}

class NewCurrencyAlertPostgresDataHandlerSpec extends PostgresDataHandlerSpec {

  lazy val dataHandler = new NewCurrencyAlertPostgresDataHandler(database, new NewCurrencyAlertPostgresDAO)

  "create" should {
    "be able to create a valid alert" in {
      val user = DataHelper.createVerifiedUser()
      val exchange = Exchange.BITTREX

      val result = dataHandler.create(user.id, exchange)
      result.isGood mustEqual true
      result.get.exchange mustEqual exchange
    }

    "reject a repeated exchange" in {
      val user = DataHelper.createVerifiedUser()
      val exchange = Exchange.BITTREX

      dataHandler.create(user.id, exchange)
      val result = dataHandler.create(user.id, exchange)
      result mustEqual Bad(RepeatedExchangeError).accumulating
    }

    "reject an unknown user" in {
      val exchange = Exchange.BITTREX

      val result = dataHandler.create(UserId.create, exchange)
      result mustEqual Bad(VerifiedUserNotFound).accumulating
    }
  }

  "get" should {
    "retrieve user alerts" in {
      val user = DataHelper.createVerifiedUser()
      val alert1 = dataHandler.create(user.id, Exchange.BITTREX).get
      val alert2 = dataHandler.create(user.id, Exchange.BITSO).get

      val result = dataHandler.get(user.id).get
      result.size mustEqual 2
      result.contains(alert1) mustEqual true
      result.contains(alert2) mustEqual true
    }
  }

  "delete" should {
    "delete an alert" in {
      val user = DataHelper.createVerifiedUser()
      val exchange = Exchange.BITTREX
      val alert = dataHandler.create(user.id, exchange).get

      val result = dataHandler.delete(alert.id, user.id)
      result mustEqual Good(alert)
    }

    "fail to delete a non existent alert" in {
      val alerts = dataHandler.getAll().get
      val nonExistentAlertId = NewCurrencyAlertId(alerts.map(_.id.int).max + 1)
      val user = DataHelper.createVerifiedUser()

      val result = dataHandler.delete(nonExistentAlertId, user.id)
      result mustEqual Bad(NewCurrencyAlertNotFoundError).accumulating
    }
  }
}
