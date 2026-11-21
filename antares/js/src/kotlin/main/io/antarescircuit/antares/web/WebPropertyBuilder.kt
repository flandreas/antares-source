package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.BitWidthExpression
import io.antarescircuit.antares.model.signal.BitWidthGraphParamType
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.LightColorExpression
import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.PredefinedColor
import io.antarescircuit.jabbah.draw.graphics.PredefinedColorRepository
import io.antarescircuit.jabbah.draw.graphics.PredefinedStroke
import io.antarescircuit.jabbah.draw.graphics.PredefinedStrokeRepository
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.Translatable
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValueParser
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.graph.model.param.GraphParamType
import io.antarescircuit.jabbah.graph.model.param.LongValueExpression
import io.antarescircuit.jabbah.graph.model.param.LongValueGraphParamType
import io.antarescircuit.jabbah.graph.view.GraphView

/** Property groups understood by the web application's inspector. */
internal object WebGroups {
	const val GENERAL = "general"
	const val APPEARANCE = "appearance"
	const val PORTS = "ports"
	const val SIMULATION = "simulation"
	const val DOCUMENTATION = "documentation"
}

/**
 * Collects the [WebProperty]s of a bean of type [B] with typed helpers that convert values to and from
 * JSON primitives. Created per bean by [WebPropertyRegistry.define], so conditions (like the desktop's
 * `editable = !bean.model.isConnected`) can be evaluated directly.
 */
internal class WebPropertyBuilder<B : Any> {

	val properties = mutableListOf<WebProperty<B, *>>()

	/** Keys of properties contributed by earlier (base class) registrations to be hidden. */
	val removals = mutableSetOf<String>()

	fun <V> add(property: WebProperty<B, V>) {
		properties.add(property)
	}

	/** Hides properties registered for base classes, like the desktop's `isShowColor = false` overrides. */
	fun remove(vararg keys: String) {
		removals.addAll(keys)
	}

	private fun <V> setter(editable: Boolean, set: ((B, V) -> Unit)?): ((B, V?) -> Unit)? =
		if (editable && set != null) { b, v -> set(b, v ?: throw IllegalArgumentException(invalidValue(v))) } else null

	fun readOnly(key: String, baseKey: String, get: (B) -> Any?, group: String = WebGroups.GENERAL) =
		add(WebProperty<B, Any?>(key, baseKey, WebPropertyType.ReadOnly, get, group = group, toJs = { it?.toString() ?: "" }))

	/** A text property; an empty input is passed as empty string. */
	fun text(
		key: String, baseKey: String, get: (B) -> String?, set: ((B, String) -> Unit)?,
		multiline: Boolean = false, editable: Boolean = true, group: String = WebGroups.GENERAL,
		baseKeyParams: Array<Any> = emptyArray(),
	) = add(WebProperty(
		key, baseKey, if (multiline) WebPropertyType.MultilineText else WebPropertyType.Text,
		get = { get(it) ?: "" }, set = setter(editable, set), group = group, baseKeyParams = baseKeyParams,
		fromJs = { jsString(it) ?: "" },
	))

	/** A text property whose empty input is passed as `null`, e.g. for optional names. */
	fun optionalText(
		key: String, baseKey: String, get: (B) -> String?, set: ((B, String?) -> Unit)?,
		editable: Boolean = true, group: String = WebGroups.GENERAL, baseKeyParams: Array<Any> = emptyArray(),
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Text,
		get = { get(it) ?: "" }, set = if (editable) set else null, group = group, baseKeyParams = baseKeyParams,
		fromJs = { jsString(it)?.takeIf { s -> s.isNotEmpty() } },
	))

	fun bool(
		key: String, baseKey: String, get: (B) -> Boolean, set: ((B, Boolean) -> Unit)?,
		editable: Boolean = true, group: String = WebGroups.GENERAL, baseKeyParams: Array<Any> = emptyArray(),
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Boolean, get = get, set = setter(editable, set), group = group,
		baseKeyParams = baseKeyParams, fromJs = { jsBoolean(it) },
	))

	fun int(
		key: String, baseKey: String, get: (B) -> Int, set: ((B, Int) -> Unit)?,
		min: Int? = null, max: Int? = null, editable: Boolean = true, group: String = WebGroups.GENERAL,
		baseKeyParams: Array<Any> = emptyArray(),
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Integer, get = get, set = setter(editable, set), group = group,
		baseKeyParams = baseKeyParams, min = min?.toDouble(), max = max?.toDouble(),
		fromJs = { jsNumber(it)?.let { d -> checkRange(d, min?.toDouble(), max?.toDouble()).toInt() } },
	))

	fun long(
		key: String, baseKey: String, get: (B) -> Long, set: ((B, Long) -> Unit)?,
		min: Long? = null, max: Long? = null, editable: Boolean = true, group: String = WebGroups.GENERAL,
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Integer, get = get, set = setter(editable, set), group = group,
		min = min?.toDouble(), max = max?.toDouble(),
		toJs = { it?.toDouble() },
		fromJs = { jsNumber(it)?.let { d -> checkRange(d, min?.toDouble(), max?.toDouble()).toLong() } },
	))

	/** An optional [Long]; an empty input is passed as `null` (desktop: `optional = true`). */
	fun optionalLong(
		key: String, baseKey: String, get: (B) -> Long?, set: ((B, Long?) -> Unit)?,
		min: Long? = null, editable: Boolean = true, group: String = WebGroups.GENERAL,
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Integer, get = get, set = if (editable) set else null, group = group,
		min = min?.toDouble(),
		toJs = { it?.toDouble() },
		fromJs = { jsNumber(it)?.let { d -> checkRange(d, min?.toDouble(), null).toLong() } },
	))

	fun double(
		key: String, baseKey: String, get: (B) -> Double, set: ((B, Double) -> Unit)?,
		min: Double? = null, max: Double? = null, editable: Boolean = true, group: String = WebGroups.GENERAL,
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Number, get = get, set = setter(editable, set), group = group,
		min = min, max = max,
		fromJs = { jsNumber(it)?.let { d -> checkRange(d, min, max) } },
	))

	/** A selection among [values]; [nullLabel] adds a `null` choice for optional properties. */
	fun <V> choice(
		key: String, baseKey: String, values: List<V>, get: (B) -> V?, set: ((B, V?) -> Unit)?,
		id: (V) -> String = { it.toString() }, label: (V) -> String = ::choiceLabel, color: (V) -> String? = { null },
		nullLabel: String? = null, editable: Boolean = true, group: String = WebGroups.GENERAL,
		type: WebPropertyType = WebPropertyType.Choice, baseKeyParams: Array<Any> = emptyArray(),
	) {
		val choices = buildList {
			if (nullLabel != null) add(WebChoice<V?>(NULL_ID, nullLabel, null))
			values.forEach { add(WebChoice<V?>(id(it), label(it), it, color(it))) }
		}
		add(WebProperty<B, V?>(
			key, baseKey, type, get = get, set = if (editable) set else null, choices = { choices }, group = group,
			baseKeyParams = baseKeyParams,
		))
	}

	/** A selection among enum [values], labelled by their (translated) `toString()`. */
	fun <E : Enum<E>> enum(
		key: String, baseKey: String, values: List<E>, get: (B) -> E, set: ((B, E) -> Unit)?,
		editable: Boolean = true, group: String = WebGroups.GENERAL,
	) = choice(key, baseKey, values, get, setter(editable, set), id = { it.name }, group = group)

	/** An optional enum, e.g. a default that falls back to system settings if `null`. */
	fun <E : Enum<E>> optionalEnum(
		key: String, baseKey: String, values: List<E>, get: (B) -> E?, set: ((B, E?) -> Unit)?,
		nullLabel: String, editable: Boolean = true, group: String = WebGroups.GENERAL,
	) = choice(key, baseKey, values, get, set, id = { it.name }, nullLabel = nullLabel, editable = editable, group = group)

	/** The custom [PredefinedColor] overriding the style color (desktop: `EditProperties.color`). */
	fun color(
		key: String = "customColor", baseKey: String = "edit.property.color",
		get: (B) -> PredefinedColor?, set: (B, PredefinedColor?) -> Unit, group: String = WebGroups.APPEARANCE,
	) = choice(
		key, baseKey, PredefinedColorRepository.provideAll(), get, set,
		id = { it.name }, label = { it.description }, color = { it.color.foregroundColor.toCss() },
		nullLabel = Translations.getString("edit.style.property.fromStyle.name"),
		group = group, type = WebPropertyType.Color,
	)

	/** The custom [PredefinedStroke] overriding the style stroke (desktop: `EditProperties.stroke`). */
	fun stroke(get: (B) -> PredefinedStroke?, set: (B, PredefinedStroke?) -> Unit) = choice(
		"customStroke", "edit.property.stroke", PredefinedStrokeRepository.provideAll(), get, set,
		id = { it.identity.id }, label = { humanize(it.identity.name) },
		nullLabel = Translations.getString("edit.style.property.fromStyle.name"),
		group = WebGroups.APPEARANCE,
	)

	/** A [Translatable] text edited in the current language, keeping other translations. */
	fun translatable(
		key: String, baseKey: String, get: (B) -> Translatable?, set: ((B, TranslatableText) -> Unit)?,
		multiline: Boolean = false, editable: Boolean = true, group: String = WebGroups.GENERAL,
	) = text(key, baseKey, { get(it)?.getOptionalTranslation() }, set?.let { s -> { b, v -> s(b, get(b).withText(v)) } }, multiline, editable, group)

	fun name(get: (B) -> Name, set: (B, Name) -> Unit, baseKey: String = "edit.property.name", editable: Boolean = true) =
		translatable("name", baseKey, get, { b, v -> set(b, Name(v)) }, editable = editable)

	fun description(
		get: (B) -> Description, set: (B, Description) -> Unit,
		key: String = "description", baseKey: String = "edit.property.description",
	) = translatable(key, baseKey, get, { b, v -> set(b, Description(v)) }, multiline = true, group = WebGroups.DOCUMENTATION)

	/** A script, edited as plain multiline text. */
	fun script(key: String, baseKey: String, get: (B) -> ScriptProperty, set: ((B, ScriptProperty) -> Unit)?, editable: Boolean = true) =
		text(key, baseKey, { get(it).script }, set?.let { s -> { b, v -> s(b, ScriptProperty(v.ifEmpty { null })) } }, multiline = true, editable = editable)

	fun location(get: (B) -> Point2D, set: (B, Point2D) -> Unit) = add(WebProperty(
		"location", "edit.property.location", WebPropertyType.Text, get = get, set = setter(true, set),
		toJs = { p -> p?.let { "${formatNumber(it.x)}, ${formatNumber(it.y)}" } },
		fromJs = { v -> jsString(v)?.let { parsePoint(it) } },
	))

	/** A [BitWidth], entered as number or as `=expression` evaluated with the circuit's parameters. */
	fun bitWidth(
		key: String = "bitWidth", baseKey: String = "element.property.bitWidth",
		get: (B) -> BitWidth, set: ((B, BitWidth) -> Unit)?,
		editable: Boolean = true, filter: (BitWidth) -> Boolean = { true }, supportExpressions: Boolean = true,
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Text, get = get,
		set = setter(editable, set?.let { s ->
			{ b: B, v: BitWidth ->
				require(v is BitWidthExpression || filter(v)) { invalidValue(v.width) }
				s(b, v)
			}
		}),
		toJs = { bw -> if (bw is BitWidthExpression) expressionText(bw.expression) else bw?.width?.toString() },
		fromJs = { v -> jsString(v)?.trim()?.takeIf { it.isNotEmpty() }?.let { BitWidthGraphParamType.parse(it, supportExpressions) } },
		evaluate = { bw, e -> if (bw is BitWidthExpression) graphOf(e)?.let { bw.evaluateIn(it) } ?: bw else bw },
	))

	/** A [LongValue], entered as number or as `=expression` evaluated with the circuit's parameters. */
	fun longValue(
		key: String, baseKey: String, get: (B) -> LongValue, set: ((B, LongValue) -> Unit)?,
		editable: Boolean = true, group: String = WebGroups.GENERAL,
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Text, get = get, set = setter(editable, set), group = group,
		toJs = { lv -> if (lv is LongValueExpression) expressionText(lv.expression) else lv?.value?.toString() },
		fromJs = { v -> jsString(v)?.trim()?.takeIf { it.isNotEmpty() }?.let { LongValueGraphParamType.parse(it) } },
		evaluate = { lv, e -> if (lv is LongValueExpression) graphOf(e)?.let { lv.evaluateIn(it) } ?: lv else lv },
	))

	/** A [MagnitudeValue] such as "100 kΩ", parsed with the first matching of [units]. */
	fun magnitude(
		key: String, baseKey: String, get: (B) -> MagnitudeValue, set: ((B, MagnitudeValue) -> Unit)?,
		vararg units: SIUnit, editable: Boolean = true,
	) = add(WebProperty(
		key, baseKey, WebPropertyType.Text, get = get, set = setter(editable, set),
		toJs = { it?.toString() },
		fromJs = { v -> jsString(v)?.trim()?.takeIf { it.isNotEmpty() }?.let { MagnitudeValueParser.parseWithUnits(it, *units) } },
	))

	/**
	 * A [LightColor] chosen among the predefined colors. A current expression value (set by the desktop's
	 * expression editor) is offered as additional choice so that it is displayed and kept.
	 */
	fun lightColor(
		key: String = "lightColor", baseKey: String = "element.property.LEDColor",
		get: (B) -> LightColor?, set: (B, LightColor?) -> Unit, current: LightColor?,
		nullLabel: String? = null, group: String = WebGroups.APPEARANCE,
	) = choice(
		key, baseKey,
		LightColor.PREDEFINED + listOfNotNull(current as? LightColorExpression),
		get, set,
		id = { if (it is LightColorExpression) expressionText(it.expression) else it.customName },
		label = { if (it is LightColorExpression) expressionText(it.expression) else it.toString() },
		color = { it.onColor.toCss() },
		nullLabel = nullLabel, group = group, type = WebPropertyType.Color,
	)
}

private const val NULL_ID = ""

private fun invalidValue(value: Any?): String = Translations.getString("edit.magnitude.invalidValue.text", "$value")

private fun graphOf(editor: Editor) = (editor.drawing as? GraphView)?.graph

private fun expressionText(expression: String): String =
	if (expression.startsWith(GraphParamType.EXPRESSION_OP)) expression else "${GraphParamType.EXPRESSION_OP}$expression"

private fun checkRange(value: Double, min: Double?, max: Double?): Double {
	require((min == null || value >= min) && (max == null || value <= max)) { invalidValue(formatNumber(value)) }
	return value
}

/** Returns the string of a JSON primitive, or `null` for `null`/`undefined`. */
internal fun jsString(value: dynamic): String? = if (value == null) null else value.toString()

internal fun jsBoolean(value: dynamic): Boolean? = when (jsTypeOf(value)) {
	"boolean" -> value.unsafeCast<Boolean>()
	"string" -> value == "true"
	else -> null
}

/** Returns a number from a JSON number or numeric string, `null` for empty input. */
internal fun jsNumber(value: dynamic): Double? = when (jsTypeOf(value)) {
	"number" -> value.unsafeCast<Double>().also { require(!it.isNaN()) { invalidValue(it) } }
	"string" -> value.unsafeCast<String>().trim().takeIf { it.isNotEmpty() }?.let {
		it.toDoubleOrNull() ?: throw IllegalArgumentException(invalidValue(it))
	}
	else -> null
}

private fun formatNumber(d: Double): String = if (d == kotlin.math.floor(d)) d.toLong().toString() else d.toString()

private fun parsePoint(s: String): Point2D {
	val parts = s.trim().split(Regex("[,;\\s]+")).filter { it.isNotEmpty() }
	val coords = parts.mapNotNull { it.toDoubleOrNull() }
	require(parts.size == 2 && coords.size == 2) { invalidValue(s) }
	return Point2D(coords[0], coords[1])
}

/** Converts [Translatable] implementations ([Name], [Description], ...) to a [TranslatableText]. */
private fun Translatable.toTranslatableText(): TranslatableText = when (this) {
	is TranslatableText -> this
	is Name -> translation
	is Description -> translation
	else -> TranslatableText(allTranslations().asSequence().toList())
}

/** Replaces the text of the current language, keeping translations into other languages. */
internal fun Translatable?.withText(text: String): TranslatableText {
	val base = this?.toTranslatableText() ?: TranslatableText()
	val language = System.currentLanguage()
	return if (text.isEmpty()) base.withoutTranslation(language) else base.withTranslation(language, text)
}

internal fun Color.toCss(): String =
	"#" + listOf(red, green, blue).joinToString("") { it.coerceIn(0, 255).toString(16).padStart(2, '0') }

/** The label of a choice: its translated `toString()`, or a humanized enum name if not translated. */
internal fun choiceLabel(value: Any?): String {
	if (value == null) return ""
	val s = try {
		value.toString()
	} catch (_: Throwable) {
		""
	}
	return if (value is Enum<*> && (s.isEmpty() || s == value.name)) humanize(value.name) else s
}

/** "THIN_DOTTED"/"ThinDotted" → "Thin dotted". */
internal fun humanize(name: String): String {
	val words = name.replace('_', ' ')
		.replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
		.lowercase()
		.trim()
	return words.replaceFirstChar { it.uppercaseChar() }
}
