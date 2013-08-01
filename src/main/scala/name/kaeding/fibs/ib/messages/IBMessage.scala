package name.kaeding.fibs
package ib.messages

import scalaz._, Scalaz._
import com.github.nscala_time.time.Imports._

sealed trait IBMessage 
sealed case class UnknownIBError(id: Int, errorCode: Int, errorMsg: String) extends IBMessage
sealed case class ManagedAccounts(accounts: String) extends IBMessage
sealed case class NextValidId(nextId: Int) extends IBMessage
sealed case class CurrentTime(time: Long) extends IBMessage
sealed case class TickPrice(tickerId: Int, field: TickField, price: Double, canAutoExecute: Int) extends IBMessage
sealed case class TickSize(tickerId: Int, field: TickField, size: Int) extends IBMessage
sealed case class TickGeneric(tickerId: Int, tickType: TickField, value: Double) extends IBMessage
sealed case class TickOptionComputation(tickerId: Int, field: TickField, impliedVol: Double, delta: Double, optPrice: Double, pvDividend: Double, gamma: Double, vega: Double, theta: Double, undPrice: Double) extends IBMessage
sealed case class TickString(tickerId: Int, tickType: TickField, value: String) extends IBMessage
sealed case class TickSnapshotEnd(reqId: Int) extends IBMessage
sealed case class HistoricalData(reqId: Int, date: String, open: Double, high: Double, 
      low: Double, close: Double, volume: Int, count: Int, wap: Double, 
      hasGaps: Boolean) extends IBMessage
sealed case class HistoricalDataError(tickerId: Int, errorCode: Int, errorMsg: String) extends IBMessage

sealed trait ErrorCodes extends IBMessage
sealed case class WarningMessage(errorCode: Int, errorMessage: String) extends ErrorCodes

sealed trait TickField
object TickField {
  implicit def tickFieldEqual = Equal.equalA[TickField]
  def read(code: Int) = code match {
    case 0 => TickBidSize
    case 1 => TickBid
    case 2 => TickAsk
    case 3 => TickAskSize
    case 4 => TickLast
    case 5 => TickLastSize
    case 6 => TickHigh
    case 7 => TickLow
    case 8 => TickVolume
    case 9 => TickClose
    case 10 => TickBidOptionComputation
    case 11 => TickAskOptionComputation
    case 12 => TickLastOptionComputation
    case 13 => TickModelOptionComputation
    case 14 => TickOpen
    case 15 => TickHigh13Week
    case 16 => TickLow13Week
    case 17 => TickHigh26Week
    case 18 => TickLow26Week
    case 19 => TickHigh52Week
    case 20 => TickLow52Week
    case 45 => TickLastTimestamp
    case 49 => TickHalted
    case x => println("unknown tick field: %d" format x); ???
  }
}
object TickBidSize extends TickField
object TickBid extends TickField
object TickAsk extends TickField
object TickAskSize extends TickField
object TickLast extends TickField
object TickLastSize extends TickField
object TickHigh extends TickField
object TickLow extends TickField
object TickVolume extends TickField
object TickClose extends TickField
object TickBidOptionComputation extends TickField
object TickAskOptionComputation extends TickField
object TickLastOptionComputation extends TickField
object TickModelOptionComputation extends TickField
object TickHigh13Week extends TickField
object TickLow13Week extends TickField
object TickHigh26Week extends TickField
object TickLow26Week extends TickField
object TickHigh52Week extends TickField
object TickLow52Week extends TickField
object TickOpen extends TickField
object TickLastTimestamp extends TickField
object TickHalted extends TickField

sealed trait BarSize
object BarSize {
  implicit def barSizeShows = new Show[BarSize] {
    override def shows(s: BarSize) = s match {
      case BarSize1Sec => "1 secs"
      case BarSize5Sec => "5 secs"
      case BarSize15Sec => "15 secs"
      case BarSize30Sec => "30 secs"
      case BarSize1Min => "1 mins"
      case BarSize2Min => "2 mins"
      case BarSize3Min => "3 mins"
      case BarSize5Min => "5 mins"
      case BarSize15Min => "15 mins"
      case BarSize30Min => "30 mins"
      case BarSize1Hour => "1 hour"
      case BarSize1Day => "1 day"
    }
  }
}
object BarSize1Sec extends BarSize
object BarSize5Sec extends BarSize
object BarSize15Sec extends BarSize
object BarSize30Sec extends BarSize
object BarSize1Min extends BarSize
object BarSize2Min extends BarSize
object BarSize3Min extends BarSize
object BarSize5Min extends BarSize
object BarSize15Min extends BarSize
object BarSize30Min extends BarSize
object BarSize1Hour extends BarSize
object BarSize1Day extends BarSize

sealed trait ShowMe
object ShowMe {
  implicit def showMeShows = new Show[ShowMe] {
    override def shows(s: ShowMe) = s match {
      case ShowMeTrades => "TRADES"
      case ShowMeMidpoint => "MIDPOINT"
      case ShowMeBid => "BID"
      case ShowMeAsk => "ASK"
      case ShowMeBidAsk => "BID_ASK"
      case ShowMeHistoricalVolatility => "HISTORICAL_VOLATILITY"
      case ShowMeOptionImpliedVolatility => "OPTION_IMPLIED_VOLATILITY"
    }
  }
}
object ShowMeTrades extends ShowMe
object ShowMeMidpoint extends ShowMe
object ShowMeBid extends ShowMe
object ShowMeAsk extends ShowMe
object ShowMeBidAsk extends ShowMe
object ShowMeHistoricalVolatility extends ShowMe
object ShowMeOptionImpliedVolatility extends ShowMe

sealed case class CommissionReport(
    execId: String, 
    commission: Double, 
    currency: String, 
    realizedPL: Double,
    totalYield: Double,
    yieldRedemptionDate: DateTime) extends IBMessage

// Responses

import com.github.nscala_time.time.Imports._

sealed case class ConnectionResult(managedAccounts: String, nextValidId: Int)
sealed case class MarketDataResult(
    symbol: String, 
    bidPrice: Option[Double], 
    bidSize: Option[Int], 
    askPrice: Option[Double], 
    askSize: Option[Int],
    lastPrice: Option[Double],
    lastSize: Option[Int],
    high: Option[Double],
    low: Option[Double],
    open: Option[Double],
    close: Option[Double],
    volume: Option[Int],
    timestamp: Option[Long],
    halted: Option[Boolean],
    received: DateTime)
sealed case class HistoricalDataPeriod(
    time: DateTime,
    open: Double,
    high: Double,
    low: Double,
    close: Double,
    volume: Int,
    count: Int,
    wap: Double,
    hasGaps: Boolean)

sealed trait OrderType
object OrderType {
  implicit val OrderTypeShows = new Show[OrderType] {
    override def shows(t: OrderType) = t match {
      case Limit => "LMT"
      case Stop => "STP"
      case StopLimit => "STP LMT"
      case TrailLimitIfTouched => "TRAIL LIT"
      case TrailMarketIfTouched => "TRAIL MIT"
      case TrailStop => "TRAIL"
      case TraitStopLimit => "TRAIL LIMIT"
      case Market => "MKT"
    }
  }
}
case object Limit extends OrderType
case object Stop extends OrderType
case object StopLimit extends OrderType
case object TrailLimitIfTouched extends OrderType
case object TrailMarketIfTouched extends OrderType
case object TrailStop extends OrderType
case object TraitStopLimit extends OrderType
case object Market extends OrderType
// ... and more