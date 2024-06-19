package silvan3r.kotlin_todo

import org.bson.types.ObjectId
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link

interface WithId {
    val id: ObjectId?
}

fun <T: WithId> T.withHateoasLinkToItself(rootUrl: String) =
    EntityModel.of(this, Link.of("${rootUrl}/$id"))
