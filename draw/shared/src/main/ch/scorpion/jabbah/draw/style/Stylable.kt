package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.draw.graphics.*

/**
 * Represents a graphical object that has a [Style].
 */
interface Stylable {

	companion object {
		const val BASE_KEY_CUSTOM_COLOR = "edit.property.color"
		const val BASE_KEY_CUSTOM_STROKE = "edit.property.stroke"
		const val BASE_KEY_SHADOW = "edit.property.shadow"
		const val BASE_KEY_FILLED = "edit.property.filled"
		const val BASE_KEY_STROKED = "edit.property.stroked"
	}

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

	val shadow: Boolean

    /** Contains the optional custom [PredefinedColor] that overwrites the color of the [Style].*/
    var customColor: PredefinedColor?

    var customStroke: PredefinedStroke?

    var customFont: Font?

	var customShadow: Boolean?
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
    customStroke: PredefinedStroke? = null,
    customFont: Font? = null,
    customShadow: Boolean? = null
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

	private var customStrokeIdentity: PredefinedStrokeIdentity? = customStroke?.identity
    override var customStroke: PredefinedStroke? get() = if (customStrokeIdentity != null) PredefinedStrokeRepository.withIdentity(customStrokeIdentity!!) else null
        set(value) {
	        if (customStrokeIdentity != value?.identity) {
		        invalidator?.invoke()
				customStrokeIdentity = value?.identity
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

	override var customShadow: Boolean? = customShadow
		set(value) {
			if (field != value) {
				val effectiveValue = if (value == shadow) null else value
				invalidator?.invoke()
				field = effectiveValue
				invalidator?.invoke()
			}
		}

    /** TODO This method can be used very often while drawing. Find an implementation that performs better.*/
    override val color: CompositeColor
        get() = CompositeColor(
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            textColor =  textColor)

    override val foregroundColor: Color get() = customColor?.color?.foregroundColor ?: style.color.foregroundColor

    override val backgroundColor: Color get() = customColor?.color?.backgroundColor ?: style.color.backgroundColor

    override val textColor: Color get() = customColor?.color?.textColor ?: style.color.textColor

    override val font: Font get() = customFont ?: style.font

    override val stroke: Stroke get() = customStroke?.stroke ?: style.stroke

	override val shadow: Boolean get() = customShadow ?: style.shadow
}
