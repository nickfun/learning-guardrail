package gs.nick.tests

import gs.nick.WebServer

import scala.collection._
import org.scalatest._
import org.junit.Test
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._
import gs.nick.server.definitions.Todo
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import gs.nick.server_tests.AkkaHttpImplicits._



@RunWith(classOf[JUnitRunner])
class AppTestSuite extends FunSpec with ScalatestRouteTest {

  val corsHeaders = Seq(
    "Access-Control-Allow-Headers",
    "Access-Control-Allow-Origin",
    "Access-Control-Allow-Methods"
  )

  def getServer(): WebServer = {
    new gs.nick.WebServer
  }

  describe("Basic routes") {
    val routes = getServer().routes

    it("responds to root when running") {
      Get("/") ~> routes ~> check {
        assert(status.intValue() === 200)
        assert(true === responseAs[String].contains("is running"))
      }
    }
  }

  describe("Supports CORS headers") {
    val routes = getServer().routes

    it("supports CORS headers even on 404 routes") {
      Get("/nothing/should/404") ~> routes ~> check {

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
      }
    }
  }

  describe("Get and list TODOs") {
    val routes = getServer().routes

    it("lists of TODO starts empty") {
      Get("/todos") ~> routes ~> check {
        assert(200 === status.intValue())
        val list = responseAs[IndexedSeq[Todo]]
        assert(true === list.isEmpty)
      }
    }

    it("can add a TODO and sets the ID") {
      val newTodo = Todo(title=Option("I am example 1"))
      Post("/todos", newTodo) ~> routes ~> check {
        assert(200 === status.intValue)
        val returnedTodo = responseAs[Todo]
        assert(true === returnedTodo.id.isDefined)
        assert(newTodo.title.get === returnedTodo.title.get)
      }
    }
  }

}
