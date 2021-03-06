package controllers

import play.api.mvc._
import scaldi.{Injectable, Injector}

class ComponentTemplateController(implicit inj: Injector) extends Controller with Injectable {

  def index() = Action {
    Ok(views.html.angular.components())
  }

}