package name.kaeding.fibs.ib.impl

import org.specs2._
import org.scalacheck._
import Gen._
import Arbitrary._
import scalaz._, Scalaz._
import scala.util.Random.shuffle
import com.github.nscala_time.time.Imports._
import scala.math.BigDecimal

import name.kaeding.fibs.ib.messages._

package object handlers {
  def mkSocket = new EClientSocketLike {
    import scala.collection.mutable.MutableList
    val cancelMktDataCalledWith: MutableList[Int] = MutableList()
    val cancelRealTimeBarsCalledWith: MutableList[Int] = MutableList()
    def cancelMktData(tickerId: Int) = cancelMktDataCalledWith += tickerId
    def cancelRealTimeBars(tickerId: Int) = cancelRealTimeBarsCalledWith += tickerId
  }
  
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
  
  implicit def genTickData = for {
    p <- posNum[Double]
    s <- posNum[Int]
    v <- posNum[Int]
    t <- posNum[Long]
    w <- posNum[Double]
    f <- arbitrary[Boolean]
  } yield MarketTickDataResult(
      BigDecimal(p).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble, 
      s, 
      v, 
      t, 
      BigDecimal(w).setScale(8, BigDecimal.RoundingMode.HALF_UP).toDouble, 
      f)

  def genListPeriod = Gen.containerOf[List, HistoricalDataPeriod](genPeriod)
  
  def genListTickData = Gen.containerOf[List, MarketTickDataResult](genTickData)

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
  
  implicit def genTickDataPackage = Arbitrary {
    for {
      tickerId <- arbitrary[Int]
      symbol <- arbitrary[String]
      ticks <- genListTickData
    } yield TickDataPackage(
        tickerId, 
        symbol, 
        ticks, 
        ticks.map(tickToMessage(tickerId)))
  }
  
  implicit def genRealTimeBarsPackage = Arbitrary {
    def historicalToLiveBar(h: HistoricalDataPeriod) = RealTimeBar(
        h.time, h.open, h.high, h.low, h.close, h.volume, h.count, h.wap)
    def historicalToLiveBarMessage(tickerId: Int)(h: HistoricalDataPeriod) =
      RealTimeBarResp(tickerId, h.time.getMillis, h.open, h.high, h.low, h.close, h.volume, h.count, h.wap)
    for {
      tickerId <- arbitrary[Int]
      symbol <- arbitrary[String]
      periods <- genListPeriod
    } yield RealTimeBarsPackage(
        tickerId,
        symbol,
        periods.map(historicalToLiveBar),
        periods.map(historicalToLiveBarMessage(tickerId)))
  }

  def tickToMessage(tickerId: Int)(t: MarketTickDataResult) =
    TickString(tickerId, RTVolume, f"${t.lastPrice}%2.2f;${t.lastSize}%d;${t.timestamp}%d;${t.volume}%d;${t.wap}%8.8f;${t.singleTrade}")
    
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
    open.some, close.some, volume.some, timestamp.some, halted.some, DateTime.now)

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