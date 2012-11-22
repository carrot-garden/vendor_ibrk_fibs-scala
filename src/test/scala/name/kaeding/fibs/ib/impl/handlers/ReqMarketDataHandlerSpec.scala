package name.kaeding.fibs.ib
package impl.handlers

import org.specs2._
import org.scalacheck._
import Arbitrary._

import scalaz._, Scalaz._
import scala.util.Random.shuffle

import messages._

class ReqMarketDataHandlerSpec extends Specification with ScalaCheck { def is =
	"ReqMarketDataHandler" ^
		"accept as much data as is given before the end token" ! stockQuoteResponseEx.pendingUntilFixed^
	end
	
	implicit def genTickerResponse = Arbitrary { for {
	  tickerId <- arbitrary[Int]
	  resp <- genResponse(tickerId)
	  messages <- genMessages(tickerId, resp)
	} yield TickerResponse(tickerId, resp, messages) }
	
	def genResponse(tickerId: Int) = for {
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
	} yield MarketDataResult("", bidPrice, bidSize, askPrice, askSize, lastPrice.some, 
	    lastSize.some, high.some, low.some, open.some, close.some, volume.some,
	    timestamp.some, halted.some)
	
	def genMessages(tickerId: Int, resp: MarketDataResult) = for {
	  askAutoExecute <- Gen.oneOf(0, 1)
	  bidAutoExecute <- Gen.oneOf(0, 1)
	  lastAutoExecute <- Gen.oneOf(0, 1)
	  highAutoExecute <- Gen.oneOf(0, 1)
	  lowAutoExecute <- Gen.oneOf(0, 1)
	  openAutoExecute <- Gen.oneOf(0, 1)
	  closeAutoExecute <- Gen.oneOf(0, 1)
	  numMessages <- Gen.choose(0, 13)
	} yield shuffle(List(
	    TickPrice(tickerId, TickAsk, resp.askPrice, askAutoExecute),
	    TickSize(tickerId, TickAskSize, resp.askSize),
	    TickPrice(tickerId, TickBid, resp.bidPrice, bidAutoExecute),
	    TickSize(tickerId, TickBidSize, resp.bidSize),
	    TickPrice(tickerId, TickLast, resp.lastPrice.get, lastAutoExecute),
	    TickSize(tickerId, TickLastSize, resp.lastSize.get),
	    TickPrice(tickerId, TickHigh, resp.high.get, highAutoExecute),
	    TickPrice(tickerId, TickLow, resp.low.get, lowAutoExecute),
	    TickPrice(tickerId, TickHigh, resp.open.get, openAutoExecute),
	    TickPrice(tickerId, TickLow, resp.close.get, closeAutoExecute),
	    TickSize(tickerId, TickVolume, resp.volume.get),
	    TickString(tickerId, TickLastTimestamp, resp.timestamp.shows),
	    TickGeneric(tickerId, TickHalted, resp.halted ? 1 | 0))).take(numMessages) :+
	    TickSnapshotEnd(tickerId)
	    
	def stockQuoteResponseEx = prop {(r: TickerResponse) => {
	  1 must_== 2
	}}
	
}

case class TickerResponse(tickerId: Int, resp: MarketDataResult, messages: List[IBMessage])