package com.alexitc.coinalerts.data.anorm

import anorm.SqlParser._
import anorm._
import com.alexitc.coinalerts.models._
import org.postgresql.util.PGobject

object AnormParsers {

  val parseUserId = str("user_id").map(UserId.apply)
  val parseEmail = str("email")(citextToString).map(UserEmail.apply)
  val parseUserVerificationToken = str("token").map(UserVerificationToken.apply)

  val parseAlertId = long("alert_id").map(AlertId.apply)
  val parseAlertType = str("alert_type").map(AlertType.fromDatabaseString)
  val parseMarket = str("market").map(Market.fromDatabaseString)
  val parseBook = str("book").map(Book.fromString(_).get) // Assumes db value is always properly formatted.
  val parseisGreaterThan = bool("is_greater_than")
  val parsePrice = get[BigDecimal]("price")
  val parseBasePrice = get[BigDecimal]("base_price")

  val parseUser = (parseUserId ~ parseEmail).map {
    case userId ~ email => User.apply(userId, email)
  }

  val parseAlert = (parseAlertId ~ parseAlertType ~ parseUserId ~ parseMarket ~ parseBook ~ parseisGreaterThan ~ parsePrice).map {
    case alertId ~ alertType ~ userId ~ market ~ book ~ isGreaterThan ~ price =>
      Alert(alertId, alertType, userId, market, book, isGreaterThan, price)
  }

  val parseBasePriceAlert = (parseAlertId ~ parseBasePrice).map {
    case alertId ~ basePrice => BasePriceAlert(alertId, basePrice)
  }

  val parsePassword = str("password").map(UserHiddenPassword.fromDatabase)

  def citextToString: Column[String] = Column.nonNull { case (value, meta) =>
    val MetaDataItem(qualified, _, clazz) = meta
    value match {
      case str: String => Right(str)
      case obj: PGobject if "citext" equalsIgnoreCase obj.getType => Right(obj.getValue)
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to String for column $qualified, class = $clazz"))
    }
  }
}