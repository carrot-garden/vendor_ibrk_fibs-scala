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
class LimitOrderSpec extends Specification with ScalaCheck { def is =
  "LimitOrder converted to IBOrder must" ^
  	"retain the action" ! exAction^
  	"retain the limit price" ! exLimitPrice^
  	"retain the quantity" ! exQuantity^
  	"have the correct order type" ! exOrderType^
  end

  def exAction = prop { (o: LimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, LimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.action must_== o.action.shows
  }

  def exLimitPrice = prop { (o: LimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, LimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.lmtPrice must_== o.limit
  }
  
  def exQuantity = prop { (o: LimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, LimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.totalQuantity must_== o.qty
  }
  
  def exOrderType = prop { (o: LimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, LimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.orderType must_== "LMT"
  }
}