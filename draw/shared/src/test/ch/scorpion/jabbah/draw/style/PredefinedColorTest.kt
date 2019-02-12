package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColorIdentity
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
        repository.register(PredefinedColor(PredefinedColorIdentity.Black, CompositeColor()))
        assertEquals("black", repository.withIdName("black")?.name)
    }

    @Test
    fun shouldProvideAll() {
        repository.register(PredefinedColor(PredefinedColorIdentity.Black, CompositeColor()))
        repository.register(PredefinedColor(PredefinedColorIdentity.White, CompositeColor()))
        assertEquals(2, repository.provideAll().size)
        assertEquals("black", repository.provideAll()[0].name)
        assertEquals("white", repository.provideAll()[1].name)
    }
}