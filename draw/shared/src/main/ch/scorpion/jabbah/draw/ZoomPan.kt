package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Contains the zoom factor and pan origin of a [View].
 */
data class ZoomPan(
	val transform: ViewToModelTransform,
	val zoomFactor: Double,
	val panOrigin: Point2D
) {

	constructor() : this(IdentityViewToModelTransform)

	constructor(transform: ViewToModelTransform) : this(transform, 1.0, 0.0, 0.0)

	constructor(transform: ViewToModelTransform, zoomFactor: Double, panX: Double, panY: Double)
		: this(transform, zoomFactor, Point2D(panX, panY))
}