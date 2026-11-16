package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Maps beans (components and circuits) to the [WebProperty]s shown in the web editor's inspector,
 * mirroring the desktop's BeanInfo classes.
 *
 * All registrations matching a bean are combined in registration order: a later property with the same key
 * replaces an earlier one, and [WebPropertyBuilder.remove] hides earlier ones. Hence base classes are registered
 * first (like `super.addProperties(...)` in BeanInfos), then subclasses.
 */
internal object WebPropertyRegistry {

	class Contribution(val properties: List<WebProperty<Any, Any?>>, val removals: Set<String> = emptySet())

	private class Entry(val matches: (Any) -> Boolean, val contribute: (Any) -> Contribution)

	private val entries = mutableListOf<Entry>()

	/** Registers [properties] for beans of type [T]. Registration order defines the display order. */
	@Suppress("UNCHECKED_CAST")
	inline fun <reified T : Any> register(noinline properties: (T) -> List<WebProperty<T, *>>) =
		register({ it is T }) { properties(it as T) as List<WebProperty<Any, Any?>> }

	fun register(matches: (Any) -> Boolean, properties: (Any) -> List<WebProperty<Any, Any?>>) =
		addEntry(matches) { Contribution(properties(it)) }

	/**
	 * Registers properties for beans of type [T] (optionally restricted by [matches]) using a [WebPropertyBuilder],
	 * which is created per bean so that conditions can be evaluated like in BeanInfos.
	 */
	@Suppress("UNCHECKED_CAST")
	inline fun <reified T : Any> define(
		noinline matches: (T) -> Boolean = { true },
		noinline build: WebPropertyBuilder<T>.(T) -> Unit,
	) = addEntry({ it is T && matches(it) }) { bean ->
		val builder = WebPropertyBuilder<T>().apply { build(bean as T) }
		Contribution(builder.properties as List<WebProperty<Any, Any?>>, builder.removals)
	}

	fun addEntry(matches: (Any) -> Boolean, contribute: (Any) -> Contribution) {
		entries.add(Entry(matches, contribute))
	}

	fun propertiesOf(bean: Any): List<WebProperty<Any, Any?>> {
		ensureInitialized()
		val result = mutableListOf<WebProperty<Any, Any?>>()
		entries.filter { it.matches(bean) }.forEach { entry ->
			val contribution = entry.contribute(bean)
			result.removeAll { it.key in contribution.removals }
			contribution.properties.forEach { p ->
				result.removeAll { it.key == p.key }
				result.add(p)
			}
		}
		return result
	}

	/** The inspector title: the translated component type (e.g. "AND gate"), or the circuit's name. */
	fun titleOf(bean: Any): String = when (bean) {
		is GraphView -> RichText.stripToPlainText(bean.graph?.name?.value ?: "")
		is Component -> bean.type.takeIf { it.isNotEmpty() } ?: bean::class.simpleName ?: ""
		is Drawing<*> -> RichText.stripToPlainText(bean.name.value)
		else -> bean::class.simpleName ?: ""
	}

	private var initialized = false

	private fun ensureInitialized() {
		if (initialized) return
		initialized = true
		registerGraphProperties()
		registerParamProperties()
		registerFigureProperties()
		registerDigitalProperties()
		registerAnalogProperties()
		registerFsmProperties()
	}
}
