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

class ReqMarketTickDataStreamHandler(security: Stock /*Security*/ ,
                                 ibActor: Actor[FibsPromiseMessage \/ IBMessage],
                                 tickerId: Int, socket: EClientSocketLike) extends FibsPromise[CloseableStream[MarketTickDataResult]] {
  private[this] val TickerId = tickerId
  val latch = new CountDownLatch(0) // don't need to block
  private[this] val RTVolumePattern = "(\\d+\\.?\\d*);(\\d+);(\\d+);(\\d+);(\\d+\\.?\\d*);(true|false)".r
  val actor = Actor[IBMessage] {
    case TickString(TickerId, RTVolume, v) ⇒ 
      parseInput(v).foreach(t => queue.add(t.some))
    case _ ⇒ ???
  }
  def parseInput(s: String) = s match {
    case RTVolumePattern(p, s, t, v, w, f) => 
      (p.parseDouble.toOption |@|
       s.parseInt.toOption |@|
       v.parseInt.toOption |@| 
       t.parseLong.toOption |@|
       w.parseDouble.toOption |@|
       f.parseBoolean.toOption)(MarketTickDataResult.apply)
    case _ => none
  }
  val stringHandler: PartialFunction[IBMessage, Unit] = {
    case m@TickString(tickerId, _, _) ⇒ actor ! m
  }
  val patterns = List(stringHandler)
  private[this] val queue: BlockingQueue[Option[MarketTickDataResult]] =
    new LinkedBlockingQueue[Option[MarketTickDataResult]]()
  private[this] def closeStream = {
    queue add None
    socket.cancelMktData(TickerId)
    ibActor ! UnregisterFibsPromise(this).left
  }
  private[this] def toStream: EphemeralStream[MarketTickDataResult] = {
    val ret: EphemeralStream[MarketTickDataResult] = queue.take match {
      case Some(d) ⇒ EphemeralStream.cons(d, toStream)
      case None    ⇒ EphemeralStream.emptyEphemeralStream
    }
    ret
  }
  def get = new CloseableStream[MarketTickDataResult] {
    def close = closeStream 
    lazy val as = toStream
  }
}
