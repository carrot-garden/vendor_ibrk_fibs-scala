package name.kaeding.fibs.ib

import scalaz._, Scalaz._
import org.scala_tools.time.Imports._

package object impl {
  implicit def periodShows = new Show[Period] {
    override def shows(period: Period) = period match {
      case p if p.getYears == 1 => "1 Y"
      case p if p.getMonths > 0 => "%d M" format p.getMonths
      case p if p.toStandardDays.getDays < 1 => "%d S" format p.toStandardSeconds.getSeconds
      case p if p.toStandardDays.getDays <= 60 => "%d D" format p.toStandardDays.getDays
      case p if p.toStandardWeeks.getWeeks <= 52 => "%d W" format p.toStandardWeeks.getWeeks
    } 
  }
}