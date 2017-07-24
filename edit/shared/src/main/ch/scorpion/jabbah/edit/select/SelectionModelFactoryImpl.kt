package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger

/**
 * A standard implementation of the [SelectionModelFactory] interface.
 */
class SelectionModelFactoryImpl : SelectionModelFactory {

    private val LOG by logger(SelectionModelFactoryImpl::class)

    /** Contains the registered [Entries][Entry] for a particular [SelectionDrawingStrategy].*/
    private val registry: MutableMap<SelectionDrawingStrategy, MutableList<Entry>> = mutableMapOf()

    /** ---- [SelectionModelFactory] interface */

    override fun create(component: Component, strategy: SelectionDrawingStrategy): SelectionModel<Component>? {
        val entries = registry.get(strategy)
        if (entries != null) {
            val entry = entries
                .filter { it.componentClassName == System.SYSTEM!!.getClassName(component.selectableComponent) }
                .firstOrNull()
            if (entry != null) {
                return entry.factory.invoke(component.selectableComponent)
            }
        }
        LOG.debug("No suitable SelectionModel found for ${System.SYSTEM!!.getClassName(component.selectableComponent)}")
        return null
    }

    override fun register(strategy: SelectionDrawingStrategy, componentClassName: String, factory: (Component) -> SelectionModel<Component>) {
        registry.getOrPut(strategy, { mutableListOf()}).add(Entry(componentClassName, factory))
    }

    /** ----  [SelectionModelFactoryImpl] */

    private data class Entry(val componentClassName: String, val factory: (Component) -> SelectionModel<Component>)

}