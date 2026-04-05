package io.antarescircuit.jabbah.edit.model

import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing

interface DrawingService {

    /**
     * Deletes the specified [Component] from its [Drawing] hold by [drawingView].
     */
    fun delete(components: List<Component>, drawing: Drawing<*>)
}

open class DrawingServiceImpl() : DrawingService {

    override fun delete(components: List<Component>, drawing: Drawing<*>) {
        val componentSet = expandDeleteBuddies(components, drawing)
        drawing.remove(componentSet.map { it.id })
    }

    protected fun expandDeleteBuddies(components: Collection<Component>, drawing: Drawing<*>): Collection<Component> {
        val componentSet: MutableSet<Component> = mutableSetOf()
        componentSet.addAll(components)
        componentSet.addAll(components.map { it.getDeleteBuddies(drawing) }.flatten())
        return componentSet
    }
}