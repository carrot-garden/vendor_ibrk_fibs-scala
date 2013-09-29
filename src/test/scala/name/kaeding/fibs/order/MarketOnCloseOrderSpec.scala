package name.kaeding.fibs
package order

import org.junit.runner.RunWith
import org.specs2.runner.{ JUnitRunner }
import org.specs2._
import org.scalacheck._
import Arbitrary._

import scalaz._, Scalaz._
import contract._
import ib.impl.HasIBOrder
import ib.impl.HasIBOrder._
import com.ib.client.{ Order => IBOrder }
import TestData._

@RunWith(classOf[JUnitRunner])
class MarketOnCloseOrderSpec extends Specification with ScalaCheck { def is =
  "MarketOnCloseOrder converted to IBOrder must" ^
  	"retain the action" ! exAction^
  	"retain the quantity" ! exQuantity^
  	"have the correct order type" ! exOrderType^
  end

  def exAction = prop { (o: MarketOnCloseOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, MarketOnCloseOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.m_action must_== o.action.shows
  }
  
  def exQuantity = prop { (o: MarketOnCloseOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, MarketOnCloseOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.m_totalQuantity must_== o.qty
  }
  
  def exOrderType = prop { (o: MarketOnCloseOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, MarketOnCloseOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.m_orderType must_== "MOC"
  }
}