package gs.nick.tests

import akka.http.scaladsl.testkit.ScalatestRouteTest
import generated.server.AkkaHttpImplicits._
import generated.server.definitions.Todo
import gs.nick.{TodosController, WebServer}
import org.scalatest.funspec.AnyFunSpec


import scala.collection._

// Testing an Akka HTTP app notes at https://doc.akka.io/docs/akka-http/current/routing-dsl/testkit.html#table-of-inspectors
class AppTestSuite extends AnyFunSpec with ScalatestRouteTest {

  val corsHeaders: Seq[String] = Seq(
    "Access-Control-Allow-Headers",
    "Access-Control-Allow-Origin",
    "Access-Control-Allow-Methods"
  )

  def generateServer: WebServer = {
    new gs.nick.WebServer
  }

  describe("Basic routes") {
    val routes = generateServer.routes

    it("responds to root when running") {
      Get("/") ~> routes ~> check {
        assert(status.intValue === 200)
        assert(true === responseAs[String].contains("server is running"))
      }
    }
  }

  describe("Merge of Todos") {
    val controller = new TodosController("http://localhost")
    it("second param takes priority") {
      val t1 = Todo(title=Option("title 1"))
      val t2 = Todo(title=Option("title 2"))
      val t3 = controller.mergeTodos(t1, t2)
      assert(t2.title.get === t3.title.get)
    }

    it("first takes priority if second is None") {
      val t1 = Todo(title=Option("title 1"))
      val t2 = Todo()
      val t3 = controller.mergeTodos(t1, t2)
      assert(t1.title.get === t3.title.get)
    }
  }

  describe("Supports CORS headers") {
    val routes = generateServer.routes

    it("supports CORS headers even on 404 routes") {
      Get("/not/found/should/404") ~> routes ~> check {

        corsHeaders.foreach { name =>
          val headerOption = header(name)
          assert(true === headerOption.isDefined)
        }
        assert(404 === status.intValue)
      }
    }

    it("supports an OPTIONS request to get the headers") {
      Options("/todos") ~> routes ~> check {
        assert(204 === status.intValue)
        corsHeaders.foreach { name =>
          val headerOption = header(name)
          assert(true === headerOption.isDefined)
        }
      }
    }
  }

  describe("Get and list TODOs") {
    val routes = generateServer.routes

    it("lists of TODO starts empty") {
      Get("/todos") ~> routes ~> check {
        assert(200 === status.intValue())
        val list = responseAs[IndexedSeq[Todo]]
        assert(true === list.isEmpty)
      }
    }

    it("can add a TODO and sets the ID") {
      val newTodo = Todo(title = Option("I am example 1"))
      Post("/todos", newTodo) ~> routes ~> check {
        assert(200 === status.intValue)
        val returnedTodo = responseAs[Todo]
        assert(true === returnedTodo.id.isDefined)
        assert(newTodo.title.get === returnedTodo.title.get)
      }
    }

    it("can add a TODO and sets the URL") {
      val newTodo = Todo(title = Option("I am example 1"))
      Post("/todos", newTodo) ~> routes ~> check {
        assert(200 === status.intValue)
        val returnedTodo = responseAs[Todo]
        assert(true === returnedTodo.url.isDefined)
        assert(newTodo.title.get === returnedTodo.title.get)
      }
    }

    it("can add a TODO and sets checked to False") {
      val newTodo = Todo(title = Option("I am example 1"))
      Post("/todos", newTodo) ~> routes ~> check {
        assert(200 === status.intValue)
        val returnedTodo = responseAs[Todo]
        assert(true === returnedTodo.completed.isDefined)
        assert(false === returnedTodo.completed.get)
      }
    }

    it("Adds three Todos and lists three back") {
      val routes = generateServer.routes
      val newTodo = Todo(title = Option("I am example 1"))
      Get("/todos") ~> routes ~> check {
        val list = responseAs[IndexedSeq[Todo]]
        assert(true === list.isEmpty)
      }
      Post("/todos", newTodo) ~> routes
      Post("/todos", newTodo) ~> routes
      Post("/todos", newTodo) ~> routes
      Get("/todos") ~> routes ~> check {
        val list = responseAs[IndexedSeq[Todo]]
        assert(3 === list.size)
      }
    }
  }

  describe("manipulate server todos") {
    val routes = generateServer.routes

    it("PATCH for update") {
      val v1 = Todo(title=Option("First Version"))

      Post("/todos", v1) ~> routes ~> check {

        assert(200 === status.intValue)
        val returnedTodo = responseAs[Todo]
        assert(v1.title.get === returnedTodo.title.get)
        assert(true === returnedTodo.url.isDefined)
        val v2 = returnedTodo.copy(title=Option("It has changed"), completed=Option(true))

        Patch(v2.url.get, v2) ~> routes ~> check {

          assert(200 === status.intValue)
          val updatedTodo = responseAs[Todo]
          assert(v2.title.get === updatedTodo.title.get)
        }

        Get(returnedTodo.url.get) ~> routes ~> check {
          val checkingTodo = responseAs[Todo]
          assert(200 === status.intValue)
          assert(true === checkingTodo.completed.get)
        }

      }
    }
  }

}
