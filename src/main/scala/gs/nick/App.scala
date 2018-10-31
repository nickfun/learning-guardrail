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


  val todosController = new TodosController(getDomain)

  override def routes: Route = {
    val allowHeader = RawHeader("Access-Control-Allow-Headers", "content-type")
    val allowOrigin = RawHeader("Access-Control-Allow-Origin", "*")
    val allowMethods = RawHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS,PATCH")
    respondWithHeaders(allowHeader, allowOrigin, allowMethods) {
      Route.seal {
        // add some helper routes that are not part of the spec
        pathSingleSlash {
          get {
            complete("The server is running :-D")
          }
        // add a response for all OPTIONS requests to the browser pre-flight checks will pass
        } ~ options {
          complete(HttpResponse(status = StatusCodes.NoContent))
        // add the routes defined in our swagger spec
        } ~ TodosResource.routes(todosController)
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
    println(s"STARTUP server will bind to port $port")
    server.startServer("0.0.0.0", port)
    println(s"SHUTDOWN server has exited")
  }

  def systemDebug(): Unit = {
    println("System Info:");
    println("java.version = " + System.getProperty("java.version"));
    println("java.vm.name = " + System.getProperty("java.vm.name"));
    println("java.vendor = " + System.getProperty("java.vendor"));
    println("java.class.path = " + System.getProperty("java.class.path"));
    println("os.name = " + System.getProperty("os.name"));
    println("os.arch = " + System.getProperty("os.arch"));
    println("os.version = " + System.getProperty("os.version"));
    println("----------");
  }
}
