package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.AbstractPropertyCommand
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
	val toJs: (V?) -> Any? = { it },
	@Suppress("UNCHECKED_CAST")
	val fromJs: (Any?) -> V? = { it as V? },
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

	fun parse(bean: B, value: Any?, editor: Editor): V? {
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
 * the desktop's `CommandPropertySwing`. Used by the web editor's property inspector and by custom [WebProperty.apply] implementations
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
	val command = propertyCommand(editor, baseKey, baseKeyParams, beans, newValue, getter, undoable, setter)
	if (command.valueChanged) runCommands(editor, listOf(command))
}

/** Creates a [WebPropertyCommand] for [beans] and establishes their current values. */
internal fun <V> propertyCommand(
	editor: Editor,
	baseKey: String,
	baseKeyParams: Array<Any>,
	beans: List<Any>,
	newValue: V?,
	getter: (Any) -> V?,
	undoable: Boolean = true,
	setter: (Any, V?) -> Unit,
): WebPropertyCommand<V> {
	val isDrawing = beans.firstOrNull() !is Component
	val ids = if (isDrawing) listOf("0") else beans.map { (it as Component).id.toString() }
	return WebPropertyCommand(editor, baseKey, baseKeyParams, ids, isDrawing, newValue, undoable, getter, setter)
		.also { it.establishOldValues() }
}

/** Executes [commands] as one transaction, so they are undone together. */
internal fun runCommands(editor: Editor, commands: List<WebPropertyCommand<*>>) {
	if (commands.isEmpty()) return
	val commandManager = editor.commandManager
	try {
		commandManager.beginTransaction(commands.first())
		commands.drop(1).forEach { commandManager.execute(it) }
		commandManager.commitTransaction()
	} catch (t: Throwable) {
		if (commandManager.isInTransaction) commandManager.rollbackTransaction()
		throw t
	}
	editor.view.repaint()
}
