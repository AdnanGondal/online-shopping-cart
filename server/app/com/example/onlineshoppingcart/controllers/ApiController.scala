package com.example.onlineshoppingcart.controllers

import com.google.inject.{Inject, Singleton}
import dao.ProductsDao
import io.circe.generic.auto._, io.circe.syntax._
import io.circe.syntax.EncoderOps
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global


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
//  def addProduct() = ???
}
