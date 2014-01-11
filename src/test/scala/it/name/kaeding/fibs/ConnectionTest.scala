package it.name.kaeding.fibs

import scalaz._, Scalaz._

import name.kaeding.fibs.ib.impl.IBImpl
import name.kaeding.fibs.contract.Stock
import name.kaeding.fibs.ib.messages._
import com.github.nscala_time.time.Imports._
import name.kaeding.fibs.order._
import java.util.TimeZone
import name.kaeding.fibs.ib.impl.OCA
import scalaz.concurrent.Future
import scala.concurrent.ExecutionContext

object ConnectionTest extends App {
  val ib = new IBImpl("localhost", 7496, Some(33))

  try {
    println("is connected should be false: %s" format ib.isConnected)
    val connection = ib.connect()
    println("connection: %s" format connection.map(_.get))
    println("is connected should be true: %s" format ib.isConnected)
    println("serverVersion: %d" format ib.serverVersion)
    println("connectionTime: %s" format ib.TwsConnectionTime)
    val currentTime = ib.currentTime
    println("currentTime: %d" format currentTime.get)
    
    val aapl = ib.reqMktDataSnapshot(Stock("AAPL"), "")
    println("aapl: %s" format aapl.get)
//    val bby = ib.reqMktDataSnapshot(Stock("BBY"), "")
//    println("bby: %s" format bby.get)
//    val aa = ib.reqMktDataSnapshot(Stock("AA"), "")
//    println("aa: %s" format aa.get)
//    val stp = ib.reqMktDataSnapshot(Stock("STP"), "")
//    println("stp: %s" format stp.get)
    
//    val periodEnd = new LocalDate("2013-08-08").toDateTimeAtStartOfDay(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York"))).withHourOfDay(10).withMinuteOfHour(30)
//    val hist = ib.reqHistoricalData(Stock("AAPL"), (periodEnd - 30.minutes), 30.minutes, BarSize1Sec, ShowMeTrades, true)
//    println("sent historical data request")
//    println("Historical AAPL: %s" format hist.get.toList)
    
//    println("Streaming AAPL:")
//    val aaplStream = ib.reqMktDataStream(Stock("AAPL"), "")
//    aaplStream.as.map(println).take(250)
//    aaplStream.close
    
//    println("Streaming AAPL ticks:")
//    val stpTickStream = ib.reqTickDataStream(Stock("STP"))
//    val exelTickStream = ib.reqTickDataStream(Stock("EXEL"))
//    import ExecutionContext.Implicits.global
//    val fut = ((Future(stpTickStream.as.map(println).take(25)) |@|
//     Future(exelTickStream.as.map(println).take(25)))((_, _) => ()) >>
//    Future { 
//      stpTickStream.close
//      exelTickStream.close
//    })
//    fut.run
    
//    println("Streaming STP ticks:")
//    val stpTickStream = ib.reqTickDataStream(Stock("STP"))
//    stpTickStream.as.map(println).take(25)
//    stpTickStream.close
    Thread.sleep(3000)
    
//    println("Streaming IMGN bars:")
//    val imgnBarStream = ib.reqRealTimeBarsFromTrades(Stock("IMGN"), 5, false)
//    imgnBarStream.as.map(println).take(25)
//    imgnBarStream.close
//    Thread.sleep(30000)
//    
//    println("Streaming IMGN ticks:")
//    val imgnTickStream = ib.reqTickDataStream(Stock("IMGN"))
//    imgnTickStream.as.map(println).take(25)
//    imgnTickStream.close
//    Thread.sleep(30000)
    
//    val aaplOrder = LimitOrder(Buy, Stock("AAPL"), 1.50, 100)
//    ib.placeOrder(aaplOrder)
//    Thread.sleep(30000)
    
//    val aaplTrailStopOrder = TrailStopOrder(Sell, Stock("AAPL"), 5.00, 100)
//    ib.placeOrder(aaplTrailStopOrder)
//    Thread.sleep(30000)
    
//    val aaplTrailStopLimitOrder = TrailStopLimitOrder(Sell, Stock("AAPL"), 0.10, 15.00, 5.00, 100)
//    ib.placeOrder(aaplTrailStopLimitOrder)
//    Thread.sleep(30000)
    
//    val aaplMoCOrder = MarketOnCloseOrder(Buy, Stock("AAPL"), 100)
//    ib.placeOrder(aaplMoCOrder)
//    Thread.sleep(30000)
    
//    val aaplTrailStopOrderOCA = TrailStopOrder(Sell, Stock("AAPL"), 5.00, 100)
//    val aaplMoCOrderOCA = MarketOnCloseOrder(Buy, Stock("AAPL"), 100)
//    val ocaGroup = aaplTrailStopOrderOCA :: aaplMoCOrderOCA :: OCA
//    ib.placeOCAOrders(ocaGroup, ReduceOnFillWithBlock)
//    Thread.sleep(30000)
    
//    println("getting order status")
//    ib.reqAllOpenOrders
//    Thread.sleep(3000)
  } catch {
    case e: Exception => e.printStackTrace
  } finally {
    ib.disconnect()
    println("is connected should be false: %s" format ib.isConnected)
  }
}