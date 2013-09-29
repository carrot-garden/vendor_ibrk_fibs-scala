package name.kaeding.fibs
package order

import scalaz._, Scalaz._
import contract._

trait Order[A] {
  def security: A
}

sealed case class OrderState(
    status: String,
    initMargin: String,
    mainMargin: String,
    equityWithLoan: String,
    commission: Double,
    minCommssion: Double,
    maxCommission: Double,
    commissionCurrency: String,
    warningText: String)

sealed trait OrderAction
object OrderAction {
  implicit def OrderActionShows = new Show[OrderAction] {
    override def shows(s: OrderAction) = s match {
      case Buy => "BUY"
      case Sell => "SELL"
      case Short => "SSHORT"
    }
  }
}
case object Buy extends OrderAction
case object Sell extends OrderAction
case object Short extends OrderAction

sealed trait OrderType

sealed case class LimitOrder[A: Contract](
    action: OrderAction,
    security: A,
    limit: Double,
    qty: Int) extends Order[A]

sealed case class TrailStopLimitOrder[A: Contract](
    action: OrderAction,
    security: A,
    limitOffset: Double,
    stop: Double,
    trail: Double,
    qty: Int) extends Order[A]

sealed case class TrailStopOrder[A: Contract](
    action: OrderAction,
    security: A,
    trail: Double,
    qty: Int) extends Order[A]

sealed case class MarketOnCloseOrder[A: Contract](
    action: OrderAction,
    security: A,
    qty: Int)