package ch.scorpion.jabbah.base

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class InheritanceTest {

	@Test
	fun shouldHaveCommonSuperClass() {
		assertSame(A::class, Inheritance.commonSuperClass(setOf(ABC::class, AC::class)))
	}

	@Test
	fun shouldNotHaveCommonSuperClass() {
		assertNull(Inheritance.commonSuperClass(setOf(A::class, B::class)))
	}

	@Test
	fun shouldHaveAllSameSuperClass() {
		assertSame(A::class, Inheritance.commonSuperClass(setOf(A::class, A::class)))
	}

	private open class A
	private open class AB : A()
	private open class ABC : AB()
	private open class AC : A()
	private class B
}