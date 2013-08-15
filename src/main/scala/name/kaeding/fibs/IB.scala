package name.kaeding.fibs

import java.net.Socket
import com.github.nscala_time.time.Imports.{ order ⇒ _, _ }
import com.ib.client.Contract
import com.ib.client.ExecutionFilter
import com.ib.client.{ Order ⇒ IBOrder }
import com.ib.client.ScannerSubscription
import scalaz.{ Order ⇒ _, _ }, Scalaz._
import scalaz.concurrent._
import contract._
import order._
import ib.messages._
import name.kaeding.fibs.ib.impl.HasIBOrder

trait IB {
  def serverVersion(): Int

  def TwsConnectionTime(): String

  def isConnected(): Boolean

  def connect(): Option[Promise[ConnectionResult]]
  def disconnect(): Unit

  def cancelScannerSubscription(tickerId: Int)

  def reqScannerParameters()

  def reqScannerSubscription(tickerId: Int,
                             subscription: ScannerSubscription)

  def reqMktDataSnapshot(
    security: Stock, // Security,
    genericTickList: String): Promise[MarketDataResult]

  def reqMktDataStream(
    security: Stock, // Security,
    genericTickList: String): CloseableStream[MarketDataResult]

  def reqTickDataStream(
    security: Stock // Security,
    ): CloseableStream[MarketTickDataResult]

  def cancelHistoricalData(tickerId: Int)

  def cancelRealTimeBars(tickerId: Int)

  def reqHistoricalData(
    contract: Stock, // Security,
    endDateTime: DateTime,
    durationStr: Period,
    barSize: BarSize,
    whatToShow: ShowMe,
    useRTH: Boolean): Promise[Stream[HistoricalDataPeriod]]

  def reqRealTimeBars(tickerId: Int, contract: Contract,
                      barSize: Int, whatToShow: String, useRTH: Boolean)

  def reqContractDetails(reqId: Int, contract: Contract)

  def reqMktDepth(tickerId: Int, contract: Contract,
                  numRows: Int)

  def cancelMktData(tickerId: Int)

  def cancelMktDepth(tickerId: Int)

  def exerciseOptions(tickerId: Int, contract: Contract,
                      exerciseAction: Int, exerciseQuantity: Int, account: String,
                      overrideNatural: Int)

  def placeOrder[S, O[S] <: Order[S]](order: O[S])(implicit hasOrder: HasIBOrder[S, O], sconv: S ⇒ Stock): Unit

  def reqAccountUpdates(subscribe: Boolean, acctCode: String)

  def reqExecutions(reqId: Int, filter: ExecutionFilter)

  def cancelOrder(id: Int)

  def reqOpenOrders()

  def reqIds(numIds: Int)

  def reqNewsBulletins(allMsgs: Boolean)

  def cancelNewsBulletins()

  def setServerLogLevel(logLevel: Int)

  def reqAutoOpenOrders(bAutoBind: Boolean)

  def reqAllOpenOrders()

  def reqManagedAccts()

  def requestFA(faDataType: Int)

  def replaceFA(faDataType: Int, xml: String)

  def currentTime(): Promise[Long]

  def reqFundamentalData(reqId: Int, contract: Contract,
                         reportType: String)

  def cancelFundamentalData(reqId: Int)

  def calculateImpliedVolatility(reqId: Int,
                                 contract: Contract, optionPrice: Double, underPrice: Double)

  def cancelCalculateImpliedVolatility(reqId: Int)

  def calculateOptionPrice(reqId: Int, contract: Contract,
                           volatility: Double, underPrice: Double)

  def cancelCalculateOptionPrice(reqId: Int)

  def reqGlobalCancel()

  def reqMarketDataType(marketDataType: Int)
}