package gs.nick

import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import generated.todoApiClient.AkkaHttpImplicits.HttpClient
import generated.todoApiClient.{AddTodoResponse, Client}
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

  def getTodo(input: AddTodoResponse): EitherT[Future, Either[Throwable, HttpResponse], Todo] = {
    val myError: Either[Throwable, HttpResponse] = Left(new Exception("could not get ID"))
    val optionResult: Option[Todo] = input match {
      case AddTodoResponse.OK(value) => Some(value)
      case AddTodoResponse.BadRequest(value) => None
    }
    EitherT[Future, Either[Throwable, HttpResponse], Todo](Future.successful {
      optionResult.toRight(myError)
    })
  }

  def test(): EitherT[Future, Either[Throwable, HttpResponse], String] = {
    val NEW_TITLE = "hello world I am different title"
    val client = buildClient()
    for {
      delete <- client.deleteAllTodos()
      _ <- client.addTodo(Todo(title = Some("The 1st todo!")))
      secondResponse <- client.addTodo(Todo(title = Some("The 2nd todo!")))
      secondTodo <- getTodo(secondResponse)
      all <- client.getTodoList()
      updatedSecond <- client.updateTodoById(secondTodo.id.get, secondTodo.copy(title=Some(NEW_TITLE)))
    } yield {
      updatedSecond.fold(
        handleOK = (todo) => {
          if (todo.title.contains(NEW_TITLE)) {
            println("Success on edit of todo")
            "good"
          } else {
            println("Error on edit of todo!")
            println(todo)
            "bad"
          }
        },
        handleNotFound =   {
          println("Failed to edit the todo, I got a 404 instead!")
          "bad"
        }
      )
    }
  }

}

object AppTester {
  def main(args: Array[String]): Unit = {
    implicit val ec = ExecutionContext.global
    val host = args.headOption.getOrElse("http://localhost:8080/")
    println(s"I WILL TEST $host")
    val test = new AppTester(host)
    test.test().value.onComplete { r =>
      println(s"result: $r")
      System.exit(1)
    }
  }
}
