package ch.scorpion.jabbah.base.geom

/**
 * Represents a 2D affine transformation that performs a linear mapping from 2D coordinates to other 2D coordinates
 * that preserves the "straightness" and "parallelness" of lines.
 *
 * Form of transformation matrix:
 * [m00 m01 m02]
 * [m10 m11 m12]
 * [ 0   0   1 ]
 */
interface AffineTransform {

    /** Returns the X coordinate scaling element (m00) of the affine transformation matrix. */
    val scaleX: Double

    /** Returns the Y coordinate scaling element (m00) of the affine transformation matrix. */
    val scaleY: Double

    /** Returns the transformation matrix as an array of the form m00, m10, m01, m11, m02, m12.*/
    fun getMatrix(): DoubleArray

    /** Concatenates this [AffineTransform] with a translation transformation by the given offsets.*/
    fun translate(tx: Double, ty: Double)

    /** Concatenates this [AffineTransform] with a scaling transformation by the given scale factor.*/
    fun scale(sx: Double, sy: Double)

    /** Concatenates this [AffineTransform] with a rotation transformation by the given angle in radians.*/
    fun rotate(theta: Double)

    /** Applies this [AffineTransform] to the specified [Point2D] and returns the result as a new [Point2D]. */
    fun transform(ptSrc: Point2D): Point2D

    /**
     * Transforms the specified [ptSrc] and stores the result in [ptDst]. If [ptDst] is `null`, a new [Point2D] is created.
     * @return the result of the transformation, which is either the updated [ptDst] or the newly created [Point2D].
     */
    fun transform(ptSrc: Point2D, ptDst: Point2D?): Point2D

    /**
     * Inverse transforms the specified [Point2D] and returns the new [Point2D].
     * @throws NonInvertibleTransformException if this [AffineTransform] is in a non-invertible state
     */
    fun inverseTransform(ptSrc: Point2D): Point2D

    /** Updates this [AffineTransform] by concatenating it with the specified [AffineTransform].*/
    fun concatenate(Tx: AffineTransform)

    /** Resets this [AffineTransform] to the Identity.*/
    fun setToIdentity()

    /** Sets this [AffineTransform] to a translation transformation.*/
    fun setToTranslation(tx: Double, ty: Double)

    /** Sets this [AffineTransform] to a rotation by angle [theta] around the center [anchorX],[anchorY].*/
    fun setToRotation(theta: Double, anchorX: Double, anchorY: Double)
}

class NonInvertibleTransformException(msg: String) : Throwable(msg)
