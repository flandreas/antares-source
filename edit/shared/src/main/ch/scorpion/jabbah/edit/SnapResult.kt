package ch.scorpion.jabbah.edit

/**
 * A utility class used to transport snapping results between the hierarchically structured [Snapper]s.
 *
 * @param x stores the snapped x coordinate
 * @param y stores the snapped y coordinate
 */
class SnapResult(var x: Double = 0.0, var y: Double = 0.0) {

	/**
     * Stores the distance between the original and the snapped x coordinate, i.e. the x distance by which the
     * original point or [Snappable] must be dislocated in order to satisfy the calculated snapping constraints.
     */
    var dx: Double = 0.0

    /**
     * Stores the distance between the original and the snapped y coordinate, i.e. the y distance by which the
     * original point or [Snappable] must be dislocated in order to satisfy the calculated snapping constraints.
     */
    var dy: Double = 0.0

    /** Stores whether the x coordinate was snapped or not.*/
    var snappedX: Boolean = false

    /** Stores whether the y coordinate was snapped or not.*/
    var snappedY: Boolean = false

    /** Stores the [Snapper] that snapped the x coordinate, if any.*/
    var snapperX: Snapper? = null

    /** Stores the [Snapper] that snapped the y coordinate, if any.*/
    var snapperY: Snapper? = null

	/** Stores the [Snappable] that snapped the x coordinate, if any.*/
	var snappableX: Snappable? = null

	/** Stores the [Snappable] that snapped the y coordinate, if any.*/
	var snappableY: Snappable? = null

    /**
     * Resets the state of this [SnapResult] to default, unsnapped values.
     *
     * If a [SnapManager] reuses a single instance of [SnapResult] for multiple snap runs, it should call
     * this method prior to calling the first [Snapper] in the hierarchy.
     */
    fun reset() {
        x = 0.0
        y = 0.0
        dx = 0.0
        dy = 0.0
        snappedX = false
        snappedY = false
        snapperX = null
        snapperY = null
	    snappableX = null
	    snappableY = null
    }

    /**
     * Snaps the x coordinate by adding the specified distance to the current distance stored in this [SnapResult].
     *
     * Marks the x coordinate as being snapped and stores the specified [Snapper] as the one that was responsible
     * for snapping it.
     *
     * @param dx the distance along the x axis by which a [Snapper] dislocated a location in order to be snapped
     * @param x the x coordinated of the snapped location after snapping
     * @param snapper the [Snapper] that did the snapping.
     */
    fun addDx(dx: Double, x: Double, snapper: Snapper) {
        this.dx += dx
        this.x = x
        snapperX = snapper
        snappedX = true
    }

    /**
     * Snaps the y coordinate by adding the specified distance to the current distance stored in this [SnapResult].
     *
     * Marks the y coordinate as being snapped and stores the specified [Snapper] as the one that was responsible
     * for snapping it.
     *
     * @param dy the distance along the y axis by which a [Snapper] dislocated a location in order to be snapped
     * @param y the y coordinated of the snapped location after snapping
     * @param snapper the [Snapper] that did the snapping.
     */
    fun addDy(dy: Double, y: Double, snapper: Snapper) {
        this.dy += dy
        this.y = y
        snapperY = snapper
        snappedY = true
    }
}