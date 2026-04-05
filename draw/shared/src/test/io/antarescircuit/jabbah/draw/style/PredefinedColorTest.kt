package io.antarescircuit.jabbah.draw.style

import io.antarescircuit.jabbah.draw.DrawTestRule
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.graphics.PredefinedColor
import io.antarescircuit.jabbah.draw.graphics.PredefinedColorIdentity.*
import io.antarescircuit.jabbah.draw.graphics.PredefinedColorRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for [PredefinedColorRepository].
 */
class PredefinedColorRepositoryTest {

    lateinit var repository: PredefinedColorRepository

    @BeforeTest
    fun setup() {
	    DrawTestRule.configure()
        repository = PredefinedColorRepository
        repository.clear()
    }

    @Test
    fun shouldRegister() {
        repository.register(PredefinedColor(Black, CompositeColor()))
        assertEquals("black", repository.withIdName("black")?.name)
    }

    @Test
    fun shouldProvideAll() {
        repository.register(PredefinedColor(Black, CompositeColor()))
        repository.register(PredefinedColor(White, CompositeColor()))
        assertEquals(2, repository.provideAll().size)
        assertEquals("black", repository.provideAll()[0].name)
        assertEquals("white", repository.provideAll()[1].name)
    }

	@Test
	fun shouldReturnNullWithIdentityNotFound() {
		repository.register(PredefinedColor(Black, CompositeColor()))
		assertNull(repository.withIdentity(Yellow))
	}

	@Test
	fun shouldReturnNullWithIdNameNotFound() {
		repository.register(PredefinedColor(Black, CompositeColor()))
		assertNull(repository.withIdName("test"))
	}
}