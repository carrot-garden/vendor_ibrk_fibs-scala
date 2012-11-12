package name.kaeding.fibs
package ib
package impl

import com.ib.client.{Order => IBOrder, _}
import scalaz._, Scalaz._
import scalaz.concurrent._
import messages._
import name.kaeding.fibs.ib.messages.IBMessage

sealed case class EWrapperImpl(ibActor: Actor[FibsPromise[_] \/ IBMessage])  extends EWrapper {

  def tickPrice(tickerId: Int, field: Int, price: Double, canAutoExecute: Int): Unit = ???

  def tickSize(tickerId: Int, field: Int, size: Int): Unit = ???

  def tickOptionComputation(tickerId: Int, field: Int, impliedVol: Double, delta: Double, optPrice: Double, pvDividend: Double, gamma: Double, vega: Double, theta: Double, undPrice: Double): Unit = ???

  def tickGeneric(tickerId: Int, tickType: Int, value: Double): Unit = ???

  def tickString(tickerId: Int, tickType: Int, value: String): Unit = ???

  def tickEFP(tickerId: Int, tickType: Int, basisPoints: Double, formattedBasisPoints: String, impliedFuture: Double, holdDays: Int, futureExpiry: String, dividendImpact: Double, dividendsToExpiry: Double): Unit = ???

  def orderStatus(orderId: Int, status: String, filled: Int, remaining: Int, avgFillPrice: Double, permId: Int, parentId: Int, lastFillPrice: Double, clientId: Int, whyHeld: String): Unit = ???

  def openOrder(orderId: Int, contract: Contract, order: IBOrder, orderState: OrderState): Unit = ???

  def openOrderEnd(): Unit = ???

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

  def historicalData(reqId: Int, date: String, open: Double, high: Double, low: Double, close: Double, volume: Int, count: Int, WAP: Double, hasGaps: Boolean): Unit = ???

  def scannerParameters(xml: String): Unit = ???

  def scannerData(reqId: Int, rank: Int, contractDetails: ContractDetails, distance: String, benchmark: String, projection: String, legsStr: String): Unit = ???

  def scannerDataEnd(reqId: Int): Unit = ???

  def realtimeBar(reqId: Int, time: Long, open: Double, high: Double, low: Double, close: Double, volume: Long, wap: Double, count: Int): Unit = ???

  def currentTime(time: Long): Unit = ibActor ! CurrentTime(time).right

  def fundamentalData(reqId: Int, data: String): Unit = ???

  def deltaNeutralValidation(reqId: Int, underComp: UnderComp): Unit = ???

  def tickSnapshotEnd(reqId: Int): Unit = ???

  def marketDataType(reqId: Int, marketDataType: Int): Unit = ???

  def commissionReport(commissionReport: CommissionReport): Unit = ???

  def error(e: Exception): Unit = throw e // ???

  def error(str: String): Unit = throw UnknownIBError(-1, -1, str)

  def error(id: Int, errorCode: Int, errorMsg: String): Unit = errorCode match {
    case c if (2100 until 2110).contains(c) => ibActor ! WarningMessage(errorCode, errorMsg).right
    case _ => throw new UnknownIBError(id, errorCode, errorMsg)
  }

  def connectionClosed(): Unit = ???

}

case class UnknownIBError(id: Int, errorCode: Int, errorMsg: String) extends Exception