package name.kaeding.fibs

import scalaz._, Scalaz._
import contract._
import com.ib.client.{ Order => IBOrder }

package object order {
  implicit def LimitOrder2IB[A: Contract](o: LimitOrder[A]): IBOrder = {
    val ret = new IBOrder
    ret
  }
}