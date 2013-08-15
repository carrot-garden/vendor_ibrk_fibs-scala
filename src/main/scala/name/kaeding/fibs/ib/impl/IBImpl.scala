package name.kaeding.fibs
package ib
package impl

import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CountDownLatch
import scala.collection.mutable.MutableList
import scalaz.{Order => _, _}, Scalaz._
import scalaz.concurrent._
import com.ib.client.Contract
import com.ib.client.ExecutionFilter
import com.ib.client.{ Order => IBOrder }
import com.ib.client.ScannerSubscription
import com.ib.client.EClientSocket
import com.github.nscala_time.time.Imports.{order => _, _}
import grizzled.slf4j.Logging

import name.kaeding.fibs.IB
import messages._
import contract._
import handlers._
import order._
import Contract._

object IBActor extends Logging {
  private[this] class IBActorState {
    var handlers: List[FibsPromise[_]] = Nil
    val unhandledMessages: MutableList[IBMessage] = MutableList[IBMessage]()
  }

  type IBMessageHandler = PartialFunction[IBMessage, Unit]
  def apply() = {
    val state = new IBActorState
    def defaultHandler(msg: IBMessage) {
      debug("Got unhandled message: %s" format msg)
      state.unhandledMessages += msg
    }
    Actor[FibsPromiseMessage \/ IBMessage](_.toEither match {
      case Left(RegisterFibsPromise(p)) => state.handlers = p :: state.handlers
      case Left(UnregisterFibsPromise(p)) => state.handlers = state.handlers.filterNot(_ == p)
      case Right(m) =>
        state.handlers.find(_.patterns.any(_.isDefinedAt(m))).cata(some = _ ! m, none = defaultHandler(m))
    })
  }
}
abstract class FibsPromise[A] {
  val patterns: List[PartialFunction[IBMessage, Unit]]
  val actor: Actor[IBMessage]
  val latch: CountDownLatch
  def get: A
  def !(m: IBMessage) = actor ! m
  def promise = Promise {
    latch.await()
    get
  }
}
sealed trait FibsPromiseMessage
case class RegisterFibsPromise(p: FibsPromise[_]) extends FibsPromiseMessage
case class UnregisterFibsPromise(p: FibsPromise[_]) extends FibsPromiseMessage

class IBImpl(host: String, port: Int, clientId: Option[Int] = None) extends IB {
  val clientIdValue = clientId.getOrElse(IDGenerator.next)
  val ibActor = IBActor()
  val ewrapper = new EWrapperImpl(ibActor)
  val clientSocket = new EClientSocket(ewrapper)
  private[this] var orderIdGenerator: Option[AtomicInteger] = None
  def nextOrderId: Int = orderIdGenerator.map(_.getAndIncrement).getOrElse(-1)
  implicit val s = Strategy.DefaultExecutorService

  def connect(): Option[Promise[ConnectionResult]] = {
    if (!isConnected()) {
      val handler = new FibsPromise[ConnectionResult] {
        var accounts: Option[ManagedAccounts] = none
        var nextId: Option[NextValidId] = none
        val latch = new CountDownLatch(2)
        val accHandler: PartialFunction[IBMessage, Unit] = {
          case ma: ManagedAccounts => {
            accounts = ma.some
            latch.countDown
            if (latch.getCount() === 0) ibActor ! UnregisterFibsPromise(this).left
          }
        }
        val nextIdHandler: PartialFunction[IBMessage, Unit] = {
          case v: NextValidId => {
            nextId = v.some
            orderIdGenerator = new AtomicInteger(v.nextId).some
            latch.countDown
            if (latch.getCount() === 0) ibActor ! UnregisterFibsPromise(this).left
          }
        }
        val patterns = List(accHandler, nextIdHandler)
        val actor = Actor[IBMessage](accHandler.orElse(nextIdHandler))
        def get = ((accounts.map(_.accounts) |@|
          nextId.map(_.nextId))(ConnectionResult.apply)).get
      }

      ibActor ! RegisterFibsPromise(handler).left
      clientSocket.eConnect(host, port, clientIdValue)
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

  def reqMktDataSnapshot(
    security: Stock, // Security
    genericTickList: String): Promise[MarketDataResult] = {
    val tickerId = IDGenerator.next
    val handler = new ReqMarketDataHandler(security, ibActor, tickerId)

    ibActor ! RegisterFibsPromise(handler).left
    clientSocket.reqMktData(
      tickerId,
      security.contract(0), //IDGenerator.next), 
      genericTickList,
      true)
    handler.promise
  }
  
  def reqMktDataStream(
    security: Stock, // Security
    genericTickList: String): CloseableStream[MarketDataResult] = {
    val tickerId = IDGenerator.next
    val handler = new ReqMarketDataStreamHandler(security, ibActor, tickerId, EClientSocketLike(clientSocket))

    ibActor ! RegisterFibsPromise(handler).left
    clientSocket.reqMktData(
      tickerId,
      security.contract(0), //IDGenerator.next), 
      genericTickList,
      false)
    handler.get
  }
  
  def reqTickDataStream(
    security: Stock // Security
    ): CloseableStream[MarketTickDataResult] = {
    val tickerId = IDGenerator.next
    val handler = new ReqMarketTickDataStreamHandler(security, ibActor, tickerId, EClientSocketLike(clientSocket))

    ibActor ! RegisterFibsPromise(handler).left
    clientSocket.reqMktData(
      tickerId,
      security.contract(0), //IDGenerator.next), 
      "233",
      false)
    handler.get
  }

  def cancelHistoricalData(tickerId: Int): Unit = {}

  def cancelRealTimeBars(tickerId: Int): Unit = {}

  // one request per 10 seconds
  private[this] val historicalDataGovernor = new Governor(10500)
  def reqHistoricalData(
    security: Stock, // Security 
    endDateTime: DateTime,
    duration: Period,
    barSize: BarSize,
    whatToShow: ShowMe,
    useRTH: Boolean): Promise[Stream[HistoricalDataPeriod]] = {
    val tickerId = IDGenerator.next
    val handler = new ReqHistoricalDataHandler(security, ibActor, tickerId)
    ibActor ! RegisterFibsPromise(handler).left

    Promise {
      historicalDataGovernor.requestClearance
      val fmt = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss")
      clientSocket.reqHistoricalData(
        tickerId,
        security.contract(0),
        fmt.print(endDateTime),
        duration.shows,
        barSize.shows,
        whatToShow.shows,
        useRTH ? 1 | 0,
        2)
    } >> handler.promise

  }

  def reqRealTimeBars(tickerId: Int, contract: Contract, barSize: Int, whatToShow: String, useRTH: Boolean): Unit = {}

  def reqContractDetails(reqId: Int, contract: Contract): Unit = {}

  def reqMktDepth(tickerId: Int, contract: Contract, numRows: Int): Unit = {}

  def cancelMktData(tickerId: Int): Unit = {}

  def cancelMktDepth(tickerId: Int): Unit = {}

  def exerciseOptions(tickerId: Int, contract: Contract, exerciseAction: Int, exerciseQuantity: Int, account: String, overrideNatural: Int): Unit = {}

  def placeOrder[S, O[S] <: Order[S]](order: O[S])(implicit hasOrder: HasIBOrder[S, O], sconv: S => Stock): Unit = {
    import HasIBOrder._
    val orderId = nextOrderId
    val ibOrder: IBOrder = hasOrder.ibOrder(order, orderId)
    val security: Stock = order.security
    val contract: Contract = security.contract(0)
    val tickerId = IDGenerator.next
    clientSocket.placeOrder(orderId, contract, ibOrder)
    // TODO: handle openOder response message
  }

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
      val latch = new CountDownLatch(1)
      val timeHandler: PartialFunction[IBMessage, Unit] = {
        case t: CurrentTime => {
          time = t.time.some
          latch.countDown
          if (latch.getCount() === 0) ibActor ! UnregisterFibsPromise(this).left
        }
      }
      val patterns = List(timeHandler)
      val actor = Actor[IBMessage](timeHandler)
      def get = time.get
    }

    ibActor ! RegisterFibsPromise(handler).left
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
