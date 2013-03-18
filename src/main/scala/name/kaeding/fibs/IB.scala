package name.kaeding.fibs

import java.net.Socket
import com.github.nscala_time.time.Imports._

import com.ib.client.Contract
import com.ib.client.ExecutionFilter
import com.ib.client.Order
import com.ib.client.ScannerSubscription

import scalaz._, Scalaz._
import scalaz.concurrent._

import contract._
import ib.messages._

trait IB {
	def serverVersion(): Int

	def TwsConnectionTime(): String

	def isConnected(): Boolean

	def eConnect(socket: Socket, clientId: Int)

	def cancelScannerSubscription(tickerId: Int)

	def reqScannerParameters()

	def reqScannerSubscription(tickerId: Int,
			subscription: ScannerSubscription)

	def reqMktData(
	    security: Stock, // Security,
		genericTickList: String, 
		snapshot: Boolean): Promise[MarketDataResult]

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

	def placeOrder(id: Int, contract: Contract, order: Order)

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