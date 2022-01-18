
import com.example.onlineshoppingcart.shared.Cart
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.OK
import play.api.libs.ws.{DefaultWSCookie, WSClient}
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

class APISpec extends PlaySpec with ScalaFutures with GuiceOneServerPerSuite{

  val baseURL = s"localhost:$port/api"
  val login = s"http://$baseURL/session"
  val productsURL = s"http://$baseURL/products"
  val addProductURL = s"http://$baseURL/product"
  val productsInCartURL = s"http://$baseURL/cart/products"
  def deleteProductInCartURL(productID: String) =
    s"http://$baseURL/cart/products/$productID"
  def actionProductInCartURL(productID: String, quantity: Int) =
    s"http://$baseURL/cart/products/$productID/quantity/$quantity"
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(10, Seconds), interval = Span(1000, Millis))
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  "The API" should {

      val wsClient = app.injector.instanceOf[WSClient]

    lazy val defaultCookie = {
      val loginCookies = Await.result(wsClient.url(login).post("me").map(p=>{
        p.headers.get("Set-Cookie").map(_.head.split(";").head)
      }),1 seconds)

      val play_session = loginCookies.get.split("=").tail.mkString("")
      DefaultWSCookie("PLAY_SESSION", play_session)
    }


    "list all the product" in {

      val response = Await.result(wsClient.url(productsURL).get(),1 second)
      response.status mustBe OK
      response.body must include("PEPPER")
      response.body must include("NAO")
      response.body must include("BEOBOT")
    }

    "add a product" in {
      val newProduct =
        """{"name" : "NewOne","code" : "New","description" : "The brand new product","price" : 100.0}"""
      val posted = wsClient.url(addProductURL).post(newProduct).futureValue
      posted.status mustBe OK

      val response = wsClient.url(productsURL).get().futureValue
      println(response.body)
      response.body must include("NewOne")
    }

    "list all the products in a cart" in {
      // login builds session cookie


      val response = (wsClient.url(productsInCartURL).addCookies(defaultCookie)).get().futureValue

      //println(response)
      response.status mustBe OK

      println(response.body)
      val listOfProduct = decode[Seq[Cart]](response.body)
      //println(listOfProduct.right.get)
      listOfProduct.getOrElse(Seq.empty) mustBe empty

    }

    "add a product in the cart" in {
      val productID = "ALD1"
      val quantity = 1
      val posted = wsClient.url(actionProductInCartURL(productID,quantity)).addCookies(defaultCookie).post("").futureValue
      posted.status mustBe OK

      val response = wsClient.url(productsInCartURL).addCookies(defaultCookie).get().futureValue
      println(response)
      response.status mustBe OK
      response.body must include("ALD1")

    }

    "delete a product from the cart" in {
      val productID = "ALD1"
      val quantity = 1
      val posted = wsClient.url(deleteProductInCartURL(productID))
        .addCookies(defaultCookie).delete().futureValue
      posted.status mustBe OK

      val response = wsClient.url(productsInCartURL).addCookies(defaultCookie).get().futureValue
      println(response)
      response.status mustBe OK
      response.body mustNot include ("ALD1")
    }

    "Update a product quantity in the cart" in {

        val productID = "ALD1"
        val quantity = 1
        val posted = wsClient.url(actionProductInCartURL(productID, quantity)).addCookies(defaultCookie).post("").futureValue

        posted.status mustBe OK

        val newQuantity = 99
        val update = wsClient.url(actionProductInCartURL(productID,
          newQuantity)).addCookies(defaultCookie).put("").futureValue
        update.status mustBe OK

      val response = wsClient.url(productsInCartURL).addCookies(defaultCookie).get().futureValue
      println(response)
      response.status mustBe OK
      response.body must include(productID)
      response.body must include(newQuantity.toString)
    }

    "return a cookie when a user logs in" in {
      val cookieFuture = wsClient.url(login).post("myID").map {
        response => {
          response.headers.get("Set-Cookie").map(
            header => header.head.split(";")
              .filter(_.startsWith("PLAY_SESSION")).head)
        }
      }
      val loginCookies = Await.result(cookieFuture,1 second)
      val play_session_key =  loginCookies.get.split("=").head
      play_session_key must equal("PLAY_SESSION")
    }


  }

}
