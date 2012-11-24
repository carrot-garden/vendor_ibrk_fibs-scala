package name.kaeding.fibs
package ib
package impl
package handlers

import java.util.concurrent.CountDownLatch
import java.util.concurrent.{ LinkedBlockingQueue, BlockingQueue }

import scalaz._, Scalaz._
import scalaz.concurrent._
import org.scala_tools.time.Imports._

import messages._
import contract._

class ReqHistoricalDataHandler(security: Stock /*Security*/ ,
  ibActor: Actor[FibsPromiseMessage \/ IBMessage],
  tickerId: Int) extends FibsPromise[Stream[HistoricalDataPeriod]] {
  private[this] val TickerId = tickerId
  val actor = Actor[IBMessage]{
    case d @ HistoricalData(TickerId, time, _, _, _, _, _, _, _, _) if (!time.startsWith("finished-")) =>
      enqueue(transformMsg(d))
    case d @ HistoricalData(TickerId, time, _, _, _, _, _, _, _, _) if (time.startsWith("finished-")) =>
      close
    case _ => ???
  }
  val historicalDataHandler: PartialFunction[IBMessage, Unit] = {
    case d @ HistoricalData(TickerId, _, _, _, _, _, _, _, _, _) => actor ! d
  }
  val patterns = List(historicalDataHandler)
  val latch = new CountDownLatch(0) // don't need to block
  def get = toStream
  private[this] def transformMsg(i: HistoricalData) = 
    HistoricalDataPeriod(
      new DateTime(i.date.parseLong.toOption.getOrElse(0L) * 1000),
      i.open,
      i.high,
      i.low,
      i.close,
      i.volume,
      i.count,
      i.wap,
      i.hasGaps)
  private[this] val queue: BlockingQueue[Option[HistoricalDataPeriod]] =
    new LinkedBlockingQueue[Option[HistoricalDataPeriod]]()
  private[this] def close = {
    queue add None
    ibActor ! UnregisterFibsPromise(this).left
  }
  private[this] def enqueue(d: HistoricalDataPeriod) = queue add d.some
  private[this] def toStream(): Stream[HistoricalDataPeriod] = queue.take match {
    case Some(d) => Stream.cons(d, toStream)
    case None => Stream.empty
  }
}
