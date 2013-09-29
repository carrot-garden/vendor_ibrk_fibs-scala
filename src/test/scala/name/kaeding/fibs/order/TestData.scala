package name.kaeding.fibs
package order

import org.specs2._
import org.scalacheck._
import Gen._
import Arbitrary._
import scalaz._, Scalaz._
import contract.Stock

object TestData {

  def genLimitStockOrder = for {
    sym <- arbitrary[String]
    limit <- posNum[Double]
    action <- Gen.oneOf(Buy, Sell, Short)
    qty <- posNum[Int]
  } yield LimitOrder[Stock](action, Stock(sym), limit, qty)
  
  def genTrailStopLimitOrder = for {
    sym <- arbitrary[String]
    limitOffset <- posNum[Double]
    stop <- posNum[Double]
    trail <- posNum[Double]
    action <- Gen.oneOf(Buy, Sell, Short)
    qty <- posNum[Int]
  } yield TrailStopLimitOrder[Stock](action, Stock(sym), limitOffset, stop, trail, qty)
  
  def genTrailStopOrder = for {
    sym <- arbitrary[String]
    trail <- posNum[Double]
    action <- Gen.oneOf(Buy, Sell, Short)
    qty <- posNum[Int]
  } yield TrailStopOrder[Stock](action, Stock(sym), trail, qty)
  
  def genMarketOnCloseOrder = for {
    sym <- arbitrary[String]
    action <- Gen.oneOf(Buy, Sell, Short)
    qty <- posNum[Int]
  } yield MarketOnCloseOrder[Stock](action, Stock(sym), qty)
  
  implicit def arbLimitStockOrder = Arbitrary(genLimitStockOrder)
  implicit def arbTrailStopLimitOrder = Arbitrary(genTrailStopLimitOrder)
  implicit def arbTrailStopOrder = Arbitrary(genTrailStopOrder)
  implicit def arbMarketOnCloseOrder = Arbitrary(genMarketOnCloseOrder)
}