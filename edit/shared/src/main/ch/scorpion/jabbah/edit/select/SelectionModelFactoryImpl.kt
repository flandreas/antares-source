package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.base.System
import kotlin.reflect.KClass

/**
 * A standard implementation of the [SelectionModelFactory] interface.
 * @property defaultFactories the factories to be used for creating a [SelectionModel] if no suitable factory was registered
 */
class SelectionModelFactoryImpl(
	private val defaultFactories: Map<SelectionDrawingStrategy,((Component) -> SelectionModel<Component>)> = emptyMap()
) : SelectionModelFactory {

	/** Contains the registered [Entries][Entry] for a particular [SelectionDrawingStrategy].*/
	private val registry: MutableMap<SelectionDrawingStrategy, MutableList<Entry>> = mutableMapOf()

	/** ---- [SelectionModelFactory] interface */

	override fun create(component: Component, strategy: SelectionDrawingStrategy): SelectionModel<Component>? {
		val entries = registry[strategy]
		if (entries != null) {
			val entry = entries.firstOrNull { it.componentClassName == System.getClassName(component.selectableComponent) }
			if (entry != null) {
				return entry.factory.invoke(component.selectableComponent)
			}
		}
		return defaultFactories[strategy]?.let { it(component.selectableComponent) }
	}

	override fun register(strategy: SelectionDrawingStrategy, componentClass: KClass<*>, factory: (Component) -> SelectionModel<Component>) {
		registry.getOrPut(strategy) { mutableListOf() }.add(Entry(System.getClassName(componentClass), factory))
	}

	/** ----  [SelectionModelFactoryImpl] */

	private data class Entry(val componentClassName: String, val factory: (Component) -> SelectionModel<Component>)

}