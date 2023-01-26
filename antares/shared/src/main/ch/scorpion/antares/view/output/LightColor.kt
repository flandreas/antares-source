package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.graph.view.VerticeView
import kotlin.math.roundToInt

/**
 * A visible object that can emit light.
 */
interface LightEmitter {

	/** Defines the [LightColor] of the light emitted by this [LightEmitter].*/
	var lightColor: LightColor
}
/**
 * Enumerates the colors of [VerticeView]s that emit light.
 */
enum class LightColor(
	override val customName: String,
	val onColor: Color,
	val offColor: Color
) : EnumProperty<LightColor> {

    RED("red", Color(255, 0, 0), Color(60, 0, 0)),
    YELLOW("yellow", Color(255, 255, 0), Color(60, 47, 0)),
    GREEN("green", Color(0, 255, 0), Color(0, 60, 0)),
    BLUE("blue", Color(0, 255, 255), Color(0, 0, 60)),
    ORANGE("orange", Color(253, 146, 71), Color(105, 2,5)),
	WHITE("white", Color(255, 255, 255), Color(8, 8, 8));

    companion object {

	    /** The name of the [String] in [Properties] representing the persistent name of the [LightColor] to be used as default.*/
	    const val PROP_DEFAULT_LIGHT_COLOR = "antares.view.output.defaultLightColor"

	    private const val GRADIENT_STEPS = 50

        fun withName(customName: String): LightColor {
            for (c in values()) {
                if (c.customName == customName) {
                    return c
                }
            }
            throw IllegalArgumentException("Unknown LightColor '$customName'")
        }

	    fun getSystemDefault(properties: Properties = BaseModule.properties): LightColor =
		    withName(properties.getString(PROP_DEFAULT_LIGHT_COLOR))
    }

	private val gradient: List<Color> by lazy { createGradient() }

    override fun toString(): String {
        return when(this) {
            RED -> Translations.getString("element.color.red")
            YELLOW -> Translations.getString("element.color.yellow")
            GREEN -> Translations.getString("element.color.green")
            BLUE -> Translations.getString("element.color.blue")
	        ORANGE -> Translations.getString("element.color.orange")
	        WHITE -> Translations.getString("element.color.white")
        }
    }

	fun gradient(level: Float): Color =
		gradient[(level.coerceIn(0.0f..1.0f) * GRADIENT_STEPS).roundToInt().coerceIn(0 until GRADIENT_STEPS)]

	private fun createGradient(): List<Color> {
		val result = mutableListOf<Color>()
		val dRed = (onColor.red - offColor.red).toFloat() / (GRADIENT_STEPS - 1)
		val dGreen = (onColor.green - offColor.green).toFloat() / (GRADIENT_STEPS - 1)
		val dBlue = (onColor.blue - offColor.blue).toFloat() / (GRADIENT_STEPS - 1)
		var red = offColor.red.toFloat()
		var green = offColor.green.toFloat()
		var blue = offColor.blue.toFloat()
		for (i in 0 until GRADIENT_STEPS) {
			result.add(Color(
				red.roundToInt().coerceIn(0..255),
				green.roundToInt().coerceIn(0..255),
				blue.roundToInt().coerceIn(0..255)))
			red += dRed
			green += dGreen
			blue += dBlue
		}
		return result
	}
}
