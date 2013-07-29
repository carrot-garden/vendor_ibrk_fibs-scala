package name.kaeding.fibs
package ib
package impl
package handlers

import org.junit.runner.RunWith
import org.specs2.runner.{ JUnitRunner }
import org.specs2._
import org.scalacheck._
import Arbitrary._

import scalaz._, Scalaz._

@RunWith(classOf[JUnitRunner])
class PriceSizeSpec extends Specification with ScalaCheck { def is =
  "PriceSize must" ^
  	"be not defined initially" ! exInitialUndefined^
  	"accept a price to be set but indicate it isn't ready" ! exInitialPriceSet^
  	"still be undefined after only a price has been set" ! exInitialPriceSetUndefined^
  	"accept a size to be set but indicate it isn't ready" ! exInitialSizeSet^
  	"still be undefined after only a size has been set" ! exInitialSizeSetUndefined^
  	"be ready after both price and size are set" ! exInitialBothSetDefined^
  	"be defined after both price and size are set" ! exInitialBothSetDefined^
  	"have the right values after both price and size are set" ! exInitialBothSetValues^
  	"return old size & price afer the size (only) is re-set" ! exResetSize^
  	"return old size & price afer the price (only) is re-set" ! exResetPrice^
  end

  def exInitialUndefined = PriceSize.empty.isDefined must_== false
  
  def exInitialPriceSet = prop { (p: Double) =>
    val ps = PriceSize.empty
    ps.setPrice(p) must_== false
  }

  def exInitialPriceSetUndefined = prop { (p: Double) =>
    val ps = PriceSize.empty
    ps.setPrice(p)
    ps.isDefined must_== false
  }
  
  def exInitialSizeSet = prop { (s: Int) =>
    val ps = PriceSize.empty
    ps.setSize(s) must_== false
  }

  def exInitialSizeSetUndefined = prop { (s: Int) =>
    val ps = PriceSize.empty
    ps.setSize(s)
    ps.isDefined must_== false
  }
  
  def exInitialBothSetReady = prop { (p: Double, s: Int) =>
    val ps = PriceSize.empty
    ps.setSize(s)
    ps.setPrice(p) must_== true
  }
  
  def exInitialBothSetDefined = prop { (p: Double, s: Int) =>
    val ps = PriceSize.empty
    ps.setSize(s)
    ps.setPrice(p)
    ps.isDefined must_== true
  }
  
  def exInitialBothSetValues = prop { (p: Double, s: Int) =>
    val ps = PriceSize.empty
    ps.setSize(s)
    ps.setPrice(p)
    (ps.price must_== p.some) and (ps.size must_== s.some)
  }
  
  def exResetSize = prop { (p: Double, s1: Int, s2: Int) =>
    val ps = PriceSize.empty
    ps.setSize(s1)
    ps.setPrice(p)
    ps.setSize(s2)
    (ps.price must_== p.some) and (ps.size must_== s1.some)
  }
  
  def exResetPrice = prop { (p1: Double, p2: Double, s: Int) =>
    val ps = PriceSize.empty
    ps.setPrice(p1)
    ps.setSize(s)
    ps.setPrice(p2)
    (ps.price must_== p1.some) and (ps.size must_== s.some)
  }
}