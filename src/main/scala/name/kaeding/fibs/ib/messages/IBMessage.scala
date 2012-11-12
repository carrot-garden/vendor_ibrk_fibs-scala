package name.kaeding.fibs.ib.messages

sealed trait IBMessage 
sealed case class ManagedAccounts(accounts: String) extends IBMessage
sealed case class NextValidId(nextId: Int) extends IBMessage

sealed trait ErrorCodes extends IBMessage
sealed case class WarningMessage(errorCode: Int, errorMessage: String) extends ErrorCodes