package silvan3r.kotlin_todo

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.hateoas.EntityModel
import org.springframework.web.reactive.function.server.*
import org.springframework.web.server.ServerWebInputException
import silvan3r.kotlin_todo.RequestParamError.PathIdHasWrongFormat
import java.net.URI

val handlerLogger = KotlinLogging.logger {}

fun ServerRequest.extractIdFromPath() = try {
    ObjectId(pathVariable("id")).right()
} catch (e: IllegalArgumentException) {
    PathIdHasWrongFormat.left()
}

suspend inline fun <reified T : Any> ServerRequest.extractBody() =
    try {
        awaitBodyOrNull<T>()
            ?.right()
            ?: RequestError.BodyIsMissing.left()
    } catch (e: ServerWebInputException) {
        handlerLogger.warn { e }
        RequestError.BodyIsInvalidJson(T::class).left()
    }

suspend fun <T> EntityModel<T>.responseCreated(rootUrl: String) where T : WithId =
    ServerResponse.created(URI("${rootUrl}/${content!!.id}"))
        .json()
        .bodyValueAndAwait(this)

suspend fun Flow<Any>.responseOk() =
    ServerResponse.ok().json().bodyAndAwait(this)

suspend fun Any.responseOk() =
    ServerResponse.ok().json().bodyValueAndAwait(this)

suspend fun <T> Either<RequestError, T>.foldServerResponse(ifRight: suspend (T) -> ServerResponse): ServerResponse =
    fold({ errors -> errors.responseError() }, { ifRight(it) })

suspend fun RequestError.responseError(): ServerResponse {
    handlerLogger.warn { this }
    return when (this) {
        is RequestParamError ->
            ServerResponse.badRequest().json().bodyValueAndAwait(this)
        is RequestError.BodyIsInvalidJson ->
            ServerResponse.badRequest().json().bodyValueAndAwait(this)
        RequestError.BodyIsMissing ->
            ServerResponse.badRequest().json().bodyValueAndAwait(this)
    }
}


suspend fun responseNoContent() = ServerResponse.noContent().buildAndAwait()
