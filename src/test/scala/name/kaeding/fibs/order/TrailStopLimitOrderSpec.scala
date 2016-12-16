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
class TrailStopLimitOrderSpec extends Specification with ScalaCheck { def is =
  "TrailStopLimitOrder converted to IBOrder must" ^
  	"retain the action" ! exAction^
  	"retain the limit offset" ! exLimitOffset^
  	"retain the stop price" ! exStopPrice^
  	"retain the trail amount" ! exTrailAmt^
  	"retain the quantity" ! exQuantity^
  	"have the correct order type" ! exOrderType^
  end

  def exAction = prop { (o: TrailStopLimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, TrailStopLimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.action must_== o.action.shows
  }

  def exLimitOffset = prop { (o: TrailStopLimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, TrailStopLimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.lmtPrice must_== (o.action match {
      case Buy => o.stop + o.limitOffset
      case _   => o.stop - o.limitOffset
    })
  }

  def exStopPrice = prop { (o: TrailStopLimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, TrailStopLimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.trailStopPrice must_== o.stop
  }

  def exTrailAmt = prop { (o: TrailStopLimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, TrailStopLimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.auxPrice must_== o.trail
  }
  
  def exQuantity = prop { (o: TrailStopLimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, TrailStopLimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.totalQuantity must_== o.qty
  }
  
  def exOrderType = prop { (o: TrailStopLimitOrder[Stock], orderId: Int) =>
    val hasIb = implicitly[HasIBOrder[Stock, TrailStopLimitOrder]]
    val ibo: IBOrder = hasIb.ibOrder(o, orderId)
    ibo.orderType must_== "TRAIL LIMIT"
  }
}