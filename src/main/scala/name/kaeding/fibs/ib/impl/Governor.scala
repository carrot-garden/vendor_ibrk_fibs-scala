package name.kaeding.fibs.ib.impl

/**
 * Allow compliance with API QoS request limits 
 * 
 * <code>governor.requestClearance</code> should be called
 * just before the request is made, in the same thread that
 * makes the request.
 * 
 * This governor is thread-safe, and is designed to be shared
 * across threads.
 * 
 */
class Governor(minTimeBetweenRequests: Int/* in ms*/) {
  private[this] var lastReq: Long = 0
  
  def requestClearance = this.synchronized {
    import java.util.Date
    val now = new Date().getTime
    val timeSince = now - lastReq
    if (timeSince < minTimeBetweenRequests) {
      Thread.sleep(minTimeBetweenRequests - timeSince)
    }
    lastReq = new Date().getTime
  }
}