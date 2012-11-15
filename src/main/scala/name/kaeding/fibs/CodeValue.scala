package name.kaeding.fibs

import scalaz._, Scalaz._

trait CodeValue[A] {
  def code(a: A): String
}
trait CodeValueOps[F] extends syntax.Ops[F] {
  implicit def F: CodeValue[F]
  final def code: String = F.code(self)
}
trait ToCodeValueOps {
  implicit def ToCodeValueOps[F](v: F)(implicit F0: CodeValue[F]) =
    new CodeValueOps[F] { def self = v; implicit def F: CodeValue[F] = F0 }
}
trait CodeValueSyntax[F] {
  implicit def ToCodeValueOps(v: F): CodeValueOps[F] = new CodeValueOps[F] {
    def self = v; 
    implicit def F: CodeValue[F] = CodeValueSyntax.this.F
  }
  def F: CodeValue[F]
}