package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.ColorGradient
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes

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

interface LightColor {

    companion object {

        /** The name of the [String] in [Properties] representing the persistent name of the [LightColor] to be used as default.*/
        const val PROP_DEFAULT_LIGHT_COLOR = "antares.view.output.defaultLightColor"

        val RED = LightColorImpl(
            "red",
            "element.color.red",
            onColorLight = RED_ON,
            offColorLight = ColorGradient.calculateAt(Color.BLACK, RED_ON, STEPS, OFF_LIGHT_AT),
            offColorDark = ColorGradient.calculateAt(Color.BLACK, RED_ON, STEPS, OFF_DARK_AT),
            editColorDark = ColorGradient.calculateAt(Color.BLACK, RED_ON, STEPS, EDIT_DARK_AT))

        val YELLOW = LightColorImpl(
            "yellow",
            "element.color.yellow",
            onColorLight = YELLOW_ON,
            offColorLight = ColorGradient.calculateAt(Color.BLACK, YELLOW_ON, STEPS, OFF_LIGHT_AT),
            offColorDark = ColorGradient.calculateAt(Color.BLACK, YELLOW_ON, STEPS, OFF_DARK_AT),
            editColorDark = ColorGradient.calculateAt(Color.BLACK, YELLOW_ON, STEPS, EDIT_DARK_AT))

        val GREEN = LightColorImpl(
            "green",
            "element.color.green",
            onColorLight = GREEN_ON,
            offColorLight = ColorGradient.calculateAt(Color.BLACK, GREEN_ON, STEPS, OFF_LIGHT_AT),
            offColorDark = ColorGradient.calculateAt(Color.BLACK, GREEN_ON, STEPS, OFF_DARK_AT),
            editColorDark = ColorGradient.calculateAt(Color.BLACK, GREEN_ON, STEPS, EDIT_DARK_AT))

        val BLUE = LightColorImpl(
            "blue",
            "element.color.blue",
            onColorLight = BLUE_ON,
            offColorLight = ColorGradient.calculateAt(Color.BLACK, BLUE_ON, STEPS, OFF_LIGHT_AT),
            offColorDark = ColorGradient.calculateAt(Color.BLACK, BLUE_ON, STEPS, OFF_DARK_AT),
            editColorDark = Color(0, 0, 60))

        val ORANGE = LightColorImpl(
            "orange",
            "element.color.orange",
            onColorLight = ORANGE_ON,
            offColorLight = ColorGradient.calculateAt(Color.BLACK, ORANGE_ON, STEPS, OFF_LIGHT_AT),
            offColorDark = ColorGradient.calculateAt(Color.BLACK, ORANGE_ON, STEPS, OFF_DARK_AT),
            editColorDark = ColorGradient.calculateAt(Color.BLACK, ORANGE_ON, STEPS, EDIT_DARK_AT))

        val WHITE = LightColorImpl(
            "white",
            "element.color.white",
            onColorLight = WHITE_ON,
            offColorLight = ColorGradient.calculateAt(Color.BLACK, WHITE_ON, STEPS, OFF_LIGHT_AT),
            offColorDark = ColorGradient.calculateAt(Color.BLACK, WHITE_ON, STEPS, OFF_DARK_AT),
            editColorDark = ColorGradient.calculateAt(Color.BLACK, WHITE_ON, STEPS, EDIT_DARK_AT))

        val PREDEFINED = listOf(RED, YELLOW, GREEN, BLUE, ORANGE, WHITE)

        fun withName(customName: String): LightColor =
            PREDEFINED.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("Unknown LightColor '$customName'")

        fun getSystemDefault(properties: Properties = BaseModule.properties): LightColor =
            withName(properties.getString(PROP_DEFAULT_LIGHT_COLOR))
    }

    val customName: String

    val onColor: Color

    val offColor: Color

    /** The [Color] to be used in edit mode.*/
    val editColor: Color

    /** The [Color] to be used in execution mode.*/
    fun executeColor(isOn: Boolean): Color

    /** A [ColorGradient] from [offColor] to [onColor].*/
    val gradient: ColorGradient
}

class LightColorImpl(
    override val customName: String,
    private val translationKey: String,
    private val onColorLight: Color,
    private val offColorLight: Color,
    private val editColorLight: Color = onColorLight.withAlpha(EDIT_LIGHT_ALPHA),
    private val onColorDark: Color = onColorLight,
    private val offColorDark: Color = offColorLight,
    private val editColorDark: Color = offColorDark
) : LightColor {

    companion object {

    }

    override val onColor: Color get() = if (Themes.get<DrawTheme>().dark) onColorDark else onColorLight

    override val offColor: Color get() = if (Themes.get<DrawTheme>().dark) offColorDark else offColorLight

    override val editColor: Color get() = if (Themes.get<DrawTheme>().dark) editColorDark else editColorLight

    override fun executeColor(isOn: Boolean): Color = if (isOn) onColor else offColor

    override val gradient by lazy { ColorGradient(offColor, onColor) }

    override fun toString(): String = Translations.getString(translationKey)
}