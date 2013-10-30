package name.kaeding.fibs
package ib
package impl
package handlers

import org.junit.runner.RunWith
import org.specs2.runner.{ JUnitRunner }
import org.specs2._
import org.scalacheck._
import Arbitrary._
import scalaz._, Scalaz._
import scalaz.concurrent._
import scala.util.Random.shuffle
import scala.collection.mutable.MutableList
import java.util.concurrent.CountDownLatch

import com.github.nscala_time.time.Imports._

import messages._
import contract._

@RunWith(classOf[JUnitRunner])
class ReqRealTimeBarsHandlerSpec extends Specification with ScalaCheck {
  def is =
    "ReqRealTimeBarsHandlerSpec" ^
      "all data sent through actor makes it into the response" ! allMessagesNoNoiseEx ^
      "accept only data with the correct tickerId, ignoring other data" ! noisyDataEx ^
      end

  def allMessagesNoNoiseEx = prop { (d: RealTimeBarsPackage) =>
    {
      val ibActor = IBActor()
      val socket = mkSocket
      val handler = new ReqRealTimeBarsHandler(Stock(d.symbol), ibActor, d.tickerId, socket)

      ibActor ! RegisterFibsPromise(handler).left

      val p = handler.promise
      d.messages.foreach(ibActor ! _.right)
      val r = p.get
      Thread.sleep(300)
      r.close

      r.as.toList must_== d.expectedBars
    }

  }

  def noisyDataEx = prop { (d: RealTimeBarsPackage, noise: List[RealTimeBarsPackage]) =>
    {
      val filteredNoise = noise.filterNot(_.tickerId === d.tickerId)
      val ibActor = IBActor()
      val socket = mkSocket
      val handler = new ReqRealTimeBarsHandler(Stock(d.symbol), ibActor, d.tickerId, socket)

      ibActor ! RegisterFibsPromise(handler).left
      val noiseHandler = new FibsPromise[Unit] {
        val targetMessages = new MutableList[IBMessage]
        val noiseIds = filteredNoise.map(_.tickerId)
        val actor = Actor[IBMessage] {
          case m @ TickString(id, _, _) if (noiseIds contains id) => logMessage(id, m)
          case _ => ???
        }
        def get = ()
        val latch = new CountDownLatch(0)
        def logMessage(id: Int, m: IBMessage) =
          if (id === d.tickerId) {
            targetMessages += m
          }
        val patterns = List(({
          case m @ TickString(id, _, _) if (noiseIds contains id) => logMessage(id, m)
        }): PartialFunction[IBMessage, Unit])
      }
      ibActor ! RegisterFibsPromise(noiseHandler).left
      val p = handler.promise

      val allMessages = (d.messages :: filteredNoise.map(_.messages)).join
      allMessages.foreach(ibActor ! _.right)
      val r = p.get
      Thread.sleep(300)
      r.close

      (noiseHandler.targetMessages must be empty) and 
      (r.as.toList must_== d.expectedBars)
    }
  }
}

case class RealTimeBarsPackage(
  tickerId: Int,
  symbol: String,
  expectedBars: List[RealTimeBar],
  messages: List[IBMessage])   