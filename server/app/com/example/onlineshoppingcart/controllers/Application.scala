package com.example.onlineshoppingcart.controllers

import javax.inject._

import com.example.onlineshoppingcart.shared.SharedMessages
import play.api.mvc._

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

}
