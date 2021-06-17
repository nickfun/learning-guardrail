package gs.nick

import java.util.UUID

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import generated.server.{Handler => TodosHandler}
import generated.server.{Resource => TodosResource}
import generated.server.definitions.Todo

class TodosDao {
  var all: Vector[Todo] = Vector.empty
  def reset(): Unit = { all = Vector.empty }
  def find(x: String): Option[Todo] = {
    all.find(p => p.id.contains(x))
  }
}

class TodosController(val domain: String)(implicit val ec: ExecutionContext) extends TodosHandler {

  val todosDao = new TodosDao

  def mergeTodos(t1: Todo, t2: Todo): Todo = {
    val id = List(t2.id, t1.id).flatten.headOption
    val title = List(t2.title, t1.title).flatten.headOption
    val order = List(t2.order, t1.order).flatten.headOption
    val completed = List(t2.completed, t1.completed).flatten.headOption
    val url = List(t2.url, t1.url).flatten.headOption
    Todo(id, title, order, completed, url)
  }

  def getUrl(id: String): String = {
    s"$domain/todos/$id"
  }


  override def getTodoList(respond: TodosResource.GetTodoListResponse.type )(): Future[TodosResource.GetTodoListResponse] = {
    println("GET todo list")
    Future {
      respond.OK(todosDao.all)
    }
  }

  override def addTodo(respond: TodosResource.AddTodoResponse.type)(newTodo: Todo): Future[TodosResource.AddTodoResponse] = {
    println("POST add todo")
    Future {
      val newId = UUID.randomUUID().toString
      val url = getUrl(newId)
      var x = newTodo.copy(id = Option(newId), url = Option(url))
      if (x.completed.isEmpty) {
        x = x.copy(completed = Option(false))
      }
      todosDao.all = todosDao.all :+ x
      respond.OK(x)
    }
  }

  override def getTodoById(respond: TodosResource.GetTodoByIdResponse.type)(todoId: String): Future[TodosResource.GetTodoByIdResponse] = {
    println("GET todo by ID " + todoId)
    Future {
      val item = todosDao.find(todoId)
      if (item.isDefined) {
        respond.OK(item.get)
      } else {
        println(s"can not find ID: $todoId")
        respond.NotFound
      }
    }
  }

  override def updateTodoById(respond: TodosResource.UpdateTodoByIdResponse.type)(todoId: String, newTodo: Todo): Future[TodosResource.UpdateTodoByIdResponse] = {
    println("PATCH update todo by id " + todoId)
    Future {
      val item = todosDao.find(todoId)
      if (item.isEmpty) {
        respond.NotFound
      } else {
        val withoutItem = todosDao.all.filterNot(_.id.getOrElse("_") == todoId)
        val updatedTodo = mergeTodos(item.get, newTodo)
        todosDao.all = withoutItem :+ updatedTodo
        respond.OK(updatedTodo)
      }
    }
  }

  override def deleteAllTodos(respond: TodosResource.DeleteAllTodosResponse.type)(): Future[TodosResource.DeleteAllTodosResponse] = {
    println("DELETE list of todos")
    Future {
      todosDao.reset()
      respond.OK
    }
  }

  override def deleteTodoById(respond: TodosResource.DeleteTodoByIdResponse.type)(todoId: String): Future[TodosResource.DeleteTodoByIdResponse] = {
    println("DELETE one todo " + todoId)
    Future {
      val item = todosDao.find(todoId)
      if (item.isEmpty) {
        respond.NotFound
      } else {
        todosDao.all = todosDao.all.filterNot { item =>
          item.id.isDefined && item.id.get == todoId
        }
        respond.OK
      }
    }
  }
}
