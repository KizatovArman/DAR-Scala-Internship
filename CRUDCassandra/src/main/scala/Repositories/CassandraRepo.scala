package Repositories

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSink, CassandraSource}
import akka.stream.scaladsl.{RunnableGraph, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import com.datastax.driver.core.{PreparedStatement, Row, Session, SimpleStatement}
import models._

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object CassandraRepo {
  case class performSelect(id: Int)
  case class performTransform(film: Film)
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

//  val sink: Sink[Film, NotUsed] = ???
//
//  val streamQueue: SourceQueueWithComplete[Film] = {
//
//    // Source of OutgoingMessage queue
//    val rabbitOutgoingSource: Source[Film, SourceQueueWithComplete[Film]] =
//      Source.queue[Film](30, OverflowStrategy.backpressure)
//
//    // graph
//    val graph: RunnableGraph[SourceQueueWithComplete[Film]] = rabbitOutgoingSource.to(sink)
//
//    // run graph - SourceQueue[OutgoingMessage]
//    graph.run()
//  }
  val source: Source[Film, SourceQueueWithComplete[Film]] = Source.queue[Film](Int.MaxValue, OverflowStrategy.backpressure)
  def Insert(film: Film) = {
    val films = Seq[Film](film).toList
    val statementBinder = (myFilm: Film, statement: PreparedStatement) => statement.bind(film)
    val sink = CassandraSink[Film](parallelism = 2, preparedInsertStatement, statementBinder)(uSession)
//    source.runWith(sink)
    val result: Future[Done] = Source(films)
      .map {
        filmI =>
          log.info(s"Film Found: ${filmI}")
          filmI
      }
      .runWith(sink)
    result
}


  def SelectQuery = CassandraSource(selectStatement)(uSession).runWith(Sink.seq)

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
      val result: Future[Done] = Insert(film)
      result.onComplete {
        case Success(value: Done) =>
          filmActorSender ! Right(SuccessfulResponse(201, "Successfully created film!"))
          log.info("CassandraRepo: successfully made performTransform request!")
        case Failure(exception) =>
          filmActorSender ! Left(ErrorResponse(500, s"Failed to create film! Internal server error!"))
          log.error(s"CassandraRepo: error message: ${exception.getMessage}, error cause: ${exception.getCause}")
      }
      log.info("FINISHED request from FILM ACTOR")
  }
}
