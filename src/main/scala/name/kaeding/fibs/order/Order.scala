package name.kaeding.fibs
package order

import scalaz._, Scalaz._
import contract._

trait Order[A] {
//  def id: Int
  def security: A
}

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
//    id: Int,
    action: OrderAction,
    security: A,
    limit: Double,
    qty: Int) extends Order[A]

sealed case class TrailStopLimitOrder[A: Contract](
//    id: Int,
    action: OrderAction,
    security: A,
    limit: Double,
    stop: Double,
    trail: Double,
    qty: Int) extends Order[A]