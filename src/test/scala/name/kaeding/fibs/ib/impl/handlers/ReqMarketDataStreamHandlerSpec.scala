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
import scalaz.concurrent._
import scala.util.Random.shuffle
import scala.collection.mutable.MutableList
import messages._
import contract._
import TickField._

import java.util.concurrent.CountDownLatch

@RunWith(classOf[JUnitRunner])
class ReqMarketDataStreamHandlerSpec extends Specification with ScalaCheck {
  def is =
    "ReqMarketDataStreamHandler" ^
      "ask data makes it through" ! askOnlyEx ^
      "bid data makes it through" ! bidOnlyEx ^
      "last data makes it through" ! lastOnlyEx ^
      "high makes it through" ! highOnlyEx ^
      "low makes it through" ! lowOnlyEx ^
      "close makes it through" ! closeOnlyEx ^
      "volume makes it through" ! volumeOnlyEx ^
      "halted makes it through" ! haltedOnlyEx ^
      "singleton data makes it through while waiting for PriceSize pair" ! highBetweenBidEx ^
      "new Ask PriceSize pair comes through" ! newAskEx ^
      //      "all data sent through actor makes it into the response" ! allMessagesNoNoiseEx ^
      //      "accept as much data as is given before the end token" ! partialDataSentEx ^
      //      "accept only data with the correct tickerId, ignoring other data" ! noisyPartialDataEx ^
      end

  def mkSocket = new EClientSocketLike {
    val calledWith: MutableList[Int] = MutableList()
    def cancelMktData(tickerId: Int) = calledWith += tickerId
  }

  def askOnlyEx = prop { (p: Double, s: Int, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickAsk, p, 0).right
    ibActor ! TickSize(tickerId, TickAskSize, s).right
    rp.get.as.head() must_== MarketDataResult(sym, None, None, p.some, s.some, None, None, None, None, None, None, None, None, None)
  }

  def bidOnlyEx = prop { (p: Double, s: Int, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickBid, p, 0).right
    ibActor ! TickSize(tickerId, TickBidSize, s).right
    val as = rp.get.as
    as.head() must_== MarketDataResult(sym, p.some, s.some, None, None, None, None, None, None, None, None, None, None, None)
  }

  def lastOnlyEx = prop { (p: Double, s: Int, t: Long, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickLast, p, 0).right
    ibActor ! TickSize(tickerId, TickLastSize, s).right
    ibActor ! TickString(tickerId, TickLastTimestamp, t.toString).right
    val as = rp.get.as
    as.head() must_== MarketDataResult(sym, None, None, None, None, p.some, s.some, None, None, None, None, None, t.some, None)
  }

  def highOnlyEx = prop { (h: Double, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickHigh, h, 0).right
    rp.get.as.head() must_== MarketDataResult(sym, None, None, None, None, None, None, h.some, None, None, None, None, None, None)
  }

  def lowOnlyEx = prop { (l: Double, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickLow, l, 0).right
    rp.get.as.head() must_== MarketDataResult(sym, None, None, None, None, None, None, None, l.some, None, None, None, None, None)
  }

  def closeOnlyEx = prop { (c: Double, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickClose, c, 0).right
    rp.get.as.head() must_== MarketDataResult(sym, None, None, None, None, None, None, None, None, None, c.some, None, None, None)
  }

  def volumeOnlyEx = prop { (v: Int, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickSize(tickerId, TickVolume, v).right
    rp.get.as.head() must_== MarketDataResult(sym, None, None, None, None, None, None, None, None, None, None, v.some, None, None)
  }

  def haltedOnlyEx = prop { (h: Boolean, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    val hInt = if (h) 1 else 0
    ibActor ! TickGeneric(tickerId, TickHalted, hInt).right
    rp.get.as.head() must_== MarketDataResult(sym, None, None, None, None, None, None, None, None, None, None, None, None, h.some)
  }

  def highBetweenBidEx = prop { (p: Double, s: Int, h: Double, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickBid, p, 0).right
    ibActor ! TickPrice(tickerId, TickHigh, h, 0).right
    ibActor ! TickSize(tickerId, TickBidSize, s).right
    val as = rp.get.as 
    List(as.head(), as.tail().head()) must_== 
      List(MarketDataResult(sym, None, None, None, None, None, None, h.some, None, None, None, None, None, None),
          MarketDataResult(sym, p.some, s.some, None, None, None, None, h.some, None, None, None, None, None, None))
    
  }

  def newAskEx = prop { (p1: Double, s1: Int, p2: Double, s2: Int, h: Double, sym: String, tickerId: Int) ⇒
    val ibActor = IBActor()
    val socket = mkSocket
    val handler = new ReqMarketDataStreamHandler(Stock(sym), ibActor, tickerId, socket)

    ibActor ! RegisterFibsPromise(handler).left
    val rp = Promise(handler.get)
    ibActor ! TickPrice(tickerId, TickAsk, p1, 0).right
    ibActor ! TickPrice(tickerId, TickHigh, h, 0).right
    ibActor ! TickSize(tickerId, TickAskSize, s1).right
    ibActor ! TickPrice(tickerId, TickAsk, p2, 0).right
    ibActor ! TickSize(tickerId, TickAskSize, s2).right
    val as = rp.get.as 
    List(as.head(), as.tail().head(), as.tail().tail().head()) must_== 
      List(MarketDataResult(sym, None, None, None, None, None, None, h.some, None, None, None, None, None, None),
          MarketDataResult(sym, None, None, p1.some, s1.some, None, None, h.some, None, None, None, None, None, None),
          MarketDataResult(sym, None, None, p2.some, s2.some, None, None, h.some, None, None, None, None, None, None))
    
  }


}