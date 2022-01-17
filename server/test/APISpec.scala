import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

class APISpec extends PlaySpec with ScalaFutures with GuiceOneServerPerSuite{
  val baseURL = s"localhost:$port/api"
  val productsURL = s"http://$baseURL/products"
  val addProductURL = s"http://$baseURL/product"
  val productsInCartURL = s"http://$baseURL/cart/products"
  def deleteProductInCartURL(productID: String) =
    s"http://$baseURL/cart/products/$productID"
  def actionProductInCartURL(productID: String, quantity: Int) =
    s"http://$baseURL/cart/products/$productID/quantity/$quantity"

  "The API" should {

      val wsClient = app.injector.instanceOf[WSClient]

    "list all the product" in {

      val response = Await.result(wsClient.url(productsURL).get(),1 second)
      response.status mustBe Ok
    }

    "add a product" in {
      val newProduct =
        """
 {
 "name" : "NewOne",
 "code" : "New",
 "description" : "The brand new product",
 "price" : 100.0
 }
"""
      val posted = wsClient.url(addProductURL).post(newProduct).futureValue
      posted.status mustBe Ok
    }

    "add a product in the cart" in {
      val productID = "ALD1"
      val quantity = 1
      val posted = wsClient.url(actionProductInCartURL(productID,quantity)).post("").futureValue
      posted.status mustBe OK
    }

    "delete a product from the cart" in {
      val productID = "ALD1"
      val quantity = 1
      val posted = wsClient.url(deleteProductInCartURL(productID))
        .delete().futureValue
      posted.status mustBe OK
    }

    "Update a product quantity in the cart" in {
      "update a product quantity in the cart" in {
        val productID = "ALD1"
        val quantity = 1
        val posted = wsClient.url(actionProductInCartURL(productID,
          quantity))
          .post("").futureValue

        posted.status mustBe OK

        val newQuantity = 99
        val update = wsClient.url(actionProductInCartURL(productID,
          newQuantity)).put("").futureValue
        update.status mustBe OK

      }
    }


  }

}
