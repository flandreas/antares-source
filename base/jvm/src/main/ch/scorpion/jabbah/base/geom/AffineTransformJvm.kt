package ch.scorpion.jabbah.base.geom


/**
 * Adapts a [java.awt.geom.AffineTransform] to the [AffineTransform] interface.
 */
class AffineTransformJvm(val transform: java.awt.geom.AffineTransform) : AffineTransform {

    override fun getMatrix(): DoubleArray {
        val matrix = doubleArrayOf()
        transform.getMatrix(matrix)
        return matrix
    }

    constructor() : this(java.awt.geom.AffineTransform())

    /** ---- [AffineTransform] interface */

    override val scaleX: Double get() = transform.scaleX

    override val scaleY: Double get() = transform.scaleY

    override fun translate(tx: Double, ty: Double) = transform.translate(tx, ty)

    override fun scale(sx: Double, sy: Double) = transform.scale(sx, sy)


    override fun rotate(theta: Double) = transform.rotate(theta)

    override fun transform(ptSrc: Point2D): Point2D {
        val result = transform.transform(java.awt.geom.Point2D.Double(ptSrc.x, ptSrc.y), null)
        return Point2D(result.x, result.y)
    }

    override fun inverseTransform(ptSrc: Point2D): Point2D {
        val result = transform.inverseTransform(java.awt.geom.Point2D.Double(ptSrc.x, ptSrc.y), null)
        return Point2D(result.x, result.y)
    }

    override fun concatenate(Tx: AffineTransform) {
        if (Tx !is AffineTransformJvm) {
            throw IllegalArgumentException("not a AffineTransformJvm")
        }
        this.transform.concatenate(Tx.transform)
    }

    override fun setToIdentity() {
        transform.setToIdentity()
    }

    override fun setToRotation(theta: Double, anchorX: Double, anchorY: Double) {
        transform.setToRotation(theta, anchorX, anchorY)
    }

    override fun setToTranslation(tx: Double, ty: Double) {
        transform.setToTranslation(tx, ty)
    }

    override fun transform(ptSrc: Point2D, ptDst: Point2D?): Point2D {
        val dest = if (ptDst != null) java.awt.geom.Point2D.Double(ptDst.x, ptDst.y) else null
        val result = transform.transform(java.awt.geom.Point2D.Double(ptSrc.x, ptSrc.y), dest)

        if (ptDst != null) {
            ptDst.setLocation(result.x, result.y)
            return ptDst
        }
        return Point2D(result.x, result.y)
    }
}
