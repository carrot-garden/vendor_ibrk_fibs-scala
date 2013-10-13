package name.kaeding.fibs
package ib
package impl

import scalaz._, Scalaz._
import order.{ Order ⇒ FibsOrder, _ }
import contract._
import messages._

import com.ib.client.{ Order ⇒ IBOrder }

trait HasIBOrder[C, A[C]] {
  def ibOrder(a: A[C], orderId: Int): IBOrder
  def fromIB(o: IBOrder, c: C): (A[C], Int)
}

object HasIBOrder extends HasIBOrderInstances

trait HasIBOrderInstances {
  import OrderType._
  implicit val FibsOrderIBOrder = new HasIBOrder[Stock, FibsOrder] {
    def ibOrder(a: FibsOrder[Stock], orderId: Int) = a match {
      case o: LimitOrder[Stock]          ⇒ LimitOrderIBOrder.ibOrder(o, orderId)
      case o: TrailStopLimitOrder[Stock] ⇒ TrailStopLimitOrderIBOrder.ibOrder(o, orderId)
      case o: TrailStopOrder[Stock]      ⇒ TrailStopOrderIBOrder.ibOrder(o, orderId)
      case o: MarketOnCloseOrder[Stock]  ⇒ MarketOnCloseIBOrder.ibOrder(o, orderId)
    }
    def fromIB(o: IBOrder, s: Stock) =
      ???
  }
  implicit val LimitOrderIBOrder: HasIBOrder[Stock, LimitOrder] = new HasIBOrder[Stock, LimitOrder] {
    def ibOrder(a: LimitOrder[Stock], orderId: Int) = {
      val ret = new IBOrder
      val orderType: messages.OrderType = Limit
      ret.m_orderId = orderId
      ret.m_action = a.action.shows
      ret.m_auxPrice = 0
      ret.m_lmtPrice = a.limit
      ret.m_orderType = orderType.shows
      ret.m_totalQuantity = a.qty
      ret.m_goodAfterTime = ""
      ret.m_goodTillDate = ""
      ret.m_transmit = true
      ret
    }
    def fromIB(o: IBOrder, s: Stock) =
      //LimitOrder[Stock]
      ???
  }
  implicit val TrailStopLimitOrderIBOrder: HasIBOrder[Stock, TrailStopLimitOrder] = new HasIBOrder[Stock, TrailStopLimitOrder] {
    def ibOrder(a: TrailStopLimitOrder[Stock], orderId: Int) = {
      val ret = new IBOrder
      val orderType: messages.OrderType = TrailStopLimit
      ret.m_orderId = orderId
      ret.m_action = a.action.shows
      ret.m_auxPrice = a.trail
      ret.m_trailStopPrice = a.stop
      ret.m_lmtPrice = a.action match {
        case Buy ⇒ a.stop + a.limitOffset
        case _   ⇒ a.stop - a.limitOffset
      }
      ret.m_orderType = orderType.shows
      ret.m_totalQuantity = a.qty
      ret.m_goodAfterTime = ""
      ret.m_goodTillDate = ""
      ret.m_transmit = true
      ret
    }
    def fromIB(o: IBOrder, s: Stock) =
      //TrailStopLimitOrder[Stock]
      ???
  }
  implicit val TrailStopOrderIBOrder: HasIBOrder[Stock, TrailStopOrder] = new HasIBOrder[Stock, TrailStopOrder] {
    def ibOrder(a: TrailStopOrder[Stock], orderId: Int) = {
      val ret = new IBOrder
      val orderType: messages.OrderType = TrailStop
      ret.m_orderId = orderId
      ret.m_action = a.action.shows
      ret.m_auxPrice = a.trail
      ret.m_orderType = orderType.shows
      ret.m_totalQuantity = a.qty
      ret.m_transmit = true
      ret
    }
    def fromIB(o: IBOrder, s: Stock) =
      //TrailStopLimitOrder[Stock]
      ???
  }
  implicit val MarketOnCloseIBOrder: HasIBOrder[Stock, MarketOnCloseOrder] = new HasIBOrder[Stock, MarketOnCloseOrder] {
    def ibOrder(a: MarketOnCloseOrder[Stock], orderId: Int) = {
      val ret = new IBOrder
      val orderType: messages.OrderType = MarketOnClose
      ret.m_orderId = orderId
      ret.m_action = a.action.shows
      ret.m_orderType = orderType.shows
      ret.m_totalQuantity = a.qty
      ret.m_transmit = true
      ret
    }
    def fromIB(o: IBOrder, s: Stock) =
      //MarketOnCloseOrder[Stock]
      ???
  }

}
trait OCAGroup {
  def ibOrders(ocaGroupName: String, ocaType: OCAType): List[Int ⇒ (Stock, IBOrder)]
  def orders: List[_ <: FibsOrder[_]]

  def ::[S: Contract, A[S] <: FibsOrder[S]](o: A[S])(implicit IBOrder: HasIBOrder[S, A], sconv: S => Stock): OCAGroup = new OCAGroup {
    override def ibOrders(ocaGroupName: String, ocaType: OCAType) = ((id: Int) ⇒ {
        val ibo = IBOrder.ibOrder(o, id)
        ibo.m_ocaGroup = ocaGroupName
        ibo.m_ocaType = OCAType.code(ocaType)
        val security: Stock = o.security
        (security, ibo)
      }) :: self.ibOrders(ocaGroupName, ocaType)
    override def orders = o :: self.orders
  }

  private[this] val self = this
}
object OCA extends OCAGroup {
  override def ibOrders(ocaGroupName: String, ocaType: OCAType) = Nil
  override def orders = Nil
}