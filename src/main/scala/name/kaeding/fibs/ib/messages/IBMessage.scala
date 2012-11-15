package name.kaeding.fibs
package ib.messages

sealed trait IBMessage 
sealed case class ManagedAccounts(accounts: String) extends IBMessage
sealed case class NextValidId(nextId: Int) extends IBMessage
sealed case class CurrentTime(time: Long) extends IBMessage
sealed case class TickPrice(tickerId: Int, field: TickField, price: Double, canAutoExecute: Int) extends IBMessage
sealed case class TickSize(tickerId: Int, field: TickField, size: Int) extends IBMessage
sealed case class TickGeneric(tickerId: Int, tickType: Int, value: Double) extends IBMessage
sealed case class TickOptionComputation(tickerId: Int, field: TickField, impliedVol: Double, delta: Double, optPrice: Double, pvDividend: Double, gamma: Double, vega: Double, theta: Double, undPrice: Double) extends IBMessage

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
    case _ => ???
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

// Responses

sealed case class ConnectionResult(managedAccounts: String, nextValidId: Int)
sealed case class MarketDataResult(
    symbol: String, 
    bidPrice: Double, 
    bidSize: Int, 
    askPrice: Double, 
    askSize: Int)