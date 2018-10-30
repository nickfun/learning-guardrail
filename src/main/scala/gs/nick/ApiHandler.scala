package gs.nick

import java.util.UUID

import gs.nick.server.definitions.Todo
import gs.nick.server.todos.{TodosHandler, TodosResource => TodosResource}

import scala.concurrent.Future

object TodosDao {
  var all: IndexedSeq[Todo] = IndexedSeq.empty
  def reset(): Unit = { all = IndexedSeq.empty }
  def find(x: String): Option[Todo] = {
    all.find(t => t.id.isDefined && t.id.get == x)
  }
}

class TodosController extends TodosHandler {
  override def getTodoList(respond: TodosResource.getTodoListResponse.type)(): Future[TodosResource.getTodoListResponse] = {
    Future.successful {
      respond.OK(TodosDao.all)
    }
  }

  override def addTodo(respond: TodosResource.addTodoResponse.type)(newTodo: Todo): Future[TodosResource.addTodoResponse] = {
    Future.successful {
      val u = UUID.randomUUID()
      val x = newTodo.copy(id = Option(u.toString))
      TodosDao.all = TodosDao.all :+ x
      respond.OK(x)
    }
  }

  override def getTodoById(respond: TodosResource.getTodoByIdResponse.type)(todoId: String): Future[TodosResource.getTodoByIdResponse] = {
    Future.successful {
      val item = TodosDao.find(todoId)
      if (item.isDefined) {
        respond.OK(item.get)
      } else {
        println(s"can not find ID: $todoId")
        respond.NotFound
      }
    }
  }

  def mergeTodo(t1: Todo, t2: Todo): Todo = {t2}

  override def updateTodoById(respond: TodosResource.updateTodoByIdResponse.type)(todoId: String, newTodo: Todo): Future[TodosResource.updateTodoByIdResponse] = {
    Future.successful {
      val item = TodosDao.find(todoId)
      if (!item.isDefined) {
        respond.NotFound
      } else {
        val withoutItem = TodosDao.all.filterNot(_.id.getOrElse("_") == todoId)
        val updatedTodo = mergeTodo(item.get, newTodo)
        TodosDao.all = withoutItem :+ updatedTodo
        respond.OK(updatedTodo)
      }
    }
  }
}
