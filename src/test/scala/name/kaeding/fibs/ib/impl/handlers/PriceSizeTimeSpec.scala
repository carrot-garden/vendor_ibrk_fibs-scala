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
class PriceSizeTimeSpec extends Specification with ScalaCheck { def is =
  "PriceSizeTime must" ^
  	"be not defined initially" ! exInitialUndefined^
  	"accept a price to be set but indicate it isn't ready" ! exInitialPriceSet^
  	"still be undefined after only a price has been set" ! exInitialPriceSetUndefined^
  	"accept a size to be set but indicate it isn't ready" ! exInitialSizeSet^
  	"still be undefined after only a size has been set" ! exInitialSizeSetUndefined^
  	"accept a time to be set but indicate it isn't ready" ! exInitialTimeSet^
  	"still be undefined after only a time has been set" ! exInitialTimeSetUndefined^
  	"accept a time to be set but indicate it isn't ready after only size is set" ! exInitialTimeSizeSet^
  	"still be undefined after only a time and price has been set" ! exInitialTimePriceSetUndefined^
  	"be ready after time, price, and size are set" ! exInitialAllSetDefined^
  	"be defined after time, price, and size are set" ! exInitialAllSetDefined^
  	"have the right values after time, price, and size are set" ! exInitialAllSetValues^
  	"return old time, size & price afer the size (only) is re-set" ! exResetSize^
  	"return old time, size & price afer the price (only) is re-set" ! exResetPrice^
  	"return old time, size & price afer the time (only) is re-set" ! exResetTime^
  end

  def exInitialUndefined = PriceSize.empty.isDefined must_== false
  
  def exInitialPriceSet = prop { (p: Double) =>
    val pst = PriceSizeTime.empty
    pst.setPrice(p) must_== false
  }

  def exInitialPriceSetUndefined = prop { (p: Double) =>
    val pst = PriceSizeTime.empty
    pst.setPrice(p)
    pst.isDefined must_== false
  }
  
  def exInitialSizeSet = prop { (s: Int) =>
    val pst = PriceSizeTime.empty
    pst.setSize(s) must_== false
  }

  def exInitialSizeSetUndefined = prop { (s: Int) =>
    val pst = PriceSizeTime.empty
    pst.setSize(s)
    pst.isDefined must_== false
  }
  
  def exInitialTimeSet = prop { (t: Long) =>
    val pst = PriceSizeTime.empty
    pst.setTime(t) must_== false
  }

  def exInitialTimeSetUndefined = prop { (t: Long) =>
    val pst = PriceSizeTime.empty
    pst.setTime(t)
    pst.isDefined must_== false
  }
  
  def exInitialTimeSizeSet = prop { (t: Long, s: Int) =>
    val pst = PriceSizeTime.empty
    pst.setSize(s)
    pst.setTime(t) must_== false
  }

  def exInitialTimePriceSetUndefined = prop { (t: Long, p: Double) =>
    val pst = PriceSizeTime.empty
    pst.setPrice(p)
    pst.setTime(t)
    pst.isDefined must_== false
  }
  
  def exInitialAllSetReady = prop { (p: Double, s: Int, t: Long) =>
    val pst = PriceSizeTime.empty
    pst.setSize(s)
    pst.setTime(t)
    pst.setPrice(p) must_== true
  }
  
  def exInitialAllSetDefined = prop { (p: Double, s: Int, t: Long) =>
    val pst = PriceSizeTime.empty
    pst.setTime(t)
    pst.setSize(s)
    pst.setPrice(p)
    pst.isDefined must_== true
  }
  
  def exInitialAllSetValues = prop { (p: Double, s: Int, t: Long) =>
    val pst = PriceSizeTime.empty
    pst.setSize(s)
    pst.setPrice(p)
    pst.setTime(t)
    (pst.price must_== p.some) and (pst.size must_== s.some) and (pst.time must_== t.some)
  }
  
  def exResetSize = prop { (p: Double, s1: Int, s2: Int, t: Long) =>
    val pst = PriceSizeTime.empty
    pst.setSize(s1)
    pst.setPrice(p)
    pst.setTime(t)
    pst.setSize(s2)
    (pst.price must_== p.some) and (pst.size must_== s1.some) and (pst.time must_== t.some)
  }
  
  def exResetPrice = prop { (p1: Double, p2: Double, s: Int, t: Long) =>
    val pst = PriceSizeTime.empty
    pst.setPrice(p1)
    pst.setSize(s)
    pst.setTime(t)
    pst.setPrice(p2)
    (pst.price must_== p1.some) and (pst.size must_== s.some) and (pst.time must_== t.some)
  }
  
  def exResetTime = prop { (p: Double, s: Int, t1: Long, t2: Long) =>
    val pst = PriceSizeTime.empty
    pst.setPrice(p)
    pst.setSize(s)
    pst.setTime(t1)
    pst.setTime(t2)
    (pst.price must_== p.some) and (pst.size must_== s.some) and (pst.time must_== t1.some)
  }
}