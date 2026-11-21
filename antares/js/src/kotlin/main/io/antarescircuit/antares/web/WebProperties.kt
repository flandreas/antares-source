package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.AbstractPropertyCommand
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.drawingBeanProvider

/** The kind of editor the web application shows for a [WebProperty]. */
internal enum class WebPropertyType(val id: String) {
	Text("text"),
	MultilineText("multiline"),
	Boolean("boolean"),
	Integer("integer"),
	Number("number"),
	Choice("choice"),
	Color("color"),
	ReadOnly("readonly"),
}

/**
 * A selectable value of a [WebPropertyType.Choice] or [WebPropertyType.Color] property.
 * @param color optional CSS color shown as swatch by the web application
 */
internal class WebChoice<V>(val id: String, val label: String, val value: V, val color: String? = null)

/**
 * A portable property descriptor, replacing the JVM-only BeanInfo classes for the web editor.
 *
 * @param key unique key within a bean, used by the web application to set values
 * @param baseKey translation base key; `<baseKey>.name` is the label and also names the undoable command
 * @param get reads the current value from the bean
 * @param set writes a value to the bean; `null` for read-only properties
 * @param choices the selectable values of [WebPropertyType.Choice] properties
 * @param evaluate optionally evaluates a parsed value in the context of the edited circuit (e.g. expressions)
 * @param apply optional custom application instead of an undoable property command, see [runPropertyCommand]
 */
internal class WebProperty<B : Any, V>(
	val key: String,
	val baseKey: String,
	val type: WebPropertyType,
	val get: (B) -> V?,
	val set: ((B, V?) -> Unit)? = null,
	val choices: ((B) -> List<WebChoice<V>>)? = null,
	val group: String = "general",
	val baseKeyParams: Array<Any> = emptyArray(),
	val min: Double? = null,
	val max: Double? = null,
	val toJs: (V?) -> dynamic = { it },
	val fromJs: (dynamic) -> V? = { it.unsafeCast<V?>() },
	val evaluate: ((V, Editor) -> V)? = null,
	val apply: ((B, V?, Editor) -> Unit)? = null,
) {
	val label: String get() = Translations.getOptionalString("$baseKey.name")?.let { formatParams(it) } ?: key

	val isReadOnly: Boolean get() = type == WebPropertyType.ReadOnly || (set == null && apply == null)

	private fun formatParams(s: String): String {
		var result = s
		baseKeyParams.forEachIndexed { i, p -> result = result.replace("{$i}", "$p") }
		return result
	}

	fun describe(bean: B): dynamic {
		val value = get(bean)
		val choices = choices?.invoke(bean)
		val choice = choices?.firstOrNull { it.value == value }
		val readOnly = isReadOnly
		val typeId = (if (readOnly) WebPropertyType.ReadOnly else type).id
		return obj {
			this.key = this@WebProperty.key
			this.label = this@WebProperty.label
			this.type = typeId
			this.group = this@WebProperty.group
			this.value = when {
				choices == null -> toJs(value)
				readOnly -> choice?.label
				else -> choice?.id
			}
			this.choices = if (readOnly) null else choices?.map { c ->
				obj {
					this.id = c.id
					this.label = c.label
					this.color = c.color
				}
			}?.toJsArray()
			this.min = this@WebProperty.min
			this.max = this@WebProperty.max
		}
	}

	fun parse(bean: B, value: dynamic, editor: Editor): V? {
		val choices = choices?.invoke(bean)
		val parsed = if (choices != null) choices.firstOrNull { it.id == value }?.value else fromJs(value)
		val evaluate = evaluate
		return if (parsed != null && evaluate != null) evaluate(parsed, editor) else parsed
	}
}

/** Lambda-based property change command, applied to all [beanIds]. */
internal class WebPropertyCommand<V>(
	editor: Editor,
	baseKey: String,
	baseKeyParams: Array<Any>,
	beanIds: Collection<String>,
	isDrawing: Boolean,
	newValue: V?,
	private val undoable: Boolean,
	private val getter: (Any) -> V?,
	private val setter: (Any, V?) -> Unit,
) : AbstractPropertyCommand<V>(
	editor,
	baseKey,
	baseKeyParams,
	if (isDrawing) drawingBeanProvider else componentBeanProvider,
	beanIds,
	newValue
) {
	override val canUndo: Boolean get() = undoable
	override fun getValue(bean: Bean): V? = getter(bean)
	override fun setValue(bean: Bean, value: V?) = setter(bean, value)
}

/**
 * Executes a property change of [beans] (components or the drawing) as [WebPropertyCommand], like
 * the desktop's `CommandPropertySwing`. Used by [WebProperties] and by custom [WebProperty.apply] implementations
 * that need a non-undoable command (`undoable = false`) or a special [setter].
 */
internal fun <V> runPropertyCommand(
	editor: Editor,
	baseKey: String,
	baseKeyParams: Array<Any>,
	beans: List<Any>,
	newValue: V?,
	getter: (Any) -> V?,
	undoable: Boolean = true,
	setter: (Any, V?) -> Unit,
) {
	val isDrawing = beans.firstOrNull() !is Component
	val ids = if (isDrawing) listOf("0") else beans.map { (it as Component).id.toString() }
	val command = WebPropertyCommand(editor, baseKey, baseKeyParams, ids, isDrawing, newValue, undoable, getter, setter)
	command.establishOldValues()
	if (!command.valueChanged) return
	val commandManager = editor.commandManager
	try {
		commandManager.beginTransaction(command)
		commandManager.commitTransaction()
	} catch (t: Throwable) {
		if (commandManager.isInTransaction) commandManager.rollbackTransaction()
		throw t
	}
	editor.view.repaint()
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

	/** Properties shared by all [beans] (same key), described for the first bean. */
	private fun commonProperties(beans: List<Any>): List<WebProperty<Any, Any?>> {
		val first = beans.firstOrNull() ?: return emptyList()
		val firstProps = WebPropertyRegistry.propertiesOf(first)
		if (beans.size == 1) return firstProps
		val others = beans.drop(1).map { b -> WebPropertyRegistry.propertiesOf(b).filter { !it.isReadOnly }.map { it.key }.toSet() }
		return firstProps.filter { p -> !p.isReadOnly && others.all { it.contains(p.key) } }
	}

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
					it.describe(first)
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
		val first = beans.firstOrNull() ?: return
		val property = commonProperties(beans).firstOrNull { it.key == key } ?: return
		val newValue = property.parse(first, value, e)

		property.apply?.let { apply ->
			beans.forEach { apply(it, newValue, e) }
			e.view.repaint()
			return
		}
		val setter = property.set ?: return
		runPropertyCommand(e, property.baseKey, property.baseKeyParams, beans, newValue, property.get, setter = setter)
	}
}
