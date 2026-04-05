package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.draw.drawable.Colorable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.style.EditStyleType

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