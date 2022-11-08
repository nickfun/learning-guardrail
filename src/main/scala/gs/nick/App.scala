package gs.nick

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader

import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import generated.server.AkkaHttpImplicits._
import generated.server.{Resource => TodosResource}

import scala.concurrent.ExecutionContext

// Server definition
// see https://doc.akka.io/docs/akka-http/current/routing-dsl/HttpApp.html
class WebServer extends HttpApp {

  implicit val restActorSystem: ActorSystem = ActorSystem(name="todos-api")
  implicit val executionContext: ExecutionContext = restActorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val port: Int = {
    val sPort = sys.env.getOrElse("PORT", "8080")
    sPort.toInt
  }

  val domain: String = {
    val default = s"localhost:$port"
    sys.env.getOrElse("DOMAIN", default)
  }

  val useHttps: Boolean = {
    sys.env.get("ENABLE_HTTPS").map(e => e.toUpperCase() == "ON").getOrElse(false)
  }

  val todosController = new TodosController(domain, useHttps)

  override def routes: Route = {

    val homeRoutes = pathSingleSlash { get { complete("The server is running :-D ")}}
    val controllerRoutes = TodosResource.routes(todosController)
    val corsRoutes = options { complete(HttpResponse(status = StatusCodes.NoContent))}
    val custom404 = complete(404, "404 resource not found on my sweet server")
    val debugRoute = path("debug") { get { complete(App.systemInfo(domain, port, useHttps)) } }

    val allowHeader = RawHeader("Access-Control-Allow-Headers", "content-type")
    val allowOrigin = RawHeader("Access-Control-Allow-Origin", "*")
    val allowMethods = RawHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS,PATCH")

    respondWithHeaders(allowHeader, allowOrigin, allowMethods) {
      Route.seal {
        homeRoutes ~ controllerRoutes ~ corsRoutes ~ debugRoute ~ custom404
      }
    }
  }
}

object App {
  def main(args: Array[String]) = {
    val server = new WebServer
  	val port = server.port
    val domain = server.domain
    systemDebug(domain, port, server.useHttps)
    server.startServer("0.0.0.0", port)
    println(s"SHUTDOWN server has exited")
    System.exit(0)
  }

  def systemDebug(domain: String, port: Int, useHttps: Boolean): Unit = {
    val info = systemInfo(domain, port, useHttps)
    println(mapToStringTable(info))
  }

  def mapToStringTable(items: Map[String, String]): String = {
    val len: Int = items.keySet.maxBy(_.length).length
    val fmt = s"%-${len}s %s"
    items.toSeq.sortBy(x => x._1).map { case (key, value) =>
      String.format(fmt, key, value)
    }.mkString("\n")
  }

  def systemInfo(domain: String, port: Int, useHttps: Boolean): Map[String, String] = {
    Map[String, String](
      "java.version" -> System.getProperty("java.version"),
      "java.vm.name" -> System.getProperty("java.vm.name"),
      "java.vendor" -> System.getProperty("java.vendor"),
      "java.class.path" -> System.getProperty("java.class.path"),
      "os.name" -> System.getProperty("os.name"),
      "os.arch" -> System.getProperty("os.arch"),
      "os.version" -> System.getProperty("os.version"),
      "Domain" -> domain,
      "Port" -> port.toString,
      "Use HTTPs" -> useHttps.toString,
    )
  }
}
