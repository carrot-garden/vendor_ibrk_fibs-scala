package name.kaeding.fibs
package ib
package impl
package handlers

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

class ReqMarketDataHandlerSpec extends Specification with ScalaCheck {
  def is =
    "ReqMarketDataHandler" ^
      "all data sent through actor makes it into the response" ! allMessagesNoNoiseEx ^
      "accept as much data as is given before the end token" ! partialDataSentEx ^
      "accept only data with the correct tickerId, ignoring other data" ! noisyPartialDataEx ^
      end

  def allMessagesNoNoiseEx = prop { (r: TickerResponse) =>
    {
      val ibActor = IBActor()
      val handler = new ReqMarketDataHandler(Stock(r.resp.symbol), ibActor, r.tickerId)

      ibActor ! RegisterFibsPromise(handler).left
      val p = handler.promise
      (r.messages :+ TickSnapshotEnd(r.tickerId)).foreach(ibActor ! _.right)

      p.get.copy(received = r.resp.received) must_== r.resp
    }
  }

  def trimMessages(r: TickerResponse) = {
    val msgs = r.messages.take(r.numMessages)
    def msgsContains(msgType: TickField) = msgs.any(_ match {
      case TickPrice(_, t, _, _) if (t === msgType) => true
      case TickSize(_, t, _) if (t === msgType) => true
      case TickString(_, t, _) if (t === msgType) => true
      case TickGeneric(_, t, _) if (t === msgType) => true
      case _ => false
    })
    val expected = MarketDataResult(r.resp.symbol,
      msgsContains(TickBid) ? r.resp.bidPrice | None,
      msgsContains(TickBidSize) ? r.resp.bidSize | None,
      msgsContains(TickAsk) ? r.resp.askPrice | None,
      msgsContains(TickAskSize) ? r.resp.askSize | None,
      msgsContains(TickLast) ? r.resp.lastPrice | None,
      msgsContains(TickLastSize) ? r.resp.lastSize | None,
      msgsContains(TickHigh) ? r.resp.high | None,
      msgsContains(TickLow) ? r.resp.low | None,
      msgsContains(TickOpen) ? r.resp.open | None,
      msgsContains(TickClose) ? r.resp.close | None,
      msgsContains(TickVolume) ? r.resp.volume | None,
      msgsContains(TickLastTimestamp) ? r.resp.timestamp | None,
      msgsContains(TickHalted) ? r.resp.halted | None,
      r.resp.received)
    r.copy(messages = msgs, resp = expected)
  }

  def partialDataSentEx = prop { (r: TickerResponse) =>
    val ibActor = IBActor()
    val handler = new ReqMarketDataHandler(Stock(r.resp.symbol), ibActor, r.tickerId)

    ibActor ! RegisterFibsPromise(handler).left
    val p = handler.promise
    val trimmed = trimMessages(r)
    (trimmed.messages :+ TickSnapshotEnd(r.tickerId)).foreach(ibActor ! _.right)

    p.get.copy(received = r.resp.received) must_== trimmed.resp

  }

  def noisyPartialDataEx = prop { (r: TickerResponse, noise: List[TickerResponse]) =>
    val filteredNoise = noise.filterNot(_.tickerId === r.tickerId)
    val ibActor = IBActor()
    val handler = new ReqMarketDataHandler(Stock(r.resp.symbol), ibActor, r.tickerId)

    ibActor ! RegisterFibsPromise(handler).left
    val noiseHandler = new FibsPromise[Unit] {
      val targetMessages = new MutableList[IBMessage]
      val noiseIds = filteredNoise.map(_.tickerId)
      val actor = Actor[IBMessage] {
        case m @ TickPrice(id, _, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickSize(id, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickString(id, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickGeneric(id, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickSnapshotEnd(id) if (noiseIds contains id) => logMessage(id, m)
        case _ => ???
      }
      def get = ()
      val latch = new CountDownLatch(0)
      def logMessage(id: Int, m: IBMessage) =
        if (id === r.tickerId) {
          targetMessages += m
        }
      val patterns = List(({
        case m @ TickPrice(id, _, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickSize(id, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickString(id, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickGeneric(id, _, _) if (noiseIds contains id) => logMessage(id, m)
        case m @ TickSnapshotEnd(id) if (noiseIds contains id) => logMessage(id, m)
      }): PartialFunction[IBMessage, Unit])
    }
    ibActor ! RegisterFibsPromise(noiseHandler).left
    val p = handler.promise
    val trimmed = trimMessages(r)
    val noisyMsgs = shuffle((trimmed.messages ::
      filteredNoise.map(n => TickSnapshotEnd(n.tickerId)) ::
      filteredNoise.map(_.messages).take(10)).join) :+ TickSnapshotEnd(r.tickerId)
    noisyMsgs.foreach(ibActor ! _.right)

    (noiseHandler.targetMessages must be empty) and 
    (p.get.copy(received = r.resp.received) must_== trimmed.resp)

  }

}

case class TickerResponse(
  tickerId: Int,
  resp: MarketDataResult,
  messages: List[IBMessage],
  numMessages: Int)
    