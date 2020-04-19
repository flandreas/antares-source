package ch.scorpion.jabbah.draw

/**
 * Defines the possible strategies for zooming [Drawable]s in a [View].
 * All [ZoomStrategyType]s center the contents of the [View].
 */
enum class ZoomStrategyType {

    /** Zooms the [View] to normal size, i.e. to 100 percent. */
    NORMAL {
        override fun apply(navigator: ViewNavigator, zoomFactor: Double?) = navigator.panCenter()
    },

    /** Zooms the [View] so that the [Drawable]s entirely fill the available [View] space. */
    FIT {
        override fun apply(navigator: ViewNavigator, zoomFactor: Double?) = navigator.fit()
    },

    /**
     * Zooms and centers the [View] so that the [Drawable]s entirely fill the available space,
     * but avoids to set the zoom factor to more than 100% if not necessary in order to see everything.
     */
    FIT_MAX_NORMAL {
        override fun apply(navigator: ViewNavigator, zoomFactor: Double?) = navigator.fitMaxNormal()
    },

    /** Zooms the [View] to a particular zoom factor. */
    VALUE {
        override fun apply(navigator: ViewNavigator, zoomFactor: Double?) = navigator.panCenter(zoomFactor ?: 1.0)
    },

    NONE {
	    override fun apply(navigator: ViewNavigator, zoomFactor: Double?) { }
    };

    abstract fun apply(navigator: ViewNavigator, zoomFactor: Double?)
}