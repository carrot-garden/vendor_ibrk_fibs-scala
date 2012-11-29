package name.kaeding.fibs
package ib
package impl
package handlers

import org.specs2._
import org.scalacheck._
import Arbitrary._
import scalaz._, Scalaz._
import scalaz.concurrent._
import scala.util.Random.shuffle
import scala.collection.mutable.MutableList
import java.util.concurrent.CountDownLatch

import org.scala_tools.time.Imports._

import messages._
import contract._

class ReqHistoricalDataHandlerSpec extends Specification with ScalaCheck {
  def is =
    "ReqHistoricalDataHandler" ^
      "all data sent through actor makes it into the response" ! allMessagesNoNoiseEx ^
      "accept only data with the correct tickerId, ignoring other data" ! noisyDataEx ^
      "returns empty stream when error message is sent indicating no data" ! noDataErrorEx ^
      end

  def allMessagesNoNoiseEx = prop { (h: HistoricalDataPackage) =>
    {
      val ibActor = IBActor()
      val handler = new ReqHistoricalDataHandler(Stock(h.symbol), ibActor, h.tickerId)

      ibActor ! RegisterFibsPromise(handler).left

      val p = handler.promise
      h.messages.foreach(ibActor ! _.right)

      p.get.toList must_== h.expectedPeriods
    }

  }

  def noisyDataEx = prop { (h: HistoricalDataPackage, noise: List[HistoricalDataPackage]) =>
    {
      val filteredNoise = noise.filterNot(_.tickerId === h.tickerId)
      val ibActor = IBActor()
      val handler = new ReqHistoricalDataHandler(Stock(h.symbol), ibActor, h.tickerId)

      ibActor ! RegisterFibsPromise(handler).left
      val noiseHandler = new FibsPromise[Unit] {
        val targetMessages = new MutableList[IBMessage]
        val noiseIds = filteredNoise.map(_.tickerId)
        val actor = Actor[IBMessage] {
          case m @ HistoricalData(id, _, _, _, _, _, _, _, _, _) if (noiseIds contains id) => logMessage(id, m)
          case _ => ???
        }
        def get = ()
        val latch = new CountDownLatch(0)
        def logMessage(id: Int, m: IBMessage) =
          if (id === h.tickerId) {
            targetMessages += m
          }
        val patterns = List(({
          case m @ HistoricalData(id, _, _, _, _, _, _, _, _, _) if (noiseIds contains id) => logMessage(id, m)
        }): PartialFunction[IBMessage, Unit])
      }
      ibActor ! RegisterFibsPromise(noiseHandler).left
      val p = handler.promise

      val allMessages = (h.messages :: filteredNoise.map(_.messages)).join
      allMessages.foreach(ibActor ! _.right)

      (noiseHandler.targetMessages must be empty) and 
      (p.get.toList must_== h.expectedPeriods)
    }
  }
  
  def noDataErrorEx = prop { (tickerId: Int, symbol: String) => 
    {
      val ibActor = IBActor()
      val handler = new ReqHistoricalDataHandler(Stock(symbol), ibActor, tickerId)

      ibActor ! RegisterFibsPromise(handler).left
      val p = handler.promise
      
      ibActor ! HistoricalDataError(tickerId, 162, "Historical Market Data Service error message:HMDS query returned no data: %s@SMART Trades".format(symbol)).right
      
      p.get.toList must be empty
    }
  }
}

case class HistoricalDataPackage(
  tickerId: Int,
  symbol: String,
  expectedPeriods: List[HistoricalDataPeriod],
  messages: List[IBMessage])
    