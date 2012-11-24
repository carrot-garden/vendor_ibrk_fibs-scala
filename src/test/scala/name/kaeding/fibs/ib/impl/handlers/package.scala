package name.kaeding.fibs.ib.impl

import org.specs2._
import org.scalacheck._
import Arbitrary._
import scalaz._, Scalaz._
import scala.util.Random.shuffle
import org.scala_tools.time.Imports._

import name.kaeding.fibs.ib.messages._

package object handlers {
  implicit def genPeriod = for {
    time <- arbitrary[Long]
    open <- arbitrary[Double]
    high <- arbitrary[Double]
    low <- arbitrary[Double]
    close <- arbitrary[Double]
    volume <- arbitrary[Int]
    count <- arbitrary[Int]
    wap <- arbitrary[Double]
    hasGaps <- arbitrary[Boolean]
  } yield HistoricalDataPeriod(
    new DateTime((time / 1000) * 1000), // ensure the ms are zeroed out
    open,
    high,
    low,
    close,
    volume,
    count,
    wap,
    hasGaps)

  def genListPeriod = Gen.containerOf[List, HistoricalDataPeriod](genPeriod)

  implicit def genHistoricalDataPackage = Arbitrary {
    for {
      tickerId <- arbitrary[Int]
      symbol <- arbitrary[String]
      periods <- genListPeriod
      finalMessage <- genFinalHistoricalMessage(tickerId)
    } yield HistoricalDataPackage(
        tickerId, 
        symbol, 
        periods, 
        periods.map(periodToMessage(tickerId)) :+ finalMessage)
  }

  def periodToMessage(tickerId: Int)(p: HistoricalDataPeriod) =
    HistoricalData(
      tickerId,
      (p.time.getMillis / 1000).shows,
      p.open,
      p.high,
      p.low,
      p.close,
      p.volume,
      p.count,
      p.wap,
      p.hasGaps)
      
  def genFinalHistoricalMessage(tickerId: Int) = for {
    date <- arbitrary[String]
    open <- arbitrary[Double]
    high <- arbitrary[Double]
    low <- arbitrary[Double]
    close <- arbitrary[Double]
    volume <- arbitrary[Int]
    count <- arbitrary[Int]
    wap <- arbitrary[Double]
    hasGaps <- arbitrary[Boolean]
  } yield HistoricalData(
      tickerId, 
      "finished-%s".format(date),
      open,
      high,
      low,
      close,
      volume,
      count,
      wap,
      hasGaps)

  implicit def genTickerResponse = Arbitrary {
    for {
      tickerId <- arbitrary[Int]
      resp <- genResponse
      messages <- genMessages(tickerId, resp)
      numMessages <- Gen.choose(0, 13)
    } yield TickerResponse(tickerId, resp, messages, numMessages)
  }

  def genResponse = for {
    askPrice <- arbitrary[Double]
    askSize <- arbitrary[Int]
    bidPrice <- arbitrary[Double]
    bidSize <- arbitrary[Int]
    lastPrice <- arbitrary[Double]
    lastSize <- arbitrary[Int]
    high <- arbitrary[Double]
    low <- arbitrary[Double]
    open <- arbitrary[Double]
    close <- arbitrary[Double]
    volume <- arbitrary[Int]
    timestamp <- arbitrary[Long]
    halted <- arbitrary[Boolean]
  } yield MarketDataResult("", bidPrice.some, bidSize.some, askPrice.some,
    askSize.some, lastPrice.some, lastSize.some, high.some, low.some,
    open.some, close.some, volume.some, timestamp.some, halted.some)

  def genMessages(tickerId: Int, resp: MarketDataResult) = for {
    askAutoExecute <- Gen.oneOf(0, 1)
    bidAutoExecute <- Gen.oneOf(0, 1)
    lastAutoExecute <- Gen.oneOf(0, 1)
    highAutoExecute <- Gen.oneOf(0, 1)
    lowAutoExecute <- Gen.oneOf(0, 1)
    openAutoExecute <- Gen.oneOf(0, 1)
    closeAutoExecute <- Gen.oneOf(0, 1)
  } yield shuffle(List(
    TickPrice(tickerId, TickAsk, resp.askPrice.get, askAutoExecute),
    TickSize(tickerId, TickAskSize, resp.askSize.get),
    TickPrice(tickerId, TickBid, resp.bidPrice.get, bidAutoExecute),
    TickSize(tickerId, TickBidSize, resp.bidSize.get),
    TickPrice(tickerId, TickLast, resp.lastPrice.get, lastAutoExecute),
    TickSize(tickerId, TickLastSize, resp.lastSize.get),
    TickPrice(tickerId, TickHigh, resp.high.get, highAutoExecute),
    TickPrice(tickerId, TickLow, resp.low.get, lowAutoExecute),
    TickPrice(tickerId, TickOpen, resp.open.get, openAutoExecute),
    TickPrice(tickerId, TickClose, resp.close.get, closeAutoExecute),
    TickSize(tickerId, TickVolume, resp.volume.get),
    TickString(tickerId, TickLastTimestamp, resp.timestamp.get.shows),
    TickGeneric(tickerId, TickHalted, resp.halted.get ? 1 | 0)))
}