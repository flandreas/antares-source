package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.MathClass
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for [Geometry].*/
class GeometryTest {

	@Before
	fun setup() {
		BaseModuleJvm.require()
	}

	@Test
	fun shouldCalculateAngleLocatedInOrigin() {
		assertThat(Geometry.angle(0.0, 0.0, 10.0, -10.0), `is`(MathClass.PI_4))
		assertThat(Geometry.angle(0.0, 0.0, 10.0, 10.0), `is`(7 * MathClass.PI_4))
	}

	@Test
	fun shouldCalculateAngleNotLocatedInOrigin() {
		assertThat(Geometry.angle(100.0, 0.0, 110.0, -10.0), `is`(MathClass.PI_4))
		assertThat(Geometry.angle(0.0, 100.0, 10.0, 110.0), `is`(7 * MathClass.PI_4))
	}

	@Test
	fun shouldWrapAngle() {
		assertThat(Geometry.wrapAngle(0.0), `is`(0.0))
		assertThat(Geometry.wrapAngle(MathClass.TWO_PI), `is`(0.0))
		assertThat(Geometry.wrapAngle(2 * MathClass.TWO_PI), `is`(0.0))
		assertThat(Geometry.wrapAngle(1.5 * MathClass.TWO_PI), `is`(MathClass.PI))
	}

	@Test
	fun shouldWrapNegativeAngle() {
		assertThat(Geometry.wrapAngle(-MathClass.PI_2), `is`(3 * MathClass.PI_2))
		assertThat(Geometry.wrapAngle(-MathClass.TWO_PI - MathClass.PI_2), `is`(3 * MathClass.PI_2))
	}

	@Test
	fun shouldBeAntiClockwiseAngleChange() {
		assertThat(Geometry.isClockwiseAngleChange(0.0, 0.0), `is`(false))
		assertThat(Geometry.isClockwiseAngleChange(0.0, MathClass.PI_2), `is`(false))
		assertThat(Geometry.isClockwiseAngleChange(3 * MathClass.TWO_PI, MathClass.PI_2), `is`(false))
	}

	@Test
	fun shouldBeClockwiseAngleChange() {
		assertThat(Geometry.isClockwiseAngleChange(MathClass.PI_2, 0.0), `is`(true))
		assertThat(Geometry.isClockwiseAngleChange(MathClass.PI_2, 3 * MathClass.TWO_PI), `is`(true))
	}
}