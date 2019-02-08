package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/**
 * Unit tests for [AffineTransformJvm].
 */
class AffineTransformJvmTest {

    @BeforeTest
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