package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import javafx.scene.transform.Affine
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate

/** Adapts a [javafx.scene.transform.Affine] to the [AffineTransform] interface.*/
class AffineTransformFx(val affine: Affine = Affine()) : AffineTransform {

    override val scaleX: Double get() = affine.mxx

    override val scaleY: Double get() = affine.myy

    override val translateX: Double get() = affine.tx

    override val translateY: Double get() = affine.ty

    override val shearX: Double get() = affine.mxy

    override val shearY: Double get() = affine.myx

    override val determinant: Double get() = affine.determinant()

    override fun getMatrix(): DoubleArray {
        throw UnsupportedOperationException("not implemented")
    }

    override fun translate(tx: Double, ty: Double) {
        affine.appendTranslation(tx, ty)
    }

    override fun scale(sx: Double, sy: Double) {
        affine.appendScale(sx, sy)
    }

    override fun rotate(theta: Double) {
        affine.appendRotation(theta)
    }

    override fun transform(ptSrc: Point2D): Point2D {
        val result = affine.transform(ptSrc.x, ptSrc.y)
        return Point2D(result.x, result.y)
    }

    override fun inverseTransform(ptSrc: Point2D): Point2D {
        val result = affine.inverseTransform(ptSrc.x, ptSrc.y)
        return Point2D(result.x, result.y)
    }

    override fun concatenate(Tx: AffineTransform) {
        if (Tx !is AffineTransformFx) {
            throw IllegalArgumentException("not an AffineTransformFx")
        }
        affine.prepend(Tx.affine)
    }

    override fun setToIdentity() {
        affine.setToIdentity()
    }

    override fun setToTranslation(tx: Double, ty: Double) {
        affine.setToTransform(Translate(tx, ty))
    }

    override fun setToRotation(theta: Double, anchorX: Double, anchorY: Double) {
        affine.setToTransform(Rotate(theta, anchorX, anchorY))
    }
}