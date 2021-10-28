package ch.scorpion.jabbah.draw

/**
 * Combines a [ZoomStrategyType] with a particular zoom factor to encapsulate a predefined zoom strategy
 * used by a [View].
 */
data class ZoomStrategy(val type: ZoomStrategyType, val zoomFactor: Double? = null) {

	companion object {
		val NONE = ZoomStrategy(ZoomStrategyType.NONE)
		val NORMAL = ZoomStrategy(ZoomStrategyType.NORMAL)
		val FIT = ZoomStrategy(ZoomStrategyType.FIT)
		val FIT_MAX_NORMAL = ZoomStrategy(ZoomStrategyType.FIT_MAX_NORMAL)
		val CENTER = ZoomStrategy(ZoomStrategyType.CENTER)
	}

    /** Applies this [ZoomStrategy] to a particular [ViewNavigator].*/
    fun apply(navigator: ViewNavigator) = type.apply(navigator, zoomFactor)
}