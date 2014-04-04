package crakken.controllers

import play.modules.reactivemongo.MongoController
import play.api.mvc.{Action, Controller}

object ApplicationController extends Controller with MongoController{
  def index = Action {
    Ok(views.html.index())
  }

  def login = Action {
    Ok(views.html.index())
  }
}
