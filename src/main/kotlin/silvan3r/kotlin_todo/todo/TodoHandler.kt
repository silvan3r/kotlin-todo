package silvan3r.kotlin_todo.todo

import arrow.core.raise.either
import arrow.core.right
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mu.KLogging
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import silvan3r.kotlin_todo.*

@Component
class TodoHandler {

    companion object : KLogging()

    private val todos = mutableListOf(
        Todo(ObjectId.get(), false, "firstTodo")
    )

    private suspend fun ObjectId.deleteTodo() = todos.removeIf { it.id == this }

    suspend fun getAll(req: ServerRequest, rootPath: String) =
        either {
            todos.asFlow().map { it.withHateoasLinkToItself(rootPath) }.right().bind()
        }.foldServerResponse { todos -> todos.responseOk() }

    suspend fun get(req: ServerRequest) =
        either {
            val id = req.extractIdFromPath().bind()
            todos.asFlow().first { it.id == id }.right().bind()
        }.foldServerResponse { todo -> todo.responseOk() }

    suspend fun insert(req: ServerRequest, rootPath: String) =
        either {
            // a new ObjectId is always created
            val todo = req.extractBody<Todo>().bind().copy(id = ObjectId.get())
            todos.add(todo)
            todo.withHateoasLinkToItself(rootPath)
        }.foldServerResponse { todo -> todo.responseCreated(rootPath) }

    suspend fun delete(req: ServerRequest) =
        either {
            val id = req.extractIdFromPath().bind()
            id.deleteTodo()
        }.foldServerResponse { responseNoContent() }

    suspend fun update(req: ServerRequest, rootPath: String): ServerResponse =
        either {
            val id = req.extractIdFromPath().bind()
            val todo = req.extractBody<Todo>().bind().copy(id = id)
            val exists = todos.any { it.id == id }
            if (exists) { // this could be accomplished via mongo upsert
                todos.replaceAll { if (it.id === id) todo else it }
            } else {
                todos.add(todo)
            }
            Pair(exists, todo.withHateoasLinkToItself(rootPath))
        }.foldServerResponse { (existed, todo) ->
            if (existed) todo.responseOk() else todo.responseCreated(rootPath)
        }

}
