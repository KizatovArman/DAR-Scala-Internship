package Repositories

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSink, CassandraSource}
import akka.stream.scaladsl.{RunnableGraph, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import com.datastax.driver.core._
import models._
import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object CassandraRepo {
  case class performSelect(id: Int)
  case class performTransform(film: Film)
  case class performUpdate(film: Film)
  case class performDelete(id: Int)

  def props(uSession: Session) = Props(new CassandraRepo(uSession))
}

class CassandraRepo(uSession: Session) extends Actor with ActorLogging{

  import CassandraRepo._
  implicit val system: ActorSystem = ActorSystem("cassandra-repo-actor")
  implicit val materializer: Materializer = ActorMaterializer()

  // Function to filter films by ID.
  def compareIDs(row: Row, id: Int): Boolean = {
    if(row.getInt("f_id") == id) true
    else false
  }

  // Statement type:
  // [1]-Select, [2]-Insert, [3]-Update, [4]-Delete
  val selectStatement = new SimpleStatement("SELECT * FROM films;").setFetchSize(20) //[1]
  val preparedInsertStatement = uSession.prepare("INSERT INTO films(f_id, f_title, year_of_publication) VALUES(?, ?, ?);")//[2]
  val preparedUpdateStatement = uSession.prepare("UPDATE films SET f_title = ?, year_of_publication = ? WHERE f_id = ?;")//[3]
  def preparedDeleteStatement(id:Int) = new SimpleStatement(s"DELETE FROM films WHERE f_id = ${id}")

//  val source: Source[Film, SourceQueueWithComplete[Film]] = Source.queue[Film](Int.MaxValue, OverflowStrategy.backpressure)

  def SelectQuery = CassandraSource(selectStatement)(uSession).runWith(Sink.seq)

  def Insert(film: Film) = {
    val films = Seq[Film](film).toList
    val statementBinder = (myFilm: Film, statement: PreparedStatement) => statement.bind(myFilm.f_id.asInstanceOf[Object], myFilm.f_title, myFilm.year_of_publication.asInstanceOf[Object])
    val sink = CassandraSink[Film](parallelism = 2, preparedInsertStatement, statementBinder)(uSession)
    val result: Future[Done] = Source(films)
      .map {
        filmI =>
          log.info(s"Film Found: ${filmI}")
          filmI
      }
      .runWith(sink)
    result
  }

  def Update(film: Film) = {
    val films = Seq[Film](film).toList
    val statementBinder = (myFilm: Film, statement: PreparedStatement) => statement.bind(myFilm.f_title, myFilm.year_of_publication.asInstanceOf[Object], myFilm.f_id.asInstanceOf[Object])
    val sink = CassandraSink[Film](parallelism = 2, preparedUpdateStatement, statementBinder)(uSession)
    val result = Source(films)
      .map {
        filmI =>
          log.info(s"Films Found: ${filmI}")
          filmI
      }
      .runWith(sink)
    result
  }

  def Delete(id: Int) = {
    val pStatement = preparedDeleteStatement(id)
    CassandraSource(pStatement)(uSession).runWith(Sink.ignore)
  }

  override def receive: Receive = {
    case performSelect(id: Int) =>
      val filmActorSender = sender()
      SelectQuery.onComplete {
        case Success(rows: immutable.Seq[Row]) =>
          val row = rows.filter(p => compareIDs(p, id))
          if(row.length > 0){
            filmActorSender ! Right(
              Film(
                row(0).getInt("f_id"),
                row(0).getString("f_title"),
                row(0).getInt("year_of_publication")))
          } else {
            filmActorSender ! Left(ErrorResponse(404, "Not found!"))
          }
        case Failure(exception) =>
          filmActorSender ! Left(ErrorResponse(500, s"Internal server error happend on Cassandra Actor side! Error: ${exception.getMessage}"))
      }

    case performTransform(film: Film) =>
      log.info("GOT request from FILM ACTOR")
      val filmActorSender = sender()
      SelectQuery.onComplete {
        case Success(value: immutable.Seq[Row]) =>
          val row = value.filter(p => compareIDs(p, film.f_id))
          if(row.length > 0) {
            filmActorSender ! Left(ErrorResponse(509, "Such film is already in database, use PUT request to change this film!"))
            log.info("CassandraRepo: tried to CREATE FILM, but it is already in database.")
          } else {
            val result: Future[Done] = Insert(film)
            result.onComplete {
              case Success(value: Done) =>
                filmActorSender ! Right(SuccessfulResponse(201, "Successfully created film!"))
                log.info("CassandraRepo: successfully made performTransform request!")
              case Failure(exception) =>
                filmActorSender ! Left(ErrorResponse(500, s"Failed to create film! Internal server error!"))
                log.error(s"CassandraRepo: error message: ${exception.getMessage}, error cause: ${exception.getCause}")
            }
          }
        case Failure(exception) =>
          filmActorSender ! Left(ErrorResponse(500, s"Failed to create film! Internal server error!"))
          log.error(s"CassandraRepo: error message: ${exception.getMessage}, error cause: ${exception.getCause}")
      }
      log.info("FINISHED request from FILM ACTOR")

    case performUpdate(film: Film) =>
      log.info("GOT message from FILM ACTOR")
      val filmActorSender = sender()
      SelectQuery.onComplete {
        case Success(value) =>
          val row = value.filter(p => compareIDs(p, film.f_id))
          if(row.length > 0) {
            val result = Update(film)
            result.onComplete {
              case Success(value) =>
                filmActorSender ! Right(SuccessfulResponse(200, "Successfully updated films!"))
                log.info("CassandraRepo: successfully made performUpdate request!")
              case Failure(exception) =>
                filmActorSender ! Left(ErrorResponse(500, "Failed to update film! Internal server error!"))
                log.error(s"CassandraRepo: error message: ${exception.getMessage}")
            }
          } else {
            filmActorSender ! Left(ErrorResponse(404, "There is no such film!"))
            log.info("CassandraRepo: tried to UPDATE FILM, but there is no such film in database.")
          }
        case Failure(ex) =>
          filmActorSender ! Left(ErrorResponse(500, s"Failed to update film! Internal server error!"))
          log.error(s"CassandraRepo: error message: ${ex.getMessage}, error cause: ${ex.getCause}")
      }

    case performDelete(id: Int) =>
      val filmActorSender = sender()
      SelectQuery.onComplete {
        case Success(value) =>
          val row = value.filter(p => compareIDs(p, id))
          if(row.length > 0) {
            val result: Future[Done] = Delete(id)
            result.onComplete {
              case Success(d: Done) =>
                filmActorSender ! Right(SuccessfulResponse(200, s"Successfully deleted film with ID: ${id}!"))
                log.info("CassandraRepo: successfully made performDelete request!")
              case Failure(exception) =>
                filmActorSender ! Left(ErrorResponse(500, "Failed to delete film! Internal server error!"))
                log.error(s"CassandraRepo: error message: ${exception.getMessage}")
            }
          } else {
            filmActorSender ! Left(ErrorResponse(404, "There is no such film to delete!"))
            log.info("CassandraRepo: tried to UPDATE FILM, but there is no such film in database.")
          }
        case Failure(ex) =>
          filmActorSender ! Left(ErrorResponse(500, s"Failed to update film! Internal server error!"))
          log.error(s"CassandraRepo: error message: ${ex.getMessage}, error cause: ${ex.getCause}")
      }
  }
}
