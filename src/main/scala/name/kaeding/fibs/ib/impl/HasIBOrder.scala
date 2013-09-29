package name.kaeding.fibs
package ib
package impl

import scalaz._, Scalaz._
import order._
import contract._
import messages._

import com.ib.client.{ Order => IBOrder }


trait HasIBOrder[C, A[C]] {
  def ibOrder(a: A[C], orderId: Int): IBOrder
  def fromIB(o: IBOrder, c: C): (A[C], Int)
}

object HasIBOrder extends HasIBOrderInstances

trait HasIBOrderInstances {
  import OrderType._
  implicit val LimitOrderIBOrder = new HasIBOrder[Stock, LimitOrder] {
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
  implicit val TrailStopLimitOrderIBOrder = new HasIBOrder[Stock, TrailStopLimitOrder] {
    def ibOrder(a: TrailStopLimitOrder[Stock], orderId: Int) = {
      val ret = new IBOrder
      val orderType: messages.OrderType = TraitStopLimit
      ret.m_orderId = orderId
      ret.m_action = a.action.shows
      ret.m_auxPrice = a.trail
      ret.m_trailStopPrice = a.stop
      ret.m_lmtPrice = a.action match {
        case Buy => a.stop + a.limitOffset
        case _   => a.stop - a.limitOffset
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
  implicit val MarketOnCloseIBOrder = new HasIBOrder[Stock, MarketOnCloseOrder] {
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