package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Port

/**
 * Created by andreas on 19.02.17.
 */
class NetActorData<S: Any>(val signal: S?, override val changedPort: Port<S>) : GraphActorData {

    override fun <T : Any> getSignal(portId: Int): T? {
        return signal as T?
    }

    override fun dataToString(): String? {
        return signal?.toString()
    }
}