package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthGraphParamType
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.LightColorGraphParamType
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.semantic.SemanticRegistry
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinition
import io.antarescircuit.jabbah.graph.model.param.GraphParamDefinitions
import io.antarescircuit.jabbah.graph.model.param.GraphParamType
import io.antarescircuit.jabbah.graph.model.param.GraphParamTypeRegistry
import io.antarescircuit.jabbah.graph.model.param.LongValueGraphParamType
import io.antarescircuit.jabbah.graph.model.param.StringGraphParamType
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Editing the parameter definitions of the open circuit (desktop: `GraphParamDefinitionsController`,
 * `GraphParamDefinitionsViewSwing`). Each change is an undoable property command on the circuit,
 * like saving the desktop's parameter definitions dialog.
 */
@JsExport
class WebParams internal constructor(private val ctx: WebEditorContext) {

	private val graphView: GraphView? get() = ctx.editor.drawing as? GraphView

	private val graph: Graph? get() = graphView?.graph

	private val editable: Boolean
		get() = graphView != null && ctx.editor.view.editable && ctx.frame().applicationModeHolder.currentMode.isEdit()

	/** Returns the parameter definitions of the open circuit and the available types as JSON, or `null`. */
	fun definitions(): String = ctx.guarded("null") {
		val graph = graph ?: return@guarded "null"
		val editable = editable
		val none = Translations.getString("graph.paramDefs.dialog.semantic.none")
		val types = supportedTypes().map { describeType(it) }
		val semantics = SemanticRegistry.semantics.map { s ->
			val id = s.customName
			val label = s.translatedName
			obj {
				this.id = id
				this.label = label
			}
		}
		val items = graph.parameterDefinitions.definitions.map { describeDefinition(it, none) }
		toJson(obj {
			this.editable = editable
			this.types = types.toJsArray()
			this.semantics = semantics.toJsArray()
			this.items = items.toJsArray()
		})
	}

	/**
	 * Adds a definition ([originalName] empty) or replaces the definition named [originalName], keeping its position.
	 * Validates like the desktop's `GraphParamDefinitionsController`.
	 * @param value the default value as ID (see [definitions])
	 * @param semantic the custom name of the [io.antarescircuit.jabbah.edit.semantic.Semantic], or empty for none
	 * @return a translated error message, or an empty string on success
	 */
	fun save(originalName: String, name: String, type: String, value: String, semantic: String, description: String): String =
		ctx.guarded("") {
			val graph = graph ?: return@guarded ""
			if (!editable) return@guarded ""
			val defs = graph.parameterDefinitions
			val trimmedName = name.trim()
			val error = when {
				StringUtils.isBlank(trimmedName) -> Translations.getString("graph.paramDefs.dialog.error.emptyName")
				trimmedName != originalName && defs.contains(trimmedName) -> Translations.getString("graph.paramDefs.dialog.error.unique")
				graph.getGraphPort<Any>(trimmedName) != null -> Translations.getString("graph.paramDefs.dialog.error.usedForPort")
				else -> null
			}
			if (error != null) return@guarded error

			val paramType = GraphParamTypeRegistry.get<Any>(type)
			val defaultValue = parseValue(paramType, value) ?: return@guarded if (value.isBlank() && paramType != StringGraphParamType) {
				Translations.getString("graph.paramDefs.dialog.error.defaultValue")
			} else {
				Translations.getString("edit.magnitude.invalidValue.text", value)
			}
			val oldDescription = defs.get(originalName)?.description
			val newDef = GraphParamDefinition.create(
				trimmedName,
				paramType,
				defaultValue,
				semantic.takeIf { it.isNotEmpty() }?.let { SemanticRegistry.withCustomName(it) },
				Description(oldDescription.withText(description.trim())),
			)
			val list = if (defs.contains(originalName)) defs.definitions.map { if (it.name == originalName) newDef else it } else defs.definitions + newDef
			apply(list.fold(GraphParamDefinitions()) { result, def -> result.withDefinition(def) })
			""
		}

	/** Removes the definition named [name]. */
	fun remove(name: String) = ctx.guarded(Unit) {
		val defs = graph?.parameterDefinitions ?: return@guarded
		if (editable && defs.contains(name)) {
			apply(defs.withoutDefinition(name))
		}
	}

	private fun apply(newDefs: GraphParamDefinitions) {
		val view = graphView ?: return
		runPropertyCommand(
			ctx.editor, "graph.property.graphParams", emptyArray(), listOf(view), newDefs,
			getter = { (it as GraphView).graph!!.parameterDefinitions },
		) { bean, value -> (bean as GraphView).graph!!.parameterDefinitions = value!! }
		ctx.notifyChanged()
	}

	/** Types with an editor for default values (desktop: `GraphParamValueEditorRegistry`). */
	private fun supportedTypes(): List<GraphParamType<*>> =
		GraphParamTypeRegistry.getAll().filter {
			it == LongValueGraphParamType || it == StringGraphParamType || it == BitWidthGraphParamType || it == LightColorGraphParamType
		}

	/** The selectable values of types edited with a combo box on the desktop, or `null` for text input. */
	private fun choices(type: GraphParamType<*>): List<Any>? = when (type) {
		BitWidthGraphParamType -> BitWidth.PREDEFINED
		LightColorGraphParamType -> LightColor.PREDEFINED
		else -> null
	}

	private fun describeType(type: GraphParamType<*>): dynamic {
		val id = type.name
		val label = type.displayableName
		val initial = choices(type)?.first()?.let { valueId(it) } ?: if (type == LongValueGraphParamType) "0" else ""
		val choices = choices(type)?.map { v ->
			val choiceId = valueId(v)
			val choiceLabel = v.toString()
			val color = (v as? LightColor)?.onColor?.toCss()
			obj {
				this.id = choiceId
				this.label = choiceLabel
				this.color = color
			}
		}?.toJsArray()
		return obj {
			this.id = id
			this.label = label
			this.initial = initial
			this.choices = choices
		}
	}

	private fun describeDefinition(def: GraphParamDefinition<*>, none: String): dynamic {
		val name = def.name
		val type = def.type.name
		val typeLabel = def.type.displayableName
		val value = valueId(def.defaultValue)
		val valueLabel = def.defaultValue.toString()
		val semantic = def.semantic?.customName
		val semanticLabel = def.semantic?.translatedName ?: none
		val description = def.description.getOptionalTranslation() ?: ""
		return obj {
			this.name = name
			this.type = type
			this.typeLabel = typeLabel
			this.value = value
			this.valueLabel = valueLabel
			this.semantic = semantic
			this.semanticLabel = semanticLabel
			this.description = description
		}
	}

	private fun valueId(value: Any): String = when (value) {
		is BitWidth -> value.width.toString()
		is LightColor -> value.customName
		is LongValue -> value.value.toString()
		else -> value.toString()
	}

	/** Parses a default value like the desktop's value editors, returning `null` if invalid. */
	private fun parseValue(type: GraphParamType<*>, id: String): Any? = when (type) {
		LongValueGraphParamType -> id.trim().toLongOrNull()?.let { LongValueImpl(it) }
		BitWidthGraphParamType -> BitWidth.PREDEFINED.firstOrNull { it.width.toString() == id }
		LightColorGraphParamType -> LightColor.PREDEFINED.firstOrNull { it.customName == id }
		StringGraphParamType -> id
		else -> null
	}
}
