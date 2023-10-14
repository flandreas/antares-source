package ch.scorpion.antares.model.testcase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ValueTest {

	@Test
	fun testCompare() {
		assertEquals(Value(99UL), Value(99UL))
		assertEquals(Value(0UL, Value.Type.DONT_CARE), Value(99UL))
		assertEquals(Value(0UL, Value.Type.UNDEFINED), Value(99UL, Value.Type.UNDEFINED))

		assertNotEquals(Value(99UL), Value(88UL))
	}
}