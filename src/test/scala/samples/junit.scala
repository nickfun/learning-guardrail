package samples


import scala.concurrent.duration.Duration
import org.junit._
import Assert._
import gs.nick.TodosController
import gs.nick.server.definitions.Todo

import scala.concurrent.Await

@Test
class AppTest {


  @Test
  def testMerge(): Unit = {

    def foramtterId(x: String): String = x
    val controller = new TodosController(foramtterId)

    val t1 = Todo(Some("ID"), Option("first"), None, None, None)
    val t2 = Todo(Some("second"), Option("second"), None, None, Option("URL123"))

    val result = controller.mergeTodo(t1, t2)
    assertTrue(t2.id.get == result.id.get)
    assertTrue(t2.title.get == result.title.get)
    assertTrue(result.url.get == t2.url.get)
  }

}


