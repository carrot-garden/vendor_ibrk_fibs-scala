package name.kaeding.fibs
package contract

import scalaz._, Scalaz._

sealed trait SecurityId {
  val id: String
}
case class SIN(id: String) extends SecurityId
case class CUSIP(id: String) extends SecurityId
case class SEDOL(id: String) extends SecurityId
case class RIC(id: String) extends SecurityId

object SecurityId {
  implicit object securityIdCodeValue extends CodeValue[SecurityId] {
    def code(a: SecurityId) = a match {
      case _: SIN => "SIN"
      case _: CUSIP => "CUSIP"
      case _: SEDOL => "SEDOL"
      case _: RIC => "RIC"
    }
  }
  implicit def securityIdShows = new Show[SecurityId] {
    override def shows(s: SecurityId) = s.id
  }
}