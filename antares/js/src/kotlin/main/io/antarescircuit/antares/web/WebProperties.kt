package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Editor

/** Describes this property of [bean] as JSON object for the web application. */
internal fun <B : Any, V> WebProperty<B, V>.describe(bean: B): dynamic {
	val property = this
	val value = get(bean)
	val choices = choices?.invoke(bean)
	val choice = choices?.firstOrNull { it.value == value }
	val readOnly = isReadOnly
	val typeId = (if (readOnly) WebPropertyType.ReadOnly else type).id
	val jsValue = when {
		choices == null -> property.toJs(value)
		readOnly -> choice?.label
		else -> choice?.id
	}
	return obj {
		this.key = property.key
		this.label = property.label
		this.type = typeId
		this.group = property.group
		this.value = jsValue
		this.choices = if (readOnly) null else choices?.map { c ->
			obj {
				this.id = c.id
				this.label = c.label
				this.color = c.color
			}
		}?.toJsArray()
		this.min = property.min
		this.max = property.max
	}
}

/**
 * Describes and changes the properties of the current selection, or of the edited circuit if nothing is selected.
 */
internal class WebProperties(
	private val editor: () -> Editor,
	private val symbolEditor: () -> Editor,
	private val isSymbolView: () -> Boolean,
) {
	companion object {
		private val LOG by logger(WebProperties::class)
	}

	private val activeEditor get() = if (isSymbolView()) symbolEditor() else editor()

	/** The beans whose properties are displayed: the selected components, or the drawing itself. */
	private fun beans(e: Editor): List<Any> {
		val selection = e.view.selectionManager.selection
		return if (selection.isEmpty()) listOf(e.drawing) else selection.map { it.propertyOwner }
	}

	/**
	 * Properties shared by all [beans], described for the first bean. A property is shared if every bean has an
	 * editable property with the same key and type. Choices are limited to those all beans offer.
	 */
	private fun commonProperties(beans: List<Any>): List<WebProperty<Any, Any?>> {
		val first = beans.firstOrNull() ?: return emptyList()
		val firstProps = WebPropertyRegistry.propertiesOf(first)
		if (beans.size == 1) return firstProps
		val others = beans.drop(1).map { b -> WebPropertyRegistry.propertiesOf(b).filter { !it.isReadOnly }.associateBy { it.key } }
		return firstProps.filter { p -> !p.isReadOnly && others.all { it[p.key]?.type == p.type } }
	}

	/** The choice ids offered by [property] of every one of [beans], or `null` if it has no choices. */
	private fun commonChoiceIds(beans: List<Any>, key: String): Set<String>? =
		beans.map { bean -> propertyOf(bean, key)?.choices?.invoke(bean)?.map { it.id }?.toSet() ?: return null }
			.reduce { a, b -> a intersect b }

	private fun propertyOf(bean: Any, key: String) = WebPropertyRegistry.propertiesOf(bean).firstOrNull { it.key == key }

	fun describe(): String {
		val e = activeEditor
		val beans = beans(e)
		val first = beans.firstOrNull() ?: return "null"
		val props = commonProperties(beans)
		return toJson(obj {
			title = if (beans.size > 1) Translations.getString("web.properties.multiple", beans.size) else WebPropertyRegistry.titleOf(first)
			subtitle = if (first is Component && beans.size == 1) "#${first.id}" else ""
			isCircuit = first !is Component
			editable = e.view.editable
			properties = props.mapNotNull {
				try {
					val description = it.describe(first)
					if (beans.size > 1 && description.choices != null) {
						val ids = commonChoiceIds(beans, it.key) ?: emptySet()
						description.choices = description.choices.unsafeCast<Array<dynamic>>()
							.filter { c -> c.id.unsafeCast<String>() in ids }.toTypedArray()
					}
					description
				} catch (t: Throwable) {
					LOG.debug("Cannot describe property ${it.key}: ${t.message}")
					null
				}
			}.toJsArray()
		})
	}

	fun set(key: String, value: dynamic) {
		val e = activeEditor
		if (!e.view.editable) return
		val beans = beans(e)
		if (commonProperties(beans).none { it.key == key }) return
		// Beans of different types have their own descriptors for the same key, e.g. a `size` choice of other values
		val targets = beans.mapNotNull { bean ->
			val property = propertyOf(bean, key) ?: return@mapNotNull null
			Target(bean, property, property.parse(bean, value, e))
		}
		if (targets.size != beans.size) return

		if (targets.any { it.property.apply != null }) {
			targets.forEach { t -> t.property.apply?.invoke(t.bean, t.value, e) }
			e.view.repaint()
			return
		}
		if (targets.any { it.property.set == null }) return
		// One command per bean type and value, all in one undoable transaction
		val commands = targets.groupBy { it.bean::class to it.value }.values.map { group ->
			val property = group.first().property
			propertyCommand(e, property.baseKey, property.baseKeyParams, group.map { it.bean }, group.first().value, property.get, setter = property.set!!)
		}.filter { it.valueChanged }
		runCommands(e, commands)
	}

	private class Target(val bean: Any, val property: WebProperty<Any, Any?>, val value: Any?)
}
