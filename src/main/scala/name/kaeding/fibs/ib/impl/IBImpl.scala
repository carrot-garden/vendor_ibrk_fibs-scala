package name.kaeding.fibs
package ib
package impl

import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.MutableList
import scalaz._, Scalaz._
import scalaz.concurrent._
import com.ib.client.Contract
import com.ib.client.ExecutionFilter
import com.ib.client.Order
import com.ib.client.ScannerSubscription
import com.ib.client.EClientSocket
import name.kaeding.fibs.IB
import messages._
import contract._
import Contract._
import java.util.concurrent.CountDownLatch
import name.kaeding.fibs.ib.messages.TickLastTimestamp
import name.kaeding.fibs.ib.messages.TickHalted

object IBActor {
  private[this] class IBActorState {
    var handlers: List[IBMessageHandler] = Nil
    val unhandledMessages: MutableList[IBMessage] = MutableList[IBMessage]()
  }

  type IBMessageHandler = PartialFunction[IBMessage, Unit]
  def apply() = {
    val state = new IBActorState
    def defaultHandler(msg: IBMessage) {
      println("Got unhandled message: %s" format msg)
      state.unhandledMessages.+=(msg)
    }
    Actor[FibsPromise[_] \/ IBMessage](_.toEither match {
      case Left(h) => state.handlers = h.inputs ::: state.handlers
      case Right(m) => {
        val (thisOne, rest) = state.handlers.partition(_.isDefinedAt(m))
        thisOne.headOption.map(h => {
          h(m)
          val others = thisOne.tail
          if (others.isEmpty)
            state.handlers = rest
          else
            state.handlers = others ++ rest
        }).getOrElse(defaultHandler(m))
      }
    })
  }
}
abstract class FibsPromise[A] {
  val inputs: List[PartialFunction[IBMessage, Unit]]
  lazy val latch = new CountDownLatch(inputs.length)
  def get: A
  def promise = Promise {
    latch.await()
    get
  }
}

class IBImpl(host: String, port: Int) extends IB {
  val clientId = IDGenerator.next
  val actor = IBActor()
  val ewrapper = new EWrapperImpl(actor)
  val clientSocket = new EClientSocket(ewrapper)
  implicit val s = Strategy.DefaultExecutorService

  def connect(): Option[Promise[ConnectionResult]] = {
    if (!isConnected()) {
      val handler = new FibsPromise[ConnectionResult] {
        var accounts: Option[ManagedAccounts] = none
        var nextId: Option[NextValidId] = none
        val accHandler: PartialFunction[IBMessage, Unit] = {
          case ma: ManagedAccounts => {
            accounts = ma.some
            latch.countDown
          }
        }
        val nextIdHandler: PartialFunction[IBMessage, Unit] = {
          case v: NextValidId => {
            nextId = v.some
            latch.countDown
          }
        }
        val inputs = List(accHandler, nextIdHandler)
        def get = ((accounts.map(_.accounts) |@|
          nextId.map(_.nextId))(ConnectionResult.apply)).get
      }

      actor ! handler.left
      clientSocket.eConnect(host, port, clientId)
      handler.promise.some
    } else {
      none
    }
  }

  def disconnect() {
    if (isConnected()) {
      clientSocket.eDisconnect()
      println("disconnected")
    }
  }

  def serverVersion(): Int = clientSocket.serverVersion()

  def TwsConnectionTime(): String = clientSocket.TwsConnectionTime()

  def isConnected(): Boolean = clientSocket.isConnected()

  def eConnect(socket: Socket, clientId: Int): Unit = ???

  def cancelScannerSubscription(tickerId: Int): Unit = {}

  def reqScannerParameters(): Unit = {}

  def reqScannerSubscription(tickerId: Int, subscription: ScannerSubscription): Unit = {}

  def reqMktData(
      security: Stock, // Security
      genericTickList: String, 
      snapshot: Boolean): Promise[MarketDataResult] = {
    val tickerId = IDGenerator.next
    val handler = new FibsPromise[MarketDataResult] {
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
        case TickSnapshotEnd(tickerId) => latch.countDown
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
	  	lp <- lastPrice
	  	ls <- lastSize
	  	h <- high
	  	l <- low
	  	o <- open
	  	c <- close
	  	v <- volume
	  	ts <- timestamp
	  	ht <- halted
      } yield MarketDataResult(security.symbol, bp, bs, ap, as, lp, ls, h, l, o, c, v, ts, ht)).get
    }

    actor ! handler.left
    clientSocket.reqMktData(
        tickerId, 
        security.contract(0),//IDGenerator.next), 
        genericTickList, 
        snapshot)
    handler.promise
  }

  def cancelHistoricalData(tickerId: Int): Unit = {}

  def cancelRealTimeBars(tickerId: Int): Unit = {}

  def reqHistoricalData(tickerId: Int, contract: Contract, endDateTime: String, durationStr: String, barSizeSetting: String, whatToShow: String, useRTH: Int, formatDate: Int): Unit = {}

  def reqRealTimeBars(tickerId: Int, contract: Contract, barSize: Int, whatToShow: String, useRTH: Boolean): Unit = {}

  def reqContractDetails(reqId: Int, contract: Contract): Unit = {}

  def reqMktDepth(tickerId: Int, contract: Contract, numRows: Int): Unit = {}

  def cancelMktData(tickerId: Int): Unit = {}

  def cancelMktDepth(tickerId: Int): Unit = {}

  def exerciseOptions(tickerId: Int, contract: Contract, exerciseAction: Int, exerciseQuantity: Int, account: String, overrideNatural: Int): Unit = {}

  def placeOrder(id: Int, contract: Contract, order: Order): Unit = {}

  def reqAccountUpdates(subscribe: Boolean, acctCode: String): Unit = {}

  def reqExecutions(reqId: Int, filter: ExecutionFilter): Unit = {}

  def cancelOrder(id: Int): Unit = {}

  def reqOpenOrders(): Unit = {}

  def reqIds(numIds: Int): Unit = {}

  def reqNewsBulletins(allMsgs: Boolean): Unit = {}

  def cancelNewsBulletins(): Unit = {}

  def setServerLogLevel(logLevel: Int): Unit = {}

  def reqAutoOpenOrders(bAutoBind: Boolean): Unit = {}

  def reqAllOpenOrders(): Unit = {}

  def reqManagedAccts(): Unit = {}

  def requestFA(faDataType: Int): Unit = {}

  def replaceFA(faDataType: Int, xml: String): Unit = {}

  def currentTime(): Promise[Long] = {
    val handler = new FibsPromise[Long] {
      var time: Option[Long] = none
      val timeHandler: PartialFunction[IBMessage, Unit] = {
        case t: CurrentTime => {
          time = t.time.some
          latch.countDown
        }
      }
      val inputs = List(timeHandler)
      def get = time.get
    }

    actor ! handler.left
    clientSocket.reqCurrentTime()
    handler.promise
  }

  def reqFundamentalData(reqId: Int, contract: Contract, reportType: String): Unit = {}

  def cancelFundamentalData(reqId: Int): Unit = {}

  def calculateImpliedVolatility(reqId: Int, contract: Contract, optionPrice: Double, underPrice: Double): Unit = {}

  def cancelCalculateImpliedVolatility(reqId: Int): Unit = {}

  def calculateOptionPrice(reqId: Int, contract: Contract, volatility: Double, underPrice: Double): Unit = {}

  def cancelCalculateOptionPrice(reqId: Int): Unit = {}

  def reqGlobalCancel(): Unit = {}

  def reqMarketDataType(marketDataType: Int): Unit = {}

}

private[impl] object IDGenerator {
  private[this] val counter = new AtomicInteger()
  def next = counter.getAndIncrement
}
