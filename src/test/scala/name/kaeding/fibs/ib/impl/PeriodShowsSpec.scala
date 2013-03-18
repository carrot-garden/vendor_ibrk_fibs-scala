package name.kaeding.fibs
package ib
package impl

import org.specs2._
import org.scalacheck._
import Arbitrary._

import com.github.nscala_time.time.Imports._
import com.github.nscala_time.time.{RichInt => RI}

import scalaz._, Scalaz._

class PeriodShowsSpec extends Specification with ScalaCheck { def is =
  "Period.shows must" ^
  	"give duration in Years if 1 year is given" ! exYear^
	"give duration in Months if specified in months" ! exMonth^
	"give duration in Seconds if specified in seconds, < 1 day" ! exSeconds^
	"give duration in Seconds if specified in minutes, < 1 day" ! exMinutes^
	"give duration in Seconds if specified in hours, < 1 day" ! exHours^
	"give duration in Days if specified in days, <= 60 days" ! exDays^
	"give duration in Days if specified in seconds, <= 60 days" ! exDaysSeconds^
	"give duration in Days if specified in minutes, <= 60 days" ! exDaysMinutes^
	"give duration in Days if specified in hours, <= 60 days" ! exDaysHours^
	"give duration in Days if specified in weeks, <= 60 days (8 weeks)" ! exDaysWeeks^
	"give duration in Weeks if specified in weeks, <= 52 weeks" ! exWeeks^ 
  end
  
  def arbInt = arbitrary[Int] suchThat (_ > 0)
  def arbSeconds = Gen.choose(1, 60*60*24 -1 )
  def arbMinutes = Gen.choose(1, 60*24 - 1)
  def arbHours = Gen.choose(1, 24 - 1)
  def arbDays = Gen.choose(1, 60)
  def arbDaysSeconds = Gen.choose(60*60*24, 60*60*24*60)
  def arbDaysMinutes = Gen.choose(60*24, 60*24*60)
  def arbDaysHours = Gen.choose(24, 24*60)
  def arbDaysWeeks = Gen.choose(1, 8)
  def arbWeeks = Gen.choose(9, 12)
  
  def exYear = 1.year.shows must_== "1 Y"
  def exMonth = Prop.forAll(arbInt)((i: Int) => i.months.shows must_== "%d M".format(i))
  def exSeconds = Prop.forAll(arbSeconds)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.seconds
    p.shows must_== "%d S".format(i)
  })
  def exMinutes = Prop.forAll(arbMinutes)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.minutes
    p.shows must_== "%d S".format(i * 60)
  })
  def exHours = Prop.forAll(arbHours)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.hours
    p.shows must_== "%d S".format(i * 60*60)
  })
  def exDays = Prop.forAll(arbDays)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.days
    p.shows must_== "%d D".format(i)
  })
  def exDaysSeconds = Prop.forAll(arbDaysSeconds)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.seconds
    p.shows must_== "%d D".format(i / (60*60*24))
  })
  def exDaysMinutes = Prop.forAll(arbDaysMinutes)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.minutes
    p.shows must_== "%d D".format(i / (60*24))
  })
  def exDaysHours = Prop.forAll(arbDaysHours)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.hours
    p.shows must_== "%d D".format(i / 24)
  })
  def exDaysWeeks = Prop.forAll(arbDaysWeeks)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.weeks
    p.shows must_== "%d D".format(i * 7)
  })
  def exWeeks = Prop.forAll(arbWeeks)((i: Int) => {
    val ri: RI = i
    val p: Period = ri.weeks
    p.shows must_== "%d W".format(i)
  })
}