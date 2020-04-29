package ch.scorpion.jabbah.edit

/**
 * A [Drawing] is a container of editable [Component]s.
 */
interface Drawing<T : Component> : ComponentContainer<T>, Bean {
    // empty so far
}