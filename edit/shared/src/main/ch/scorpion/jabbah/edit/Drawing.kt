package ch.scorpion.jabbah.edit

/**
 * A [Drawing] is a container of editable [Component]s.
 */
interface Drawing<T : Component> : ComponentContainer<T> {

    /** Disposes this [Drawing] and all inner object when it is not used any more. */
    fun dispose()
}