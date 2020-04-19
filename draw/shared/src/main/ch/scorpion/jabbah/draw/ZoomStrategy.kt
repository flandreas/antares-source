package ch.scorpion.jabbah.draw

/**
 * Combines a [ZoomStrategyType] with a particular zoom factor to encapsulate a predefines zoom strategy
 * used by a [View].
 */
data class ZoomStrategy(val type: ZoomStrategyType, val zoomFactor: Double? = null) {

	companion object {
		val NONE = ZoomStrategy(ZoomStrategyType.NONE)
	}

    /** Applies this [ZoomStrategy] to a particular [ViewNavigator].*/
    fun apply(navigator: ViewNavigator) = type.apply(navigator, zoomFactor)
}