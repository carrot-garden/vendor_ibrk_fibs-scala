package name.kaeding.fibs
package ib
package impl

import com.ib.client.{ Order => IBOrder, CommissionReport => IBCommissionReport, MarketDataType => IBMarketDataType, _ }
import scalaz._, Scalaz._
import scalaz.concurrent._
import com.github.nscala_time.time.Imports._
import messages._

sealed case class EWrapperImpl(ibActor: Actor[FibsPromiseMessage \/ IBMessage]) extends EWrapper {

  /**
   * As seen from class EWrapperImpl, the missing signatures are as follows.
   *  For convenience, these are usable as stub implementations.
   */
  def accountSummary(x$1: Int, x$2: String, x$3: String, x$4: String, x$5: String): Unit = ???
  def accountSummaryEnd(x$1: Int): Unit = ???
  def accountUpdateMulti(x$1: Int, x$2: String, x$3: String, x$4: String, x$5: String, x$6: String): Unit = ???
  def accountUpdateMultiEnd(x$1: Int): Unit = ???
  def connectAck(): Unit = ???
  def deltaNeutralValidation(x$1: Int, x$2: com.ib.client.DeltaNeutralContract): Unit = ???
  def displayGroupList(x$1: Int, x$2: String): Unit = ???
  def displayGroupUpdated(x$1: Int, x$2: String): Unit = ???
  def orderStatus(x$1: Int, x$2: String, x$3: Double, x$4: Double, x$5: Double, x$6: Int, x$7: Int, x$8: Double, x$9: Int, x$10: String): Unit = ???
  def position(x$1: String, x$2: com.ib.client.Contract, x$3: Double, x$4: Double): Unit = ???
  def positionEnd(): Unit = ???
  def positionMulti(x$1: Int, x$2: String, x$3: String, x$4: com.ib.client.Contract, x$5: Double, x$6: Double): Unit = ???
  def positionMultiEnd(x$1: Int): Unit = ???
  def softDollarTiers(x$1: Int, x$2: Array[com.ib.client.SoftDollarTier]): Unit = ???
  def updatePortfolio(x$1: com.ib.client.Contract, x$2: Double, x$3: Double, x$4: Double, x$5: Double, x$6: Double, x$7: Double, x$8: String): Unit = ???
  def verifyAndAuthCompleted(x$1: Boolean, x$2: String): Unit = ???
  def verifyAndAuthMessageAPI(x$1: String, x$2: String): Unit = ???
  def verifyCompleted(x$1: Boolean, x$2: String): Unit = ???
  def verifyMessageAPI(x$1: String): Unit = ???

  def securityDefinitionOptionalParameter(x$1: Int, x$2: String, x$3: Int, x$4: String, x$5: String, x$6: //
  java.util.Set[String], x$7: java.util.Set[java.lang.Double]): Unit = ???

  def securityDefinitionOptionalParameterEnd(x$1: Int): Unit = ???

  def tickPrice(tickerId: Int, field: Int, price: Double, canAutoExecute: Int): Unit =
    ibActor ! TickPrice(tickerId, TickField.read(field), price, canAutoExecute).right

  def tickSize(tickerId: Int, field: Int, size: Int): Unit =
    ibActor ! TickSize(tickerId, TickField.read(field), size).right

  def tickOptionComputation(
    tickerId: Int, field: Int, impliedVol: Double, delta: Double,
    optPrice: Double, pvDividend: Double, gamma: Double, vega: Double,
    theta: Double, undPrice: Double): Unit =
    ibActor ! TickOptionComputation(tickerId, TickField.read(field), impliedVol,
      delta, optPrice, pvDividend, gamma, vega, theta, undPrice).right

  def tickGeneric(tickerId: Int, tickType: Int, value: Double): Unit =
    ibActor ! TickGeneric(tickerId, TickField.read(tickType), value).right

  def tickString(tickerId: Int, tickType: Int, value: String): Unit =
    ibActor ! TickString(tickerId, TickField.read(tickType), value).right

  def tickEFP(tickerId: Int, tickType: Int, basisPoints: Double, formattedBasisPoints: String, impliedFuture: Double, holdDays: Int, futureExpiry: String, dividendImpact: Double, dividendsToExpiry: Double): Unit = ???

  def orderStatus(orderId: Int, status: String, filled: Int, remaining: Int, avgFillPrice: Double, permId: Int, parentId: Int, lastFillPrice: Double, clientId: Int, whyHeld: String): Unit =
    println(s"orderStatus: $orderId, $status, $filled, $remaining, $avgFillPrice, $permId, $parentId, $lastFillPrice, $clientId, $whyHeld")

  def openOrder(orderId: Int, contract: Contract, order: IBOrder, orderState: OrderState): Unit = ???
  //    println(s"openOrder: $orderId, ${contract.shows}, ${order.shows}, ${orderState.shows}")

  def openOrderEnd(): Unit =
    println("openOrderEnd")

  def updateAccountValue(key: String, value: String, currency: String, accountName: String): Unit = ???

  def updatePortfolio(contract: Contract, position: Int, marketPrice: Double, marketValue: Double, averageCost: Double, unrealizedPNL: Double, realizedPNL: Double, accountName: String): Unit = ???

  def updateAccountTime(timeStamp: String): Unit = ???

  def accountDownloadEnd(accountName: String): Unit = ???

  def nextValidId(orderId: Int): Unit = ibActor ! NextValidId(orderId).right

  def contractDetails(reqId: Int, contractDetails: ContractDetails): Unit = ???

  def bondContractDetails(reqId: Int, contractDetails: ContractDetails): Unit = ???

  def contractDetailsEnd(reqId: Int): Unit = ???

  def execDetails(reqId: Int, contract: Contract, execution: Execution): Unit = ???

  def execDetailsEnd(reqId: Int): Unit = ???

  def updateMktDepth(tickerId: Int, position: Int, operation: Int, side: Int, price: Double, size: Int): Unit = ???

  def updateMktDepthL2(tickerId: Int, position: Int, marketMaker: String, operation: Int, side: Int, price: Double, size: Int): Unit = ???

  def updateNewsBulletin(msgId: Int, msgType: Int, message: String, origExchange: String): Unit = ???

  def managedAccounts(accountsList: String): Unit = ibActor ! ManagedAccounts(accountsList).right

  def receiveFA(faDataType: Int, xml: String): Unit = ???

  def historicalData(reqId: Int, date: String, open: Double, high: Double,
    low: Double, close: Double, volume: Int, count: Int, WAP: Double,
    hasGaps: Boolean): Unit =
    ibActor ! HistoricalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps).right

  def scannerParameters(xml: String): Unit = ???

  def scannerData(reqId: Int, rank: Int, contractDetails: ContractDetails, distance: String, benchmark: String, projection: String, legsStr: String): Unit = ???

  def scannerDataEnd(reqId: Int): Unit = ???

  def realtimeBar(reqId: Int, time: Long, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, count: Int): Unit =
    ibActor ! RealTimeBarResp(reqId, time, open, high, low, close, volume, count, wap).right

  def currentTime(time: Long): Unit = ibActor ! CurrentTime(time).right

  def fundamentalData(reqId: Int, data: String): Unit = ???

  //  def deltaNeutralValidation(reqId: Int, underComp: UnderComp): Unit = ???

  def tickSnapshotEnd(reqId: Int): Unit =
    ibActor ! TickSnapshotEnd(reqId).right

  def marketDataType(reqId: Int, marketDataType: Int): Unit =
    ibActor ! MarketDataTypeMsg(reqId, MarketDataType.read(marketDataType)).right

  val yyyymmddFormat = DateTimeFormat.forPattern("yyyyMMdd")
  def commissionReport(r: IBCommissionReport): Unit =
    CommissionReport(
      r.m_execId,
      r.m_commission,
      r.m_currency,
      r.m_realizedPNL,
      r.m_yield,
      yyyymmddFormat.parseDateTime(r.m_yieldRedemptionDate.shows))

  def error(e: Exception): Unit = throw e // ???

  def error(str: String): Unit = ibActor ! UnknownIBError(-1, -1, str).right

  def error(id: Int, errorCode: Int, errorMsg: String): Unit = errorCode match {
    case c if (2100 until 2110).contains(c) => ibActor ! WarningMessage(errorCode, errorMsg).right
    case 162                                => ibActor ! HistoricalDataError(id, errorCode, errorMsg).right
    case _                                  => ibActor ! UnknownIBError(id, errorCode, errorMsg).right
  }

  def connectionClosed(): Unit = ???

}

