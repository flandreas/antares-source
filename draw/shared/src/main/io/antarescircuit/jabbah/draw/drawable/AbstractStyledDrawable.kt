package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.draw.style.*

/**
 * Enhances [AbstractDrawable] with support for [Stylable].
 */
abstract class AbstractStyledDrawable(
	stylable: Stylable
) : AbstractDrawable(), Stylable by stylable {

	constructor(
		styleType: StyleType,
		styleProvider: StyleProvider
	) : this(StylableImpl(styleType = styleType, styleProvider = styleProvider))

	init {
		// Cannot be supplied in the constructor
		stylable.invalidator = { invalidate() }
	}
}
