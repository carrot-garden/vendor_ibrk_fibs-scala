package name.kaeding.fibs
package contract

import scalaz._, Scalaz._
import com.ib.client.{ Contract => IBContract }

sealed trait Security
case class Stock(
  symbol: String,
  securityId: Option[SecurityId] = none,
  currency: String = "USD") extends Security
  
object Stock {
  implicit object stockContract extends Contract[Stock] {
    def contract(s: Stock) = contractId => 
      new IBContract(
          contractId,
          s.symbol,
          "STK",
          "", // expiry
          0.0, // strike
          "", // right
          "", // multiplier
          "SMART",
          s.currency,
          "", // localSymbol
          new java.util.Vector(), // comboLegs
          "ISLAND", // primaryExchange
          false, // includeExpired
          s.securityId.map(s => s.code).getOrElse(""),
          s.securityId.map(_.shows).getOrElse(""))
  }
}

case class StockOption(securityId: SecurityId) extends Security
case class Future(securityId: SecurityId) extends Security
case class Index(securityId: SecurityId) extends Security
case class FOP(securityId: SecurityId) extends Security
case class Cash(securityId: SecurityId) extends Security
case class BAG(securityId: SecurityId) extends Security