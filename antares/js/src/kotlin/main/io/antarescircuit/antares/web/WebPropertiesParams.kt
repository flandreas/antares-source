package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthGraphParamType
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.LightColorGraphParamType
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinitions
import io.antarescircuit.jabbah.graph.model.param.GraphParamValueCommand
import io.antarescircuit.jabbah.graph.model.param.LongValueGraphParamType
import io.antarescircuit.jabbah.graph.model.param.StringGraphParamType
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl

private const val PARAMS_GROUP = "parameters"

/**
 * Parameter values of generic sub-circuits (desktop: `SubGraphVerticeViewImplBeanInfo` with
 * `GraphParamValuePropertyFactoryRegistry`), changed as undoable [GraphParamValueCommand]s.
 */
internal fun WebPropertyRegistry.registerParamProperties() {
	// Labels show the parameter name only, as the properties are grouped under "Parameters"
	Translations.addKey("web.param.value.name", "{0}")
	Translations.addKey("web.param.semanticValue.name", "{0} ({1})")

	define<SubGraphVerticeViewImpl> { sub ->
		if (sub.model.designError != null) return@define
		val defs = paramDefinitions(sub) ?: return@define
		if (defs.isEmpty) return@define
		defs.forEach { def -> paramValue(sub, def) }
		// Keep the documentation after the parameters
		description({ it.description }, { b, v -> b.description = v })
	}
}

private fun paramDefinitions(sub: SubGraphVerticeViewImpl): GraphParamDefinitions? = try {
	sub.model.graphUUID?.let { LibraryModule.libraryHolder.getMetaGraph(it).graph.model?.parameterDefinitions }
} catch (_: Throwable) {
	null
}

/** Adds the property for the value of [def], using the editor of its type like the desktop's property editors. */
@Suppress("UNCHECKED_CAST")
private fun WebPropertyBuilder<SubGraphVerticeViewImpl>.paramValue(sub: SubGraphVerticeViewImpl, def: GraphParamDefinition<*>) {
	val key = "param.${def.name}"
	// The builder creates the property with the value conversions of the type; setters are replaced by commands
	val builder = WebPropertyBuilder<SubGraphVerticeViewImpl>()
	when (def.type) {
		LongValueGraphParamType -> builder.longValue(key, "", { valueOf(it, def) as LongValue }, { _, _ -> })
		BitWidthGraphParamType -> builder.bitWidth(key, "", { valueOf(it, def) as BitWidth }, { _, _ -> })
		LightColorGraphParamType -> builder.lightColor(key, "", { valueOf(it, def) as LightColor }, { _, _ -> }, current = valueOf(sub, def) as LightColor)
		StringGraphParamType -> builder.text(key, "", { valueOf(it, def) as String }, { _, _ -> })
		else -> return
	}
	builder.properties.forEach { add(withCommand(it as WebProperty<SubGraphVerticeViewImpl, Any?>, def as GraphParamDefinition<Any>)) }
}

/** The value of [def] in [sub], or its default value if not set (e.g. a definition added after placing [sub]). */
private fun valueOf(sub: SubGraphVerticeViewImpl, def: GraphParamDefinition<*>): Any =
	sub.model.paramValues.getValue(def.name)?.value ?: def.defaultValue

/** Copies [property], labelled by the parameter name and applied as [GraphParamValueCommand]. */
private fun withCommand(
	property: WebProperty<SubGraphVerticeViewImpl, Any?>,
	def: GraphParamDefinition<Any>,
): WebProperty<SubGraphVerticeViewImpl, Any?> {
	val semantic = def.semantic
	// The desktop's command name, e.g. "Parameter 'n'"
	val commandKey = if (semantic != null) "graph.paramDefs.genericSemanticParameter" else "graph.paramDefs.genericParameter"
	val params: Array<Any> = if (semantic != null) arrayOf(def.name, semantic.translatedName) else arrayOf(def.name)
	return WebProperty(
		property.key,
		if (semantic != null) "web.param.semanticValue" else "web.param.value",
		property.type,
		property.get,
		set = null,
		choices = property.choices,
		group = PARAMS_GROUP,
		baseKeyParams = params,
		toJs = property.toJs,
		fromJs = property.fromJs,
		evaluate = property.evaluate,
		apply = { bean, value, editor -> runParamValueCommand(def, editor, commandKey, params, bean, value) },
	)
}

private fun runParamValueCommand(
	def: GraphParamDefinition<Any>,
	editor: Editor,
	baseKey: String,
	baseKeyParams: Array<Any>,
	bean: SubGraphVerticeViewImpl,
	value: Any?,
) {
	requireNotNull(value) { Translations.getString("edit.magnitude.invalidValue.text", "") }
	val command = GraphParamValueCommand(def, editor, baseKey, baseKeyParams, componentBeanProvider, listOf(bean.id.toString()), value)
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
}
