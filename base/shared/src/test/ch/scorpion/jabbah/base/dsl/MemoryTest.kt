package ch.scorpion.jabbah.base.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryTest {

	private val memory = Memory()

	@Test
	fun shouldStoreValueInGlobalScope() {
		val variable = variable("a")
		memory.define(variable)
		memory.setValue(variable, 42)

		assertEquals(42, memory.getValue(variable))
	}

	@Test
	fun shouldShadowValueInSubScope() {
		val globalA = variable("a")
		memory.define(globalA)
		memory.setValue(globalA, 42)

		val localA = variable("a")
		memory.enterScope("block")
		memory.define(localA)
		memory.setValue(localA, 99)

		assertEquals(99, memory.getValue(localA))
	}

	@Test
	fun shouldReadGlobalInSubScope() {
		val globalA = variable("a")
		memory.define(globalA)
		memory.setValue(globalA, 42)

		val localB = variable("b")
		memory.enterScope("block")
		memory.define(localB)
		memory.setValue(localB, 99)

		assertEquals(42, memory.getValue(globalA))
	}

	@Test
	fun shouldShadowInSubScope() {
		val globalA = variable("a")
		memory.define(globalA)
		memory.setValue(globalA, 42)

		val localA = variable("a")
		memory.enterScope("block")
		memory.define(localA)
		memory.setValue(localA, 99)
		memory.exitScope(globalA)

		assertEquals(42, memory.getValue(globalA))
	}

	private fun variable(name: String): Variable =
		Variable(CodeLocation(0, 0, 0), Token(TokenType.VAR, name))
}