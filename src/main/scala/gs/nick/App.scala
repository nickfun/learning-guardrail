package gs.nick

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import gs.nick.server.AkkaHttpImplicits._
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import gs.nick.server.todos.TodosResource

import scala.concurrent.ExecutionContext

// Server definition
object WebServer extends HttpApp {
  implicit val restActorSystem: ActorSystem = ActorSystem(name="todos-api")

  implicit val executionContext: ExecutionContext = restActorSystem.dispatcher
  // Counterpart which materializes streams for the REST endpoints
  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(restActorSystem).withSupervisionStrategy { ex =>
      println("akka supervisor got an error, will now restart", ex)
      Supervision.restart
    }
  )

  def getPort: Int = {
    val sPort = sys.env.getOrElse("PORT", "8080")
    sPort.toInt
  }

  def getDomain: String = {
    val port = getPort
    val default = s"http://localhost:$port"
    sys.env.getOrElse("DOMAIN", default)
  }

  def defaultUrlFormatter(id: String): String = {
    val domain = getDomain
    s"$domain/todos/$id"
  }

  val todosController = new TodosController(defaultUrlFormatter)

  override def routes: Route = {
    respondWithHeaders(RawHeader("Access-Control-Allow-Origin", "*"), RawHeader("Access-Control-Allow-Methods", "GET,HEAD,POST,DELETE,OPTIONS,PUT,PATCH")) {
      path("/") {
        get {
          complete("The server is running :-D")
        }
      } ~ TodosResource.routes(todosController)
    }
  }
}

object App {
  def main(args: Array[String]) = {
  	val port = WebServer.getPort
    val domain = WebServer.getDomain
    println(s"STARTUP  domain = $domain")
    println(s"STARTUP  port = $port")
    println(s"STARTUP server will bind to port $port")
    WebServer.startServer("0.0.0.0", port)
    println(s"STARTDOWN server is past running")
  }
}
