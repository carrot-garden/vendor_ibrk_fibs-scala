package name.kaeding.fibs

import scalaz._

trait CloseableStream[A] {
  def as: EphemeralStream[A]
  def close: Unit
}