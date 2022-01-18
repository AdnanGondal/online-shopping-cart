package com.example.onlineshoppingcart.controllers

import com.google.inject.{Inject, Singleton}
import dao.ProductsDao
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, BaseController, ControllerComponents}
import com.example.onlineshoppingcart.shared.Product
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class ApiController @Inject()(cc: ControllerComponents, productsDao:
ProductsDao) extends AbstractController(cc) with BaseController with Circe {
  // *********** CART Controler ******** //
//  def listCartProducts() = ???
//  def deleteCartProduct(id: String) = ???
//  def addCartProduct(id: String, quantity: String) =
//    ???
//  def updateCartProduct(id: String, quantity: String) =
//    ???
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
