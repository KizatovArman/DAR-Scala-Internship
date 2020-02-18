import java.util
import java.util._

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity
import akka.stream.alpakka.cassandra.scaladsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContextExecutor
//import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.alpakka.cassandra.scaladsl.{CassandraFlow, CassandraSource}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import com.datastax.driver.core._
import com.google.common.util.concurrent._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshalling.Marshal
import com.typesafe.config.{Config}
import java.util._



object Boot extends App {

  case class Film(f_id: Int, f_title: String, year_of_publication: Int)

  val cluster = new Cluster
  .Builder()
    .addContactPoint("127.0.0.1")
    .withPort(9042)
    .build()

  implicit val session = cluster.connect("armantest")
  implicit val system: ActorSystem = ActorSystem("online-film-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val query = "SELECT * FROM films WHERE f_id = 1;"
  val query2 = "INSERT INTO films(f_id, f_title, year_of_publication) VALUES(002, 'Iron Man', 2008);"

  val fut:Future[Iterable[Row]] = Future[Iterable[Row]] {
    session.executeAsync(query).get().asScala
  }

  fut.onComplete {
    case Success(value: Iterable[Row]) =>
      println(value.toList(0))
    case Failure(exception) =>
      println(exception.getMessage)
  }
  val statement = new Statement {}


//  println(cluster.isClosed)

//  val stmt = new SimpleStatement(s"SELECT * FROM films;").setFetchSize(20)
//  val ss: Future[ResultSet] = Future[ResultSet] {
//    CassandraSource(stmt).asInstanceOf[ResultSet]
//  }
//  ss.onComplete {
//    case Success(sourceRows: ResultSet) =>
//      for(r: Row <- sourceRows.asScala.toList) {
//        val filmId = r.getInt("f_id")
//        val filmTitle = r.getString("f_title")
//        val filmYear = r.getInt("year_of_publication")
//        println(s"FilmID: ${filmId}, Film Title: ${filmTitle}, FilmYear: ${filmYear}")
//      }
//
//    case Failure(exception) =>
//      println(exception.getMessage)
//  }
//  session.
//  rows.onComplete{
//    case Success(allrows: ResultSet) =>
//      for(r: Row <- allrows.asScala.toList) {
//        val filmId = r.getInt("f_id")
//        val filmTitle = r.getString("f_title")
//        val filmYear = r.getInt("year_of_publication")
//        println(s"FilmID: ${filmId}, Film Title: ${filmTitle}, FilmYear: ${filmYear}")
//      }
//    case Failure(exception) =>
//      println(exception.getMessage)
//  }



  //  stmt.onComplete {
//    case Success(value) =>
//      val rows: Source[Row, NotUsed] = CassandraSource(value)
//      println(rows)
//
//      println(rows.map(r => new Film(r.getInt("f_id"), r.getString("f_title"), r.getInt("year_of_publication"))))
//      println(rows.asJava.asScala.map(r => new Film(r.getInt("f_id"), r.getString("f_title"), r.getInt("year_of_publication"))))
//
//    case Failure(exception) =>
//      println(exception.getMessage)
//  }

//  println(rows)



//  val preparedStatement: PreparedStatement = session.prepare(query)
//  val statementBinder = (myInteger: Int, statement: PreparedStatement) => statement.bind(myInteger)
//  val sink = CassandraSink[Int](parallelism = 2, preparedStatement, statementBinder)
//
//  //  println(session.isClosed)
//  println(session.execute(query2))
//  val result: ResultSetFuture = session.executeAsync(query)
//  val listResult = result.get().asScala.toList
//  for(r:Row <- listResult) {
//    val filmId = r.getInt("f_id")
//    val filmTitle = r.getString("f_title")
//    val filmYear = r.getInt("year_of_publication")
//    println(s"FilmID: ${filmId}, Film Title: ${filmTitle}, FilmYear: ${filmYear}")
//  }


  // String interpolation
//  implicit class CqlStrings(val context: StringContext) extends AnyVal {
//    def cql(args: Any*)(implicit session: Session): Future[PreparedStatement] = {
//      val statement = new SimpleStatement(context.raw(args: _*))
//      session.prepareAsync(statement)
//    }
//  }
//
//  implicit def listenableFutureToFuture[T](listenableFuture: ListenableFuture[T]):
//    Future[T] = {
//    val promise = Promise[T]()
//    Futures.addCallback(listenableFuture, new FutureCallback[T] {
//      def onFailure(error: Throwable) = {
//        promise.failure(error)
//      }
//      def onSuccess(result: T) = {
//        promise.success(result)
//      }
//    })
//    promise.future
//  }
//
//  def execute(statement: Future[PreparedStatement], params: Any*)(executionContext: scala.concurrent.ExecutionContext, session: Session):
//  Future[ResultSet] = statement.map(_.bind(params.map(_.asInstanceOf[Object]))).flatMap(session.executeAsync(_))
//
//  implicit val session = new Cluster
//      .Builder()
//    .addContactPoint("localhost")
//    .withPort(9042)
//    .build()
//    .connect()
//
//  val statement = cql"SELECT * FROM my_keyspace.my_table WHERE my_key = ?"
//  val myKey = 3
//  val resultSet = execute(cql"SELECT * FROM my_keyspace.my_table", myKey)
////  val rows: Future[Iterable[Row]] = resultSet.map(_.asScala)
//  println(resultSet)

}
