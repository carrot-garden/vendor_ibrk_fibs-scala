package name.kaeding.fibs
package ib
package impl
package handlers

import java.util.concurrent.CountDownLatch

import scalaz._, Scalaz._
import scalaz.concurrent._

import messages._
import contract._

class ReqMarketDataHandler(security: Stock/*Security*/, 
		ibActor: Actor[FibsPromiseMessage \/ IBMessage],
		tickerId: Int) extends FibsPromise[MarketDataResult] {
	  private[this] val TickerId = tickerId
      var bidPrice: Option[Double] = none 
      var bidSize: Option[Int] = none 
      var askPrice: Option[Double] = none 
      var askSize: Option[Int] = none
      var lastPrice: Option[Double] = none
      var lastSize: Option[Int] = none
      var timestamp: Option[Long] = none
      var halted: Option[Boolean] = none
      var volume: Option[Int] = none
      var high: Option[Double] = none
      var low: Option[Double] = none
      var close: Option[Double] = none
      var open: Option[Double] = none
      val actor = Actor[IBMessage]{
        case TickPrice(TickerId, TickBid, p, _) => bidPrice = p.some
        case TickSize(TickerId, TickBidSize, v) => bidSize = v.some
        case TickPrice(TickerId, TickAsk, p, _) => askPrice = p.some
        case TickSize(TickerId, TickAskSize, v) => askSize = v.some
        case TickPrice(TickerId, TickLast, p, _) => lastPrice = p.some
        case TickSize(TickerId, TickLastSize, v) => lastSize = v.some
        case TickString(TickerId, TickLastTimestamp, v) => timestamp = v.parseLong.toOption
        case TickPrice(TickerId, TickHigh, p, _) => high = p.some
        case TickPrice(TickerId, TickLow, p, _) => close = p.some
        case TickPrice(TickerId, TickClose, p, _) => close = p.some
        case TickPrice(TickerId, TickOpen, p, _) => open = p.some
        case TickSize(TickerId, TickVolume, v) => volume = v.some
        case TickGeneric(TickerId, TickHalted, v) => {
          v match {
            case 1 => halted = true.some
            case 0 => halted = false.some
          }
        }
        case TickSnapshotEnd(tickerId) => {
          latch.countDown
          ibActor ! UnregisterFibsPromise(this).left
        }
        case _ => ???
      }
      val latch = new CountDownLatch(1)
      val priceHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, _, _, _) => actor ! m
      }
      val sizeHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickSize(tickerId, _, _) => actor ! m
      }
      val stringHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickString(tickerId, _, _) => actor ! m
      }
      val genericHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickGeneric(tickerId, _, _) => actor ! m
      }
      val snapshotEndHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickSnapshotEnd(tickerId) => actor ! m
      }
      val patterns = List(
          priceHandler, 
          sizeHandler, 
          stringHandler, 
          genericHandler,
          snapshotEndHandler)
      def get = (for {
        bp <- bidPrice
        bs <- bidSize
        ap <- askPrice
        as <- askSize
      } yield MarketDataResult(security.symbol, bp, bs, ap, as, lastPrice, lastSize, 
          high, low, open, close, volume, timestamp, halted)).get
    }