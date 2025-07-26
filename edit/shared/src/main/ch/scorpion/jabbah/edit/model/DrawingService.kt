package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing

interface DrawingService {

    /**
     * Deletes the specified [Component] from its [Drawing] hold by [drawingView].
     */
    fun delete(components: List<Component>, drawing: Drawing<Component>)
}

open class DrawingServiceImpl() : DrawingService {

    override fun delete(components: List<Component>, drawing: Drawing<Component>) {
        val componentSet = expandDeleteBuddies(components, drawing)
        drawing.remove(componentSet.map { it.id })
    }

    protected fun expandDeleteBuddies(components: Collection<Component>, drawing: Drawing<Component>): Collection<Component> {
        val componentSet: MutableSet<Component> = mutableSetOf()
        componentSet.addAll(components)
        componentSet.addAll(components.map { it.getDeleteBuddies(drawing) }.flatten())
        return componentSet
    }
}