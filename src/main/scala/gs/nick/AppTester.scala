package gs.nick

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import generated.todoApiClient.AkkaHttpImplicits.HttpClient
import generated.todoApiClient.Client
import generated.todoApiClient.definitions.Todo
import cats.implicits._
import cats.data.EitherT

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

class AppTester(server: String) {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  implicit val system: ActorSystem = ActorSystem()
  val client = buildClient

  def singleRequestClient(): HttpClient = {
    val context = Http().defaultClientHttpsContext
    val settings = ConnectionPoolSettings(system)
    val client = (request: HttpRequest) => Http().singleRequest(request, context, settings)
    client
  }

  def buildClient(): Client = {
    implicit val base: HttpClient = singleRequestClient()
    val client = Client(server)
    client
  }

  def test(): EitherT[Future, Either[Throwable, HttpResponse], String] = {
    val client = buildClient()
    for {
      delete <- client.deleteAllTodos()
      _ <- client.addTodo(Todo(title = Some("The 1st todo!")))
      second <- client.addTodo(Todo(title = Some("The 2nd todo!")))
      all <- client.getTodoList()
    } yield {
      all.fold(
        handleOK = (result) => {
          if (result.length == 2) {
            "Worked as expected"
          } else {
            s"Failed, i deleted all and added two, but result as size ${result.length}"
          }
        },
        handleInternalServerError = {
          "error! all bad!"
        }
      )
    }
  }

}

object AppTester {
  def main(args: Array[String]): Unit = {
    implicit val ec = ExecutionContext.global
    val host = args.headOption.getOrElse("https://todo-backend-guardrail.herokuapp.com")
    println(s"I WILL TEST $host")
    val test = new AppTester(host)
    test.test().value.onComplete { r =>
      println(s"result: $r")
      System.exit(1)
    }
  }
}
