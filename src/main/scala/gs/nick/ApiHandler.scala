package gs.nick

import java.util.UUID

import gs.nick.server.definitions.Todo
import gs.nick.server.todos.{TodosHandler, TodosResource => TodosResource}

import scala.concurrent.Future

class TodosDao {
  var all: IndexedSeq[Todo] = IndexedSeq.empty
  def reset(): Unit = { all = IndexedSeq.empty }
  def find(x: String): Option[Todo] = {
    all.find(t => t.id.isDefined && t.id.get == x)
  }
}

class TodosController(val domain: String) extends TodosHandler {

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

  override def getTodoList(respond: TodosResource.getTodoListResponse.type)(): Future[TodosResource.getTodoListResponse] = {
    println("GET todo list")
    Future.successful {
      respond.OK(todosDao.all)
    }
  }

  override def addTodo(respond: TodosResource.addTodoResponse.type)(newTodo: Todo): Future[TodosResource.addTodoResponse] = {
    println("POST add todo")
    Future.successful {
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

  override def getTodoById(respond: TodosResource.getTodoByIdResponse.type)(todoId: String): Future[TodosResource.getTodoByIdResponse] = {
    println("GET todo by ID " + todoId)
    Future.successful {
      val item = todosDao.find(todoId)
      if (item.isDefined) {
        respond.OK(item.get)
      } else {
        println(s"can not find ID: $todoId")
        respond.NotFound
      }
    }
  }

  override def updateTodoById(respond: TodosResource.updateTodoByIdResponse.type)(todoId: String, newTodo: Todo): Future[TodosResource.updateTodoByIdResponse] = {
    println("PATCH update todo by id " + todoId)
    Future.successful {
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

  override def deleteAllTodos(respond: TodosResource.deleteAllTodosResponse.type)(): Future[TodosResource.deleteAllTodosResponse] = {
    println("DELETE list of todos")
    Future.successful {
      todosDao.reset()
      respond.OK
    }
  }

  override def deleteTodoById(respond: TodosResource.deleteTodoByIdResponse.type)(todoId: String): Future[TodosResource.deleteTodoByIdResponse] = {
    println("DELETE one todo " + todoId)
    Future.successful {
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
