package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.graphics.*

/**
 * Represents a graphical object that has a [Style].
 */
interface Stylable {

    var invalidator: (() -> Unit)?

    var styleProvider: StyleProvider

    /** Returns the custom color, or else the style color, or else the default color from the [Properties]. */
    val color: CompositeColor

    /** Returns the custom foreground color, or else the style foreground color, or else the default foreground color from the [Properties]. */
    val foregroundColor: Color

    /** Returns the custom background color, or else the style background color, or else the default background color from the [Properties]. */
    val backgroundColor: Color

    /** Returns the custom text color, or else the style text color, or else the default text color from the [Properties]. */
    val textColor: Color

    /** Returns the custom font, or else the style font, or else the default font from the [Properties]. */
    val font: Font

    /** Returns the custom stroke, or else the style stroke, or else the default stroke from the [Properties]. */
    val stroke: Stroke

    var styleType: StyleType

    val style: Style

    var filled: Boolean

    var stroked: Boolean

    /** Contains the optional custom [PredefinedColor] that overwrites the color of the [Style].*/
    var customColor: PredefinedColor?

    var customStroke: Stroke?

    var customFont: Font?
}

/**
 * [StylableImpl] is a [Stylable] whose attributes can be individually overwritten.
 *
 * [StylableImpl] contains accessor methods for every type of attribute supported by [Stylable].
 * When accessing an attribute, these accessor methods first check whether the attribute is overwritten. If not
 * overwritten, they return the corresponding attribute of the [Style]. If no [Style] is defined,
 * they return the property value defined in the [Properties] instance.
 *
 * TODO Refactoring: Can the invalidator pattern be implemented using delegated properties?
 */
class StylableImpl(
        override var invalidator: (() -> Unit)? = null,
        styleType: StyleType,
        override var styleProvider: StyleProvider,
        filled: Boolean = true,
        stroked: Boolean = true,
        customColor: PredefinedColor? = null,
        customStroke: Stroke? = null,
        customFont: Font? = null
) : Stylable {

    override var styleType: StyleType = styleType
        set(value) {
            if (field != value) {
                invalidator?.invoke()
                field = value
                invalidator?.invoke()
            }
        }

    override val style: Style get() = styleProvider.getStyle(styleType)

    override var filled: Boolean = filled
        set(value) {
            if (field != value) {
                invalidator?.invoke()
                field = value
                invalidator?.invoke()
            }
        }

    override var stroked: Boolean = stroked
        set(value) {
            if (field != value) {
                invalidator?.invoke()
                field = value
                invalidator?.invoke()
            }
        }

	private var customColorIdentity: PredefinedColorIdentity? = customColor?.identity
    override var customColor: PredefinedColor? get() = if (customColorIdentity != null) PredefinedColorRepository.withIdentity(customColorIdentity!!) else null
        set(value) {
            if (customColorIdentity != value?.identity) {
                invalidator?.invoke()
	            customColorIdentity = value?.identity
                invalidator?.invoke()
            }
        }

    override var customStroke: Stroke? = customStroke
        set(value) {
            if (field != value) {
                invalidator?.invoke()
                field = value
                invalidator?.invoke()
            }
        }

    override var customFont: Font? = customFont
        set(value) {
            if (field != value) {
                invalidator?.invoke()
                field = value
                invalidator?.invoke()
            }
        }

    /** TODO This method can be used very often while drawing. Find an implementation that performs better.*/
    override val color: CompositeColor
        get() = CompositeColor(
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            textColor =  textColor)

    override val foregroundColor: Color
        get() {
            if (customColor != null) {
                return customColor!!.color.foregroundColor
            }
            return style.color.foregroundColor
        }

    override val backgroundColor: Color
        get() {
            if (customColor != null) {
                return customColor!!.color.backgroundColor
            }
            return style.color.backgroundColor
        }

    override val textColor: Color
        get() {
            if (customColor != null) {
                return customColor!!.color.textColor
            }
            return style.color.textColor
        }

    override val font: Font
        get() {
            if (customFont != null) {
                return customFont!!
            }
            return style.font
        }

    override val stroke: Stroke
        get() {
            if (customStroke != null) {
                return customStroke!!
            }
            return style.stroke
        }
}
