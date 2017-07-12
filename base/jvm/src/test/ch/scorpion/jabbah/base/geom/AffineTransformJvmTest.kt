package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AffineTransformJvm].
 */
class AffineTransformJvmTest {

    @Before
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldAddTranslation() {
        val transform = AffineTransformJvm()
        transform.translate(100.0, 50.0)
        transform.translate(20.0, 40.0)
        assertEquals(Point2D(220.0, 190.0), transform.transform(Point2D(100.0, 100.0)))
    }
}