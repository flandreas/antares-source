package ch.scorpion.jabbah.draw

/**
 * Combines a [ZoomStrategyType] with a particular zoom factor to encapsulate a predefines zoom strategy
 * used by a [View].
 */
data class ZoomStrategy(val type: ZoomStrategyType, val zoomFactor: Double?) {
    constructor(type: ZoomStrategyType) : this(type, null)

    /** Applies this [ZoomStrategy] to a particular [ViewNavigator].*/
    fun apply(navigator: ViewNavigator) = type.apply(navigator, zoomFactor)
}