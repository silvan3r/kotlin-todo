package silvan3r.kotlin_todo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter
import silvan3r.kotlin_todo.todo.TodoHandler

@Configuration
class Router(private val todoHandler: TodoHandler) {
    @Bean
    fun routes() = coRouter {
        accept(APPLICATION_JSON).nest {
            val todosRoot = "/todos"
            todosRoot.nest {
                GET("") { todoHandler.getAll(todosRoot) }
                POST("") { todoHandler.insert(it, todosRoot) }
                PUT("") { todoHandler.insert(it, todosRoot) }
                "/{id}".nest {
                    GET("", todoHandler::get)
                    DELETE("", todoHandler::delete)
                    PUT("") { todoHandler.update(it, todosRoot) }
                }
            }
        }
    }
}
