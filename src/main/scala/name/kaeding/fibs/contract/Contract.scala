package name.kaeding.fibs
package contract

import scalaz._, Scalaz._
import com.ib.client.{ Contract => IBContract }

trait Contract[A] {
  def contract(a: A): IBContract
}
trait ContractOps[F] extends syntax.Ops[F] {
  implicit def F: Contract[F]
  final def contract: IBContract = F.contract(self)
}
trait ToContractOps {
  implicit def ToContractOps[F](v: F)(implicit F0: Contract[F]) = 
    new ContractOps[F] {def self = v; implicit def F: Contract[F] = F0 }
}
trait ContractSyntax[F] {
  implicit def ToContractOps(v: F): ContractOps[F] = new ContractOps[F] {
    def self = v;
    implicit def F: Contract[F] = ContractSyntax.this.F
  }
  def F: Contract[F]
}

