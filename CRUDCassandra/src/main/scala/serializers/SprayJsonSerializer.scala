package serializers

import models._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait SprayJsonSerializer extends DefaultJsonProtocol{
  implicit val successfulFormat:RootJsonFormat[SuccessfulResponse] = jsonFormat2(SuccessfulResponse)
  implicit val errorFormat:RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse)

  implicit val filmFormat:RootJsonFormat[Film] = jsonFormat3(Film)
}
