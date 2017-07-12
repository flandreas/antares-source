package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.MathClass
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AffineTransformImpl].
 */
class AffineTransformImplTest {

    @Before
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldTranslate() {
        val transform = AffineTransformImpl()
        transform.translate(100.0, 50.0)
        assertEquals(Point2D(200.0, 150.0), transform.transform(Point2D(100.0, 100.0)))
    }

    @Test
    fun shouldScale() {
        val transform = AffineTransformImpl()
        transform.scale(2.0, 2.0)
        assertEquals(Point2D(10.0, 8.0), transform.transform(Point2D(5.0, 4.0)))
    }

    @Test
    fun shouldRotate() {
        val transform = AffineTransformImpl()
        transform.rotate(MathClass.PI)
        assertEquals(Point2D(-100.0, -0.0), transform.transform(Point2D(100.0, 0.0)))
    }

    @Test
    fun shouldIgnoreZeroRotation() {
        val transform = AffineTransformImpl()
        transform.rotate(0.0)
        assertEquals(Point2D(200.0, 150.0), transform.transform(Point2D(200.0, 150.0)))
    }

    @Test
    fun shouldBeReversible() {
        val transform = AffineTransformImpl()
        transform.translate(100.0, 50.0)
        transform.rotate(MathClass.PI)
        transform.rotate(-MathClass.PI)
        transform.translate(-100.0, -50.0)
        assertEquals(Point2D(200.0, 150.0), transform.transform(Point2D(200.0, 150.0)))
    }

    @Test
    fun shouldBeReversible2() {
        val transform = AffineTransformImpl()
        transform.translate(100.0, 50.0)
        transform.rotate(0.0)
        transform.rotate(-0.0)
        transform.translate(-100.0, -50.0)
        assertEquals(Point2D(200.0, 150.0), transform.transform(Point2D(200.0, 150.0)))
    }

    @Test
    fun shouldAddTranslation() {
        val transform = AffineTransformImpl()
        transform.translate(100.0, 50.0)
        transform.translate(20.0, 40.0)
        assertEquals(Point2D(220.0, 190.0), transform.transform(Point2D(100.0, 100.0)))

    }

}