package gs.nick

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import gs.nick.server.AkkaHttpImplicits._
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import gs.nick.server.todos.TodosResource

import scala.concurrent.ExecutionContext

// Server definition
class WebServer extends HttpApp {

  implicit val restActorSystem: ActorSystem = ActorSystem(name="todos-api")
  implicit val executionContext: ExecutionContext = restActorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def getPort: Int = {
    val sPort = sys.env.getOrElse("PORT", "8080")
    sPort.toInt
  }

  def getDomain: String = {
    val port = getPort
    val default = s"http://localhost:$port"
    sys.env.getOrElse("DOMAIN", default)
  }


  val todosController = new TodosController(getDomain)

  override def routes: Route = {

    val homeRoutes = pathSingleSlash { get { complete("The server is running :-D ")}}
    val controllerRoutes = TodosResource.routes(todosController)
    val corsRoutes = options { complete(HttpResponse(status = StatusCodes.NoContent))}

    val allowHeader = RawHeader("Access-Control-Allow-Headers", "content-type")
    val allowOrigin = RawHeader("Access-Control-Allow-Origin", "*")
    val allowMethods = RawHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS,PATCH")

    respondWithHeaders(allowHeader, allowOrigin, allowMethods) {
      Route.seal {
        homeRoutes ~ controllerRoutes ~ corsRoutes
      }
    }
  }
}

object App {
  def main(args: Array[String]) = {
    val server = new WebServer
  	val port = server.getPort
    val domain = server.getDomain
    systemDebug()
    println(s"STARTUP domain = $domain")
    println(s"STARTUP port = $port")
    server.startServer("0.0.0.0", port)
    println(s"SHUTDOWN server has exited")
  }

  def systemDebug(): Unit = {
    Seq(
      "----------",
      "System Info:",
      "java.version = " + System.getProperty("java.version"),
      "java.vm.name = " + System.getProperty("java.vm.name"),
      "java.vendor = " + System.getProperty("java.vendor"),
      "java.class.path = " + System.getProperty("java.class.path"),
      "os.name = " + System.getProperty("os.name"),
      "os.arch = " + System.getProperty("os.arch"),
      "os.version = " + System.getProperty("os.version"),
      "----------"
    ).foreach(println)
  }
}
