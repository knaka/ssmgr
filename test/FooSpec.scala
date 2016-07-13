import org.scalatestplus.play._
// import play.api.test._
// import play.api.test.Helpers._

class FooSpec extends PlaySpec {
  "The string" should {
    "be equal to something" in {
      val s = "Hello"
      s mustBe "Hello"
    }
  }
}
