package name.kaeding.fibs
package ib
package impl
package handlers

import scalaz._, Scalaz._
import scalaz.concurrent._

import messages._
import contract._

class ReqMarketDataHandler(security: Stock/*Security*/) extends FibsPromise[MarketDataResult] {
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
      val messageActor = Actor[IBMessage]{
        case TickPrice(tickerId, TickBid, p, _) => {
          bidPrice = p.some
          latch.countDown
        }
        case TickSize(tickerId, TickBidSize, v) => {
          bidSize = v.some
          latch.countDown
        }
        case TickPrice(tickerId, TickAsk, p, _) => {
          askPrice = p.some
          latch.countDown
        }
        case TickSize(tickerId, TickAskSize, v) => {
          askSize = v.some
          latch.countDown
        }
        case TickPrice(tickerId, TickLast, p, _) => {
          lastPrice = p.some
          latch.countDown
        }
        case TickSize(tickerId, TickLastSize, v) => {
          lastSize = v.some
          latch.countDown
        }
        case TickString(tickerId, TickLastTimestamp, v) => {
          timestamp = v.parseLong.toOption
          latch.countDown
        }
        case TickPrice(tickerId, TickHigh, p, _) => {
          high = p.some
          latch.countDown
        }
        case TickPrice(tickerId, TickLow, p, _) => {
          low = p.some
          latch.countDown
        }
        case TickPrice(tickerId, TickClose, p, _) => {
          close = p.some
          latch.countDown
        }
        case TickPrice(tickerId, TickOpen, p, _) => {
          open = p.some
          latch.countDown
        }
        case TickSize(tickerId, TickVolume, v) => {
          volume = v.some
          latch.countDown
        }
        case TickGeneric(tickerId, TickHalted, v) => {
          v match {
            case 1 => halted = true.some
            case 0 => halted = false.some
          }
          latch.countDown
        }
        case TickSnapshotEnd(tickerId) => for (_ <- 1L to latch.getCount) latch.countDown
        case _ => ???
      }
      val bidPriceHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, TickBid, p, _) => messageActor ! m
      }
      val bidSizeHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickSize(tickerId, TickBidSize, v) => messageActor ! m
      }
      val askPriceHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, TickAsk, p, _) => messageActor ! m
      }
      val askSizeHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickSize(tickerId, TickAskSize, v) => messageActor ! m
      }
      val lastPriceHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, TickLast, p, _) => messageActor ! m
      }
      val lastSizeHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickSize(tickerId, TickLastSize, v) => messageActor ! m
      }
      val highHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, TickHigh, p, _) => messageActor ! m
      }
      val lowHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, TickLow, p, _) => messageActor ! m
      }
      val openHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, TickOpen, p, _) => messageActor ! m
      }
      val closeHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickPrice(tickerId, TickClose, p, _) => messageActor ! m
      }
      val volumeHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickSize(tickerId, TickVolume, v) => messageActor ! m
      } 
      val timestampHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickString(tickerId, TickLastTimestamp, v) => messageActor ! m
      }
      val haltedHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickGeneric(tickerId, TickHalted, v) => messageActor ! m
      }
      val snapshotEndHandler: PartialFunction[IBMessage, Unit] = {
        case m@TickSnapshotEnd(tickerId) => messageActor ! m
      }
      val inputs = List(
          bidPriceHandler, 
          bidSizeHandler, 
          askPriceHandler, 
          askSizeHandler,
          lastPriceHandler,
          lastSizeHandler,
          highHandler,
          lowHandler,
          openHandler,
          closeHandler,
          volumeHandler,
          timestampHandler,
          haltedHandler,
          snapshotEndHandler)
      def get = (for {
        bp <- bidPrice
        bs <- bidSize
        ap <- askPrice
        as <- askSize
      } yield MarketDataResult(security.symbol, bp, bs, ap, as, lastPrice, lastSize, 
          high, low, open, close, volume, timestamp, halted)).get
    }