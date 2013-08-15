package it.name.kaeding.fibs

import name.kaeding.fibs.ib.impl.IBImpl
import name.kaeding.fibs.contract.Stock
import name.kaeding.fibs.ib.messages._
import com.github.nscala_time.time.Imports._
import name.kaeding.fibs.order.LimitOrder
import name.kaeding.fibs.order.Buy
import java.util.TimeZone

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
//    val aapl = ib.reqMktDataSnapshot(Stock("AAPL"), "")
//    println("aapl: %s" format aapl.get)
    
//    val periodEnd = new LocalDate("2013-08-08").toDateTimeAtStartOfDay(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York"))).withHourOfDay(10).withMinuteOfHour(30)
//    val hist = ib.reqHistoricalData(Stock("AAPL"), (periodEnd - 30.minutes), 30.minutes, BarSize1Sec, ShowMeTrades, true)
//    println("sent historical data request")
//    println("Historical AAPL: %s" format hist.get.toList)
    
//    println("Streaming AAPL:")
//    val aaplStream = ib.reqMktDataStream(Stock("AAPL"), "")
//    aaplStream.as.map(println).take(250)
//    aaplStream.close
    
    println("Streaming AAPL ticks:")
    val aaplStream = ib.reqTickDataStream(Stock("AAPL"))
    aaplStream.as.map(println).take(250)
    aaplStream.close
    
//    val aaplOrder = LimitOrder(Buy, Stock("AAPL"), 1.50, 100)
//    ib.placeOrder(aaplOrder)
    Thread.sleep(3000)
  } finally {
    ib.disconnect()
    println("is connected should be false: %s" format ib.isConnected)
  }
}