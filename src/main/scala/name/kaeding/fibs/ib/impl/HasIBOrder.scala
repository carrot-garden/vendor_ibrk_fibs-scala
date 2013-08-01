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
    
  }
}