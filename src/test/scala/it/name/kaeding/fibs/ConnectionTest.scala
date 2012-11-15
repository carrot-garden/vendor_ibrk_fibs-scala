package it.name.kaeding.fibs

import name.kaeding.fibs.ib.impl.IBImpl

import name.kaeding.fibs.contract.Stock

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
  } finally {
    ib.disconnect()
    println("is connected should be false: %s" format ib.isConnected)
  }
}