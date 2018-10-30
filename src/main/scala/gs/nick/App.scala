package gs.nick

import akka.actor.ActorSystem
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

  override def routes: Route =
    TodosResource.routes(todosController)
}

object App {
  def main(args: Array[String]) = {
  	val port = WebServer.getPort
    println(s"\nSERVER WILL BIND $port")
    WebServer.startServer("localhost", port)
  }
}
