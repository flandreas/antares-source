package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.drawable.Colorable
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.style.EditStyleType

abstract class AbstractBelowSelectionModel<T: Component>(
	component: T,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	styleType: StyleType = EditStyleType.SELECTION,
	protected val outset: Int = DEF_OUTSET
) : AbstractSelectionModel<T>(component), Colorable {

	companion object {

		/** The number of pixels to add to the [Component]'s bounding box at each side. */
		const val DEF_OUTSET = 5

		/** The arc size of the rounded rectangle.*/
		const val ARC_SIZE = 15
	}

	override var color: CompositeColor = styleProvider.getStyle(styleType).color
		set(value) {
			field = value
			invalidate()
		}
}