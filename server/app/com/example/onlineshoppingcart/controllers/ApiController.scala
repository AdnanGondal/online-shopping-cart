package com.example.onlineshoppingcart.controllers

import com.google.inject.{Inject, Singleton}
import dao.ProductsDao
import play.api.mvc.{AbstractController, BaseController, ControllerComponents}

@Singleton
class ApiController @Inject()(cc: ControllerComponents, productDao:
ProductsDao) extends AbstractController(cc) with BaseController {
  // *********** CART Controler ******** //
//  def listCartProducts() = ???
//  def deleteCartProduct(id: String) = ???
//  def addCartProduct(id: String, quantity: String) =
//    ???
//  def updateCartProduct(id: String, quantity: String) =
//    ???
  // *********** Product Controler ******** //
  def listProduct() = Action.async { req =>
    TODO(req)
  }
//  def addProduct() = ???
}
