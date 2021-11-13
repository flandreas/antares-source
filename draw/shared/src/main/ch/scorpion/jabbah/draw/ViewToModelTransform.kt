package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Provides methods for transforming coordinates from view space to model space, and vice versa.
 */
interface ViewToModelTransform {

    /** Transforms a x coordinate from view space to model space by applying the [View]'s zoom factor and pan origin.*/
    fun viewToModelX(x: Double): Double

    /** Transforms a y coordinate from view space to model space by applying the [View]'s zoom factor and pan origin.*/
    fun viewToModelY(y: Double): Double

    /** Transforms a [Point2D] from view space to model space by applying the [View]'s zoom factor and pan origin.*/
    fun viewToModel(p: Point2D): Point2D

    fun viewToModelLength(length: Double): Double

    /** Transforms a [Point2D] from model space to view space by applying the [View]'s zoom factor and pan origin.*/
    fun modelToView(p: Point2D): Point2D

    fun modelToViewX(x: Double): Double

    fun modelToViewY(y: Double): Double

    /**
     * Transforms a [Point2D] from model space to view space by applying the specified zoom factor and the
     * [View]'s current pan origin.
     *
     * Can be used for calculating new pan origins when the zoom factor changes, for example to implement "zoom to
     * normal / centered".
     */
    fun modelToView(p: Point2D, zoomFactor: Double): Point2D

    fun modelToViewLength(length: Double): Double

	fun modelToViewLength(length: Double, zoomFactor: Double): Double
}

object IdentityViewToModelTransform : ViewToModelTransform {

    override fun viewToModelX(x: Double): Double = x

    override fun viewToModelY(y: Double): Double = y

    override fun viewToModel(p: Point2D): Point2D = p

	override fun viewToModelLength(length: Double): Double = length

    override fun modelToView(p: Point2D): Point2D = p

	override fun modelToViewX(x: Double): Double = x

	override fun modelToViewY(y: Double): Double = y

    override fun modelToView(p: Point2D, zoomFactor: Double): Point2D = p

	override fun modelToViewLength(length: Double): Double = length

	override fun modelToViewLength(length: Double, zoomFactor: Double): Double = length
}