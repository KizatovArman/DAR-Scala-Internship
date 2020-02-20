import actors.FilmActor
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import com.datastax.driver.core.{Cluster, Session}
import models._
import serializers.SprayJsonSerializer

object Boot extends App with SprayJsonSerializer {

  def createSession(ip: String, port: Int, keyspace: String):Session = {
    val buildSession = new Cluster
        .Builder()
        .addContactPoint(ip)
        .withPort(port)
        .build()
        .connect(keyspace)

    buildSession
  }
  val sessionT = createSession("127.0.0.1", 9042, "armantest")
  implicit val system: ActorSystem = ActorSystem("film-service")
  implicit val materializer: Materializer = ActorMaterializer()

  implicit val timeout: Timeout = Timeout(60.seconds)

  val filmActor = system.actorOf(FilmActor.props(sessionT), "filmActor")
  val log = LoggerFactory.getLogger("Boot")

  val route =
    path("healthcheck") {
      get {
        complete {
          "OK"
        }
      }
    } ~
  pathPrefix("filmservice") {
    path("films"/ Segment) {filmId =>
      get {
        complete {
          (filmActor ? FilmActor.GetFilm(filmId.toInt)).mapTo[Either[ErrorResponse, Film]]
        }
      } ~
      delete {
        complete {
          (filmActor ? FilmActor.DeleteFilm(filmId.toInt)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
        }
      }
    } ~
    path("films")
    {
      post {
        entity(as[Film]) { film =>
          complete{
            (filmActor ? FilmActor.CreateFilm(film)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
          }
        }
      } ~
      put {
        entity(as[Film]) { film =>
          complete {
            (filmActor ? FilmActor.UpdateFilm(film)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
          }
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

}
