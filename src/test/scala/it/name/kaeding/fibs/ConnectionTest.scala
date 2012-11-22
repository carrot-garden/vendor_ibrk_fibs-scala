package it.name.kaeding.fibs

import name.kaeding.fibs.ib.impl.IBImpl
import name.kaeding.fibs.contract.Stock
import name.kaeding.fibs.ib.messages._
import org.scala_tools.time.Imports._

object ConnectionTest extends App {
  val ib = new IBImpl("localhost", 7496)

  try {
    println("is connected should be false: %s" format ib.isConnected)
    val connection = ib.connect()
    println("connection: %s" format connection.map(_.get))
    println("is connected should be true: %s" format ib.isConnected)
    println("serverVersion: %d" format ib.serverVersion)
    println("connectionTime: %s" format ib.TwsConnectionTime)
    val currentTime = ib.currentTime
    println("currentTime: %d" format currentTime.get)
    val aapl = ib.reqMktData(Stock("AAPL"), "", true)
    println("aapl: %s" format aapl.get)
    ib.reqHistoricalData(Stock("AAPL"), DateTime.now, 1.day, BarSize3Min, ShowMeTrades, true)
    Thread.sleep(3000)
  } finally {
    ib.disconnect()
    println("is connected should be false: %s" format ib.isConnected)
  }
}