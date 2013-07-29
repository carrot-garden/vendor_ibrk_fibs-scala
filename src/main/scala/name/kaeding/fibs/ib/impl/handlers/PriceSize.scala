package name.kaeding.fibs.ib.impl.handlers

import scalaz._, Scalaz._

/** NotThreadSafe */
private[handlers] class PriceSize {
  private[this] var _price: Option[Double] = none
  private[this] var _size: Option[Int] = none
  private[this] var newPrice: Option[Double] = none
  private[this] var newSize: Option[Int] = none
  def setPrice(p: Double): Boolean = {
    if (newSize.isDefined) {
      _size = newSize
      _price = p.some
      newSize = none
      newPrice = none
      true
    } else {
      newPrice = p.some
      false
    }
  }
  def setSize(s: Int): Boolean = {
    if (newPrice.isDefined) {
      _size = s.some
      _price = newPrice
      newSize = none
      newPrice = none
      true
    } else {
      newSize = s.some
      false
    }
  }
  def price = _price
  def size = _size
  def isDefined = _size.isDefined && _price.isDefined
}
private[handlers] object PriceSize {
  def empty = new PriceSize()
}

/** NotThreadSafe */
private[handlers] class PriceSizeTime {
  private[this] var _price: Option[Double] = none
  private[this] var _size: Option[Int] = none
  private[this] var _time: Option[Long] = none
  private[this] var newPrice: Option[Double] = none
  private[this] var newSize: Option[Int] = none
  private[this] var newTime: Option[Long] = none
  def setPrice(p: Double): Boolean = {
    if (newSize.isDefined && newTime.isDefined) {
      _size = newSize
      _time = newTime
      _price = p.some
      newSize = none
      newPrice = none
      newTime = none
      true
    } else {
      newPrice = p.some
      false
    }
  }
  def setSize(s: Int): Boolean = {
    if (newPrice.isDefined && newTime.isDefined) {
      _size = s.some
      _price = newPrice
      _time = newTime
      newSize = none
      newPrice = none
      newTime = none
      true
    } else {
      newSize = s.some
      false
    }
  }
  def setTime(t: Long): Boolean = {
    if (newPrice.isDefined && newSize.isDefined) {
      _time = t.some
      _price = newPrice
      _size = newSize
      newSize = none
      newPrice = none
      newTime = none
      true
    } else {
      newTime = t.some
      false
    }
  }
  def price = _price
  def size = _size
  def time = _time
  def isDefined = _size.isDefined && _price.isDefined && _time.isDefined
}
private[handlers] object PriceSizeTime {
  def empty = new PriceSizeTime()
}