package controllers

import javax.inject._
import play.api._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

/** This controller creates an `Action` to handle HTTP requests to the valid
  * moves generator.
  */
@Singleton
class TestController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  // TODO: Figure out how to write this instead.
  case class Moves(moves: List[String])

  case class Move(row: Int, col: Int)

  implicit val moveWrites: Writes[Move] =
    (JsPath \ "row").write[Int].and((JsPath \ "col").write[Int])(unlift(Move.unapply))

  def index(): Action[AnyContent] = Action {
    val json = Json.toJson(List(Move(5, 6), Move(3, 4)))

    Ok(json)
  }
}
