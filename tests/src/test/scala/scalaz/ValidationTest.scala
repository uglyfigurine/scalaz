package scalaz

import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalazArbitrary._

class ValidationTest extends Spec {
  import std.AllInstances._

  checkAll("Validation", order.laws[Validation[Int, Int]])
  checkAll("FailureProjection", order.laws[FailureProjection[Int, Int]])

  type ValidationInt[A] = Validation[Int, A]
  type FailureProjectionInt[A] = FailureProjection[Int, A]

  checkAll("Validation", semigroup.laws[ValidationInt[Int]])
  checkAll("Validation", plus.laws[ValidationInt])
  checkAll("Validation", applicative.laws[ValidationInt])
  checkAll("Validation", traverse.laws[ValidationInt])
  checkAll("Validation", bifunctor.laws[Validation])

  checkAll("FailureProjection", semigroup.laws[FailureProjectionInt[Int]])
  checkAll("FailureProjection", plus.laws[FailureProjectionInt])
  checkAll("FailureProjection", applicative.laws[FailureProjectionInt])
  checkAll("FailureProjection", traverse.laws[FailureProjectionInt])

  checkAll("FailureProjection", bifunctor.laws[FailureProjection])

  "fpoint and point" in {

    import syntax.pointed._
    import std.AllInstances._
    import Validation._


    val vi = success[String, Int](0)

    val voi: Validation[String, Option[Int]] = vi map (Some(_))
    val ovi: Option[Validation[String, Int]] = vi.point[Option]
    voi must be_===(success[String, Option[Int]](Some(0)))
    ovi must be_===(Some(vi))

    {
      import syntax.functor._
      val voi2: Validation[String, Option[Int]] = vi.fpoint[Option]
      voi2 must be_===(success[String, Option[Int]](Some(0)))
    }
  }

  "show" in {
    import syntax.show._
    Validation.success[String, Int](0).shows must be_===("Success(0)")
    Validation.failure[String, Int]("fail").shows must be_===("Failure(fail)")
  }

  "example" in {
    import std.AllInstances._
    import syntax.functor._
    import std.string.stringSyntax._
    val x = "0".parseBoolean.failure.fpair
    ok
  }
  
  "ap2" should {
    "accumulate failures in order" in {
      import syntax.show._
      val fail1 = Failure("1").toValidationNEL
      val fail2 = Failure("2").toValidationNEL
      val f = (_:Int) + (_:Int)
      Apply[({type l[a] = ValidationNEL[String, a]})#l].ap2(fail1, fail2)(Success(f)).shows must be_===("Failure([1,2])")
    }
  }

  "map2" should {
    "accumulate failures in order" in {
      import syntax.show._
      val fail1 = Failure("1").toValidationNEL
      val fail2 = Failure("2").toValidationNEL
      val f = (_:Int) + (_:Int)
      Apply[({type l[a] = ValidationNEL[String, a]})#l].map2(fail1, fail2)(f).shows must be_===("Failure([1,2])")
    }
  }

  object instances {
    def show[E: Show, A: Show] = Show[Validation[E, A]]
    def equal[E: Equal, A: Equal] = Equal[Validation[E, A]]
    def order[E: Order, A: Order] = Order[Validation[E, A]]
    def pointed[E] = Pointed[({type λ[α]=Validation[E, α]})#λ]
    def semigroup[E: Semigroup, A] = Semigroup[Validation[E, A]]
    def applicative[E: Semigroup] = Applicative[({type λ[α]=Validation[E, α]})#λ]
    def traverse[E: Semigroup] = Traverse[({type λ[α]=Validation[E, α]})#λ]
    def plus[E: Semigroup] = Plus[({type λ[α]=Validation[E, α]})#λ]
    def bitraverse = Bitraverse[Validation]

    // checking absence of ambiguity
    def equal[E: Order, A: Order] = Equal[Validation[E, A]]
    def pointed[E: Semigroup] = Pointed[({type λ[α] = Validation[E, α]})#λ]

    object failProjection {
      def show[E: Show, A: Show] = Show[FailureProjection[E, A]]
      def equal[E: Equal, A: Equal] = Equal[FailureProjection[E, A]]
      def order[E: Order, A: Order] = Order[FailureProjection[E, A]]
      def pointed[E] = Pointed[({type λ[α]=FailureProjection[E, α]})#λ]
      def semigroup[E: Semigroup, A] = Semigroup[FailureProjection[E, A]]
      def applicative[E: Semigroup] = Applicative[({type λ[α]=FailureProjection[E, α]})#λ]
      def traverse[E: Semigroup] = Traverse[({type λ[α]=FailureProjection[E, α]})#λ]
      def plus[E: Semigroup] = Plus[({type λ[α]=FailureProjection[E, α]})#λ]
      def bitraverse = Bitraverse[FailureProjection]

      // checking absence of ambiguity
      def equal[E: Order, A: Order] = Equal[FailureProjection[E, A]]
      def pointed[E: Semigroup] = Pointed[({type λ[α]=FailureProjection[E, α]})#λ]
    }
  }
}
