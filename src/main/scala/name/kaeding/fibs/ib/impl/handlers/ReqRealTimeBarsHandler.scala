package name.kaeding.fibs
package ib
package impl
package handlers

import java.util.concurrent.CountDownLatch
import scalaz._, Scalaz._
import scalaz.concurrent._
import messages._
import contract._
import java.util.concurrent.{ LinkedBlockingQueue, BlockingQueue }
import com.ib.client.EClientSocket
import com.github.nscala_time.time.Imports._
import grizzled.slf4j.Logging

class ReqRealTimeBarsHandler(security: Stock /*Security*/ ,
                             ibActor: Actor[FibsPromiseMessage \/ IBMessage],
                             tickerId: Int, socket: EClientSocketLike) extends FibsPromise[CloseableStream[RealTimeBar]] with Logging { 
  private[this] val TickerId = tickerId
  val latch = new CountDownLatch(0) // don't need to block
  val actor = Actor[IBMessage] {
    case RealTimeBarResp(TickerId, time, open, high, low, close, volume, count, wap) ⇒ 
      queue.add(RealTimeBar(new DateTime(time * 1000), open, high, low, close, volume, count, wap).some)
    case _ ⇒ ???
  }
  val barHandler: PartialFunction[IBMessage, Unit] = {
    case m@RealTimeBarResp(TickerId, time, open, high, low, close, volume, count, wap) ⇒ actor ! m
  }
  val patterns = List(barHandler)
  private[this] def toStream: EphemeralStream[RealTimeBar] = {
    val ret: EphemeralStream[RealTimeBar] = queue.take match {
      case Some(d) ⇒ EphemeralStream.cons(d, toStream)
      case None    ⇒ EphemeralStream.emptyEphemeralStream
    }
    ret
  }
  private[this] val queue: BlockingQueue[Option[RealTimeBar]] =
    new LinkedBlockingQueue[Option[RealTimeBar]]()
  private[this] def closeStream = {
    queue add None
    socket.cancelRealTimeBars(tickerId)
    ibActor ! UnregisterFibsPromise(this).left
  }
  def get = new CloseableStream[RealTimeBar] {
    def close = closeStream 
    lazy val as = toStream
  }
}