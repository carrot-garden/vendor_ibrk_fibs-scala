package name.kaeding.fibs.ib.impl

import com.ib.client.EClientSocket

trait EClientSocketLike {
  def cancelMktData(tickerId: Int): Unit
  def cancelRealTimeBars(tickerId: Int): Unit
}
object EClientSocketLike {
  def apply(socket: EClientSocket) = new EClientSocketLike {
    def cancelMktData(tickerId: Int) = socket.cancelMktData(tickerId)
    def cancelRealTimeBars(tickerId: Int) = socket.cancelRealTimeBars(tickerId)
  }
}