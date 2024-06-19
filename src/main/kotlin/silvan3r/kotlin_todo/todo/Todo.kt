package silvan3r.kotlin_todo.todo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import silvan3r.kotlin_todo.WithId

data class Todo(
    @Id
    // @JsonIgnore
    override val id: ObjectId? = null,
    val done: Boolean = false,
    val name: String,
) : WithId
