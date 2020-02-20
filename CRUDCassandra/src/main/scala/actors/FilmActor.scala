package actors

import akka.pattern.ask
import scala.language.implicitConversions
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer, Outlet, SourceShape}
import com.datastax.driver.core._
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import models._
import Repositories.CassandraRepo
import akka.util.Timeout


object FilmActor {
  case class CreateFilm(film: Film)
  case class GetFilm(id: Int) // by id
  case class UpdateFilm(fim: Film)
  case class DeleteFilm(id: Int)
  def props(uSession: Session) = Props(new FilmActor(uSession))
}

class FilmActor(uSession: Session) extends Actor with ActorLogging {

  import FilmActor._
  implicit val system: ActorSystem = ActorSystem("online-film-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(20.seconds)

  val cassandraRepo = system.actorOf(CassandraRepo.props(uSession), "cassandra-repo")

  def receive: Receive = {
    case CreateFilm(film: Film) =>
      log.info("Got request from BOOT")
      val realSender = sender()
      val eitherFuture: Future[Either[ErrorResponse, SuccessfulResponse]] = (cassandraRepo ? CassandraRepo.performTransform(film)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
      eitherFuture.onComplete {
        case Success(value: Either[ErrorResponse, SuccessfulResponse]) =>
          if(value.isLeft) {
            realSender ! Left(value.left.get)
          } else {
            realSender ! Right(value.right.get)
          }
        case Failure(exception: Throwable) =>
          realSender ! Left(ErrorResponse(500, s"Internal server error happend on Film Actor side! Error: ${exception.getMessage}"))
      }
      log.info("Finished request from BOOT")

    case GetFilm(id: Int) =>
      val realSender = sender()
      val eitherFuture: Future[Either[ErrorResponse, Film]] = (cassandraRepo ? CassandraRepo.performSelect(id)).mapTo[Either[ErrorResponse, Film]]
      eitherFuture.onComplete {
        case Success(value: Either[ErrorResponse, Film]) =>
          if(value.isLeft) {
            realSender ! Left(value.left.get)
          } else {
            realSender ! Right(value.right.get)
          }
        case Failure(exception: Throwable) =>
          realSender ! Left(ErrorResponse(500, s"Internal server error happend on Film Actor side! Error: ${exception.getMessage}"))
      }

    case UpdateFilm(film: Film) =>
      val realSender = sender()
      val eitherFuture: Future[Either[ErrorResponse, SuccessfulResponse]] = (cassandraRepo ? CassandraRepo.performUpdate(film)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
      eitherFuture.onComplete {
        case Success(value: Either[ErrorResponse, SuccessfulResponse]) =>
          if(value.isLeft) {
            realSender ! Left(value.left.get)
          } else {
            realSender ! Right(value.right.get)
          }
        case Failure(exception: Throwable) =>
          realSender ! Left(ErrorResponse(500, s"Internal server error happend on Film Actor side! Error: ${exception.getMessage}"))
      }

    case DeleteFilm(id: Int) =>
      val realSender = sender()
      val eitherFuture: Future[Either[ErrorResponse, SuccessfulResponse]] = (cassandraRepo ? CassandraRepo.performDelete(id)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
      eitherFuture.onComplete {
        case Success(value: Either[ErrorResponse, SuccessfulResponse]) =>
          if(value.isLeft) {
            realSender ! Left(value.left.get)
          } else {
            realSender ! Right(value.right.get)
          }
        case Failure(exception: Throwable) =>
          realSender ! Left(ErrorResponse(500, s"Internal server error happend on Film Actor side! Error: ${exception.getMessage}"))
      }
  }
}


/// GET ATTEMPTS:

//      val uQuery: Statement = createQuery(2, tempFilm).setFetchSize(20)
//      val rows = CassandraSource(uQuery)(uSession).runWith(Sink.seq)
//      rows.onComplete {
//        case Success(value: immutable.Seq[Row]) =>
//          val gg: immutable.Seq[Row] = value.filter(p => compareIDs(p, id))
//          if(gg.length > 0) {
//            val r = gg(0)
//            realSender ! Right(Film(r.getInt("f_id"), r.getString("f_title"), r.getInt("year_of_publication")))
//          } else realSender ! Left(ErrorResponse(404, s"Not found!"))
//        case Failure(exception) =>
//          log.error(exception.getMessage)
//          realSender ! Left(ErrorResponse(500, s"Internal server error: ${exception.getMessage}"))
//
//      }
//      result.map{ result => s"Response found ${result}"}.recover{
//        case ex: Exception =>
//          log.error(ex.getMessage)
//          throw ex
//      }
//      val fut: Future[Row] = Future[Row]{
//        uSession.executeAsync(uQuery).get().asScala
//        uSession.execute(uQuery).one()
//      }
//      fut.onComplete {
//        case Success(value: Row) =>
//          val rowList = value
//          println(rowList)
//          val filmID = rowList(0).getInt("f_id")
//          val filmTitle = rowList(0).getString("f_title")
//          val filmYear = rowList(0).getInt("year_of_publication")
//          realSender ! Right(SuccessfulResponse(200, s"${filmID}, ${filmTitle}, ${filmYear}"))
//        case Failure(exception) =>
//          realSender ! Left(ErrorResponse(404, "Such film doesn't exists. We are sorry :("))
//          log.error(s"Error: Film with ID: ${id} doesn't contained in database.")
//      }

/// GET ATTEMPTS;

/// POST ATTEMPTS:

//      val uQuery = createQuery(1, film)
//      val preparedStatement = uSession.prepare(s"INSERT INTO films(f_id, f_title, year_of_publication) VALUES(?, ?, ?);")
//      val statementBuild = (myFilm: Film, statement: PreparedStatement) => statement.bind(film)
//      val sink = CassandraSink[Film](parallelism = 2, preparedStatement, statementBuild)(uSession)
//      val source = Source(List[Film])
//      val gg = source.runWith(sink)
//      gg.onComplete {
//        case Success(value) =>
//          println(value)
//          realSender ! Right(SuccessfulResponse(200, "CREATED!"))
//        case Failure(exception) =>
//          realSender ! Left(ErrorResponse(500, exception.getMessage))
//      }


//      val cassandraResponse = uSession.executeAsync(uQuery).asInstanceOf[Future[ResultSet]]
//      cassandraResponse.onComplete {
//        case Success(value: ResultSet) =>
//          realSender ! Right(SuccessfulResponse(201, s"Film: ${film.f_title}, with ID: ${film.f_id} has successfully been created."))
//          log.info(s"Successfully added '${film.f_title}' Film, with ID: ${film.f_id}.")
//        case Failure(exception) =>
//          realSender ! Left(ErrorResponse(500, s"Internal server error :(. ${exception.getMessage}"))
//          log.info(s"Error: ${exception.getCause.toString}, ${exception.getMessage}")
//
//      }

/// POST ATTEMPTS;