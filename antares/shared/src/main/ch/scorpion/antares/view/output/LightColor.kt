package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.ColorGradient
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * A visible object that can emit light.
 */
interface LightEmitter {

	/** Defines the [LightColor] of the light emitted by this [LightEmitter].*/
	var lightColor: LightColor
}

private val RED_ON = Color(255, 0, 0)
private val YELLOW_ON = Color(255, 255, 0)
private val GREEN_ON = Color(0, 255, 0)
private val BLUE_ON = Color(0, 255, 255)
private val ORANGE_ON = Color(253, 146, 71)
private val WHITE_ON = Color.WHITE

private const val STEPS = 100
private const val OFF_LIGHT_AT = 25
private const val OFF_DARK_AT = 8
private const val EDIT_DARK_AT = 20
private const val EDIT_LIGHT_ALPHA = 64

/**
 * Enumerates the colors of [VerticeView]s that emit light.
 */
enum class LightColor(
	override val customName: String,
	private val onColorLight: Color,
	private val offColorLight: Color,
    private val editColorLight: Color = onColorLight.withAlpha(EDIT_LIGHT_ALPHA),
    private val onColorDark: Color = onColorLight,
    private val offColorDark: Color = offColorLight,
    private val editColorDark: Color = offColorDark
) : EnumProperty<LightColor> {

    RED(
        "red",
        onColorLight = RED_ON,
        offColorLight = ColorGradient.calculateAt(Color.BLACK, RED_ON, STEPS, OFF_LIGHT_AT),
        offColorDark = ColorGradient.calculateAt(Color.BLACK, RED_ON, STEPS, OFF_DARK_AT),
        editColorDark = ColorGradient.calculateAt(Color.BLACK, RED_ON, STEPS, EDIT_DARK_AT),
    ),

    YELLOW(
        "yellow",
        onColorLight = YELLOW_ON,
        offColorLight = ColorGradient.calculateAt(Color.BLACK, YELLOW_ON, STEPS, OFF_LIGHT_AT),
        offColorDark = ColorGradient.calculateAt(Color.BLACK, YELLOW_ON, STEPS, OFF_DARK_AT),
        editColorDark = ColorGradient.calculateAt(Color.BLACK, YELLOW_ON, STEPS, EDIT_DARK_AT),
    ),

    GREEN(
        "green",
        onColorLight = GREEN_ON,
        offColorLight = ColorGradient.calculateAt(Color.BLACK, GREEN_ON, STEPS, OFF_LIGHT_AT),
        offColorDark = ColorGradient.calculateAt(Color.BLACK, GREEN_ON, STEPS, OFF_DARK_AT),
        editColorDark = ColorGradient.calculateAt(Color.BLACK, GREEN_ON, STEPS, EDIT_DARK_AT),
    ),

    BLUE(
        "blue",
        onColorLight = BLUE_ON,
        offColorLight = ColorGradient.calculateAt(Color.BLACK, BLUE_ON, STEPS, OFF_LIGHT_AT),
        offColorDark = ColorGradient.calculateAt(Color.BLACK, BLUE_ON, STEPS, OFF_DARK_AT),
        editColorDark = Color(0, 0, 60),
    ),

    ORANGE(
        "orange",
        onColorLight = ORANGE_ON,
        offColorLight = ColorGradient.calculateAt(Color.BLACK, ORANGE_ON, STEPS, OFF_LIGHT_AT),
        offColorDark = ColorGradient.calculateAt(Color.BLACK, ORANGE_ON, STEPS, OFF_DARK_AT),
        editColorDark = ColorGradient.calculateAt(Color.BLACK, ORANGE_ON, STEPS, EDIT_DARK_AT),
    ),

	WHITE(
        "white",
        onColorLight = WHITE_ON,
        offColorLight = ColorGradient.calculateAt(Color.BLACK, WHITE_ON, STEPS, OFF_LIGHT_AT),
        offColorDark = ColorGradient.calculateAt(Color.BLACK, WHITE_ON, STEPS, OFF_DARK_AT),
        editColorDark = ColorGradient.calculateAt(Color.BLACK, WHITE_ON, STEPS, EDIT_DARK_AT),
    );

    companion object {

	    /** The name of the [String] in [Properties] representing the persistent name of the [LightColor] to be used as default.*/
	    const val PROP_DEFAULT_LIGHT_COLOR = "antares.view.output.defaultLightColor"

        fun withName(customName: String): LightColor {
            for (c in entries) {
                if (c.customName == customName) {
                    return c
                }
            }
            throw IllegalArgumentException("Unknown LightColor '$customName'")
        }

	    fun getSystemDefault(properties: Properties = BaseModule.properties): LightColor =
		    withName(properties.getString(PROP_DEFAULT_LIGHT_COLOR))
    }

    val onColor: Color get() = if (Themes.get<DrawTheme>().dark) onColorDark else onColorLight

    val offColor: Color get() = if (Themes.get<DrawTheme>().dark) offColorDark else offColorLight

    /** The [Color] to be used in edit mode.*/
    val editColor: Color get() = if (Themes.get<DrawTheme>().dark) editColorDark else editColorLight

    /** The [Color] to be used in execution mode.*/
    fun executeColor(isOn: Boolean): Color = if (isOn) onColor else offColor

    /** A [ColorGradient] from [offColor] to [onColor].*/
	val gradient by lazy { ColorGradient(offColor, onColor) }

    override fun toString(): String =
        when(this) {
            RED -> Translations.getString("element.color.red")
            YELLOW -> Translations.getString("element.color.yellow")
            GREEN -> Translations.getString("element.color.green")
            BLUE -> Translations.getString("element.color.blue")
            ORANGE -> Translations.getString("element.color.orange")
            WHITE -> Translations.getString("element.color.white")
        }
}
