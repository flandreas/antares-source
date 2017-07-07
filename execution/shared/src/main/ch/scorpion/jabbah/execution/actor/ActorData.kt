package ch.scorpion.jabbah.execution.actor

/**
 * Represents the state of an [Actor] at the beginning of an execution step in order to support stateless [Actor]s.
 *
 * An [Actor] should capture its current state before requesting scheduling. THe [Scheduler] keeps the passed
 * [ActorData] while the requests waits to be scheduled. At the end of an execution step, the [Actor]
 * receives the [ActorData] back in the method [Actor.execute].
 */
interface ActorData {

    /** Returns the data as a textual representation that can be used for logging and tracing.*/
    fun dataToString(): String?
}

class SimpleActorData(val data: String? = null) : ActorData {
    override fun dataToString(): String? = data
}