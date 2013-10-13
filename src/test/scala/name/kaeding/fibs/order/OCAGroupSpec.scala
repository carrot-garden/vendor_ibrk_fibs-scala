package name.kaeding.fibs
package order

import org.junit.runner.RunWith
import org.specs2.runner.{ JUnitRunner }
import org.specs2._
import org.scalacheck._
import Arbitrary._

import scalaz._, Scalaz._
import contract._
import order.{Order => FibsOrder, _}
import OrderAction._
import ib.messages._
import ib.impl._
import ib.impl.HasIBOrder._
import com.ib.client.{ Order => IBOrder }
import TestData._

@RunWith(classOf[JUnitRunner])
class OCAGroupSpec extends Specification with ScalaCheck { def is =
  "OCAGroup converted to IBOrder must" ^
  	"represent each input order" ! exEachOrder^
  	"have an OCA name" ! exOcaGroupHasName^
  	"have the same OCA Group name" ! exOcaGroupSameName^
  	"have a distinct OCA Group name from another OCA group" ! exOcaGroupDistinctName^
  	"have the right OCA type" ! exOcaTypeMatches^
  end

  def exEachOrder = prop { (os: List[FibsOrder[Stock]], ocaName: String, ocaType: OCAType) =>
    val oca = os.foldLeft(OCA: OCAGroup)((oca, o) => o :: oca)
    oca.ibOrders(ocaName, ocaType).map(_.apply(-1)).map(o => (o._2.m_action, o._2.m_totalQuantity)) must containTheSameElementsAs(
        os.map(o => (o.action.shows, o.qty)))
  }

  def exOcaGroupHasName = prop { (os: NonEmptyList[FibsOrder[Stock]], ocaName: String, ocaType: OCAType) =>
    val oca = os.foldLeft(OCA: OCAGroup)((oca, o) => o :: oca)
    oca.ibOrders(ocaName, ocaType).map(_.apply(-1)).map(o => Option(o._2.m_ocaGroup)) must contain(beSome(ocaName)).forall
  }

  def exOcaGroupSameName = prop { (os: List[FibsOrder[Stock]], ocaName: String, ocaType: OCAType) =>
    val oca = os.foldLeft(OCA: OCAGroup)((oca, o) => o :: oca)
    oca.ibOrders(ocaName, ocaType).map(_.apply(-1)).map(o => Option(o._2.m_ocaGroup)).distinct.length must be_<(2)
  }

  def exOcaGroupDistinctName = prop { (os: NonEmptyList[FibsOrder[Stock]], ocaName1: String, ocaType: OCAType) =>
    val oca1 = os.foldLeft(OCA: OCAGroup)((oca, o) => o :: oca)
    val oca2 = os.foldLeft(OCA: OCAGroup)((oca, o) => o :: oca)
    oca1.ibOrders(ocaName1, ocaType).map(_.apply(-1)).map(o => Option(o._2.m_ocaGroup)).distinct must_!= 
      oca2.ibOrders(ocaName1 + "-2", ocaType).map(_.apply(-1)).map(o => Option(o._2.m_ocaGroup)).distinct
  }
  
  def exOcaTypeMatches = prop { (os: NonEmptyList[FibsOrder[Stock]], ocaName: String, ocaType: OCAType) =>
    val oca = os.foldLeft(OCA: OCAGroup)((oca, o) => o :: oca)
    oca.ibOrders(ocaName, ocaType).map(_.apply(-1)).map(o => Option(o._2.m_ocaType)) must contain(beSome(OCAType.code(ocaType))).forall 
  }

}