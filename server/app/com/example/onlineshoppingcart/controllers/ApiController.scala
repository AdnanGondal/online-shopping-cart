package com.example.onlineshoppingcart.controllers

import com.google.inject.{Inject, Singleton}
import dao.{CartsDao, ProductsDao}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, BaseController, ControllerComponents, Result}
import com.example.onlineshoppingcart.shared.{Cart, Product, ProductInCart}
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class ApiController @Inject()(cc: ControllerComponents, productsDao:
ProductsDao, cartsDao: CartsDao) extends AbstractController(cc) with BaseController with Circe {

  val recoverError: PartialFunction[Throwable, Result] = {
    case e => {
      logger.error("Database writing error",e)
      InternalServerError("Cannot write in the database")
    }
  }

  def login() = Action { request =>
    request.body.asText match {
      case None => BadRequest
      case Some(user) => Ok.withSession("user" -> user)
    }
  }
  // *********** CART Controler ******** //
 def listCartProducts() = Action.async { req =>
   val userOption = req.session.get("user")

   userOption match {
     case Some(user) => {
       logger.info(s"User '$user' is asking for the list of product in the cart")
       val futureInsert = cartsDao.cart4(user)

       futureInsert.map(products => {
         Ok(products.asJson)
       }).recover(recoverError)
     }
     case None => Future.successful(Unauthorized)
   }
 }
  def deleteCartProduct(id: String) = Action.async {request =>
    val userOption = request.session.get("user")
    userOption match {
      case Some(user) => {
        logger.info(s"User '$user' is asking to delete the product'$id' from the cart")
        val futureInsert = cartsDao.remove(ProductInCart(user, id))
        futureInsert.map(_ => Ok).recover(recoverError)
      }
      case None => Future.successful(Unauthorized)
    }
  }

  def addCartProduct(id: String, quantity: String) = Action.async{ req =>
    val userOption = req.session.get("user")

    userOption match{
      case Some(user) => {
        val futureInsert = cartsDao.insert(Cart(user,id,quantity.toInt))
        futureInsert.map(_ => Ok).recover(recoverError)
      }
      case None => Future.successful(Unauthorized)
    }

  }


  def updateCartProduct(id: String, quantity: String) = Action.async { request =>
      val userOption = request.session.get("user")
    print("we are hereee...."+userOption)
      userOption match {
        case Some(user) => {
          logger.info(s"User '$user' is updating the product'$id' in it is cart with a quantity of '$quantity")
          val futureInsert = cartsDao.update(Cart(user, id,
            quantity.toInt))
          futureInsert.map(_ => Ok).recover(recoverError)
        }
        case None => Future.successful(Unauthorized)
      }
  }

  // *********** Product Controler ******** //
  def listProduct() = Action.async { req =>
    val futureProducts = productsDao.all()
    for (
      products <- futureProducts
    ) yield (Ok(products.asJson))

  }
  def addProduct() = Action.async( request =>
    {
      val productOrNot = decode[Product](request.body.asText.getOrElse(""))

      productOrNot match {
        case Right(product) => {
          val futureInsert = productsDao.insert(product).recover {
            case e => {
              logger.error("Error while writing in the database",e)
              InternalServerError("Cannot Write in database")
            }
          }
          futureInsert.map(_ => Ok)
        }
        case Left(error) => {
          logger.error("Error while adding a product",error)
          Future.successful(BadRequest)
        }
      }

    }

  )
}
