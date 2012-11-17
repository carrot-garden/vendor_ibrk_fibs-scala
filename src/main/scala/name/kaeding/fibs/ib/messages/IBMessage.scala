package name.kaeding.fibs
package ib.messages

sealed trait IBMessage 
sealed case class ManagedAccounts(accounts: String) extends IBMessage
sealed case class NextValidId(nextId: Int) extends IBMessage
sealed case class CurrentTime(time: Long) extends IBMessage
sealed case class TickPrice(tickerId: Int, field: TickField, price: Double, canAutoExecute: Int) extends IBMessage
sealed case class TickSize(tickerId: Int, field: TickField, size: Int) extends IBMessage
sealed case class TickGeneric(tickerId: Int, tickType: TickField, value: Double) extends IBMessage
sealed case class TickOptionComputation(tickerId: Int, field: TickField, impliedVol: Double, delta: Double, optPrice: Double, pvDividend: Double, gamma: Double, vega: Double, theta: Double, undPrice: Double) extends IBMessage
sealed case class TickString(tickerId: Int, tickType: TickField, value: String) extends IBMessage
sealed case class TickSnapshotEnd(reqId: Int) extends IBMessage

sealed trait ErrorCodes extends IBMessage
sealed case class WarningMessage(errorCode: Int, errorMessage: String) extends ErrorCodes

sealed trait TickField
object TickField {
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

// Responses

sealed case class ConnectionResult(managedAccounts: String, nextValidId: Int)
sealed case class MarketDataResult(
    symbol: String, 
    bidPrice: Double, 
    bidSize: Int, 
    askPrice: Double, 
    askSize: Int,
    lastPrice: Option[Double],
    lastSize: Option[Int],
    high: Option[Double],
    low: Option[Double],
    open: Option[Double],
    close: Option[Double],
    volume: Option[Int],
    timestamp: Option[Long],
    halted: Option[Boolean])