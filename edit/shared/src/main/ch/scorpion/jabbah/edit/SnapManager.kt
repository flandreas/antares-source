package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * [SnapManager] manages the entire snapping functionality on behalf of an [Editor] and provides the
 * snapping interface to be used by interaction logic that needs snapping.
 *
 * The following example shows how an event handler that translates mouse dragged events into new [Component]
 * locations can use the snapping interface in order to make sure that the new [Component] location is snapped.
 *
 * <pre>
 * class EventHandler : InputEventHandlerAdapter
 * {
 * 	public Drawable mouseDragged(Editor editor, MouseEvent event, double x, double y) {
 * 		Point2D snap = editor.getSnapManager().snap(x, y);
 * 		component.setLocation(x + snap.getX(), y + snap.getY());
 * 	}
 * }
 * </pre>
 *
 * Note that this code should not make a distinction of whether snapping is currently enabled or not. This is up to the
 * [SnapManager] and/or the [Snapper]s managed by it. If snapping is disabled, the returned offset vector
 * will contain zero offsets.
 */
interface SnapManager {

    /** Controls whether snapping is generally enabled in this [SnapManager].*/
    var snapEnabled: Boolean

    /** Controls whether highlighting of snap results is enabled in this [SnapManager].*/
    var highlightEnabled: Boolean

    /**
     * Adds a new [Snapper] at the bottom of the stack of [Snapper]s that is managed by this
     * [SnapManager].
     *
     * The added [Snapper] will have the lowest priority of all [Snapper]s that have been added so far, that
     * is it will only be asked to snap a location if all other snappers have denied to snap the location.
     *
     * @param snapper the new [Snapper] to be added.
     */
    fun addSnapper(snapper: Snapper)

    /**
     * Snaps the point with the specified coordinates and returns the offset that must be added to the specified
     * coordinates in order to satisfy the snapping constraints.
     *
     * @param x the x coordinate of the point to be snapped
     * @param y the y coordinate of the point to be snapped
     * @return the offset that must be added to the point in order to satisfy the snapping constraints.
     */
    fun snap(x: Double, y: Double): Point2D

    /**
     * Snaps the x coordinate of a point and returns the offset that must be added to the specified coordinate in order
     * to satisfy the snapping constraints.
     *
     * The specified y coordinate is only used for highlighting purposes.
     *
     * @param x the x coordinate of a point to be snapped
     * @param y the y coordinate of a point to be snapped, only used for highlighting purposes
     * @return the offset that must be added to the x coordinate in order to satisfy the snapping constraints.
     */
    fun snapX(x: Double, y: Double): Double

    /**
     * Snaps the y coordinate of a point and returns the offset that must be added to the specified coordinate in order
     * to satisfy the snapping constraints.
     *
     * The specified x coordinate is only used for highlighting purposes.
     *
     * @param x the x coordinate of a point to be snapped, only used for highlighting purposes
     * @param y the y coordinate of a point to be snapped
     * @return the offset that must be added to the y coordinate in order to satisfy the snapping constraints.
     */
    fun snapY(x: Double, y: Double): Double

    /**
     * Snaps an [Snappable] and returns the offset that must be added to the specified coordinate in order to
     * satisfy the snapping constraints.
     *
     * This method takes an optional dislocation vector defined by dx and dy, which is virtually added to the location
     * of the [Snappable]. This is especially useful when moving a [Component] should be snapped.
     *
     * @param snappable the [Snappable] to be snapped
     * @param dx the x coordinate of the optional dislocation vector
     * @param dy the y coordinate of the optional dislocation vector
     * @return the offset that must be added to the [Snappable] in order to satisfy the snapping constraints.
     */
    fun snap(snappable: Snappable, dx: Double, dy: Double): Point2D

}