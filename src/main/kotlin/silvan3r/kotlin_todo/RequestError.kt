package silvan3r.kotlin_todo

import kotlin.reflect.KClass

sealed class RequestError(val msg: String) {
    object BodyIsMissing : RequestError("body is missing")

    class BodyIsInvalidJson(clazz: KClass<*>) :
        RequestError("body is not valid json for type $clazz")
}

sealed class RequestParamError(msg: String): RequestError(msg) {
    object PathIdHasWrongFormat: RequestParamError("id has wrong format")
}
