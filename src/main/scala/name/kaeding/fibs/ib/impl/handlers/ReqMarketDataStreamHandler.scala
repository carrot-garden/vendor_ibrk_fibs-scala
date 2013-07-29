package name.kaeding.fibs
package ib
package impl
package handlers

import java.util.concurrent.CountDownLatch
import scalaz._, Scalaz._
import scalaz.concurrent._
import messages._
import contract._

import java.util.concurrent.{ LinkedBlockingQueue, BlockingQueue }
import com.ib.client.EClientSocket

class ReqMarketDataStreamHandler(security: Stock /*Security*/ ,
                                 ibActor: Actor[FibsPromiseMessage \/ IBMessage],
                                 tickerId: Int, socket: EClientSocketLike) extends FibsPromise[CloseableStream[MarketDataResult]] {
  private[this] val TickerId = tickerId
  val latch = new CountDownLatch(0) // don't need to block
  private[this] val bid: PriceSize = PriceSize.empty
  private[this] val ask: PriceSize = PriceSize.empty
  private[this] val last: PriceSizeTime = PriceSizeTime.empty
  var halted: Option[Boolean] = none
  var volume: Option[Int] = none
  var high: Option[Double] = none
  var low: Option[Double] = none
  var close: Option[Double] = none
  var open: Option[Double] = none
  val actor = Actor[IBMessage] {
    case TickPrice(TickerId, TickBid, p, _) ⇒
      if (bid.setPrice(p)) enqueue
    case TickSize(TickerId, TickBidSize, v) ⇒
      if (bid.setSize(v)) enqueue
    case TickPrice(TickerId, TickAsk, p, _) ⇒
      if (ask.setPrice(p)) enqueue
    case TickSize(TickerId, TickAskSize, v) ⇒
      if (ask.setSize(v)) enqueue
    case TickPrice(TickerId, TickLast, p, _) ⇒
      if (last.setPrice(p)) enqueue
    case TickSize(TickerId, TickLastSize, v) ⇒
      if (last.setSize(v)) enqueue
    case TickString(TickerId, TickLastTimestamp, v) ⇒ 
      v.parseLong.toOption.foreach(t => if (last.setTime(t)) enqueue)
    case TickPrice(TickerId, TickHigh, p, _)        ⇒ 
      high = p.some
      enqueue
    case TickPrice(TickerId, TickLow, p, _)         ⇒ 
      low = p.some
      enqueue
    case TickPrice(TickerId, TickClose, p, _)       ⇒ 
      close = p.some
      enqueue
    case TickPrice(TickerId, TickOpen, p, _)        ⇒ 
      open = p.some
      enqueue
    case TickSize(TickerId, TickVolume, v)          ⇒ 
      volume = v.some
      enqueue
    case TickGeneric(TickerId, TickHalted, v) ⇒ {
      v match {
        case 1 ⇒ halted = true.some
        case 0 ⇒ halted = false.some
      }
      enqueue
    }
    case _ ⇒ ???
  }
  val priceHandler: PartialFunction[IBMessage, Unit] = {
    case m@TickPrice(tickerId, _, _, _) ⇒ actor ! m
  }
  val sizeHandler: PartialFunction[IBMessage, Unit] = {
    case m@TickSize(tickerId, _, _) ⇒ actor ! m
  }
  val stringHandler: PartialFunction[IBMessage, Unit] = {
    case m@TickString(tickerId, _, _) ⇒ actor ! m
  }
  val genericHandler: PartialFunction[IBMessage, Unit] = {
    case m@TickGeneric(tickerId, _, _) ⇒ actor ! m
  }
  val patterns = List(
    priceHandler,
    sizeHandler,
    stringHandler,
    genericHandler)
  private[this] val queue: BlockingQueue[Option[MarketDataResult]] =
    new LinkedBlockingQueue[Option[MarketDataResult]]()
  private[this] def closeStream = {
    queue add None
    socket.cancelMktData(TickerId)
    ibActor ! UnregisterFibsPromise(this).left
  }
  private[this] def enqueue: Unit =
    queue add MarketDataResult(security.symbol, bid.price, bid.size, ask.price,
      ask.size, last.price, last.size, high, low, open, close, volume, last.time,
      halted).some
  private[this] def toStream: EphemeralStream[MarketDataResult] = {
    queue.take match {
      case Some(d) ⇒ EphemeralStream.cons(d, toStream)
      case None    ⇒ EphemeralStream.emptyEphemeralStream
    }
  }
  case object CloseStreamMessage
  private[this] val closeActor = Actor[Any] {
    case _ ⇒ closeStream
  }
  def get = new CloseableStream[MarketDataResult] {
    def close = closeActor ! CloseStreamMessage
    lazy val as = toStream
  }
}
