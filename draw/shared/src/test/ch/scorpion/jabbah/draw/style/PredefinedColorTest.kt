package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.graphics.PredefinedColorIdentity
import org.junit.Assert.*
import org.junit.Test
import org.hamcrest.CoreMatchers.*
import org.junit.Before

/**
 * Unit tests for [PredefinedColorRepository].
 */
class PredefinedColorRepositoryTest {

    lateinit var repository: PredefinedColorRepository

    @Before
    fun setup() {
        BaseModuleJvm.require()
        repository = PredefinedColorRepository
        repository.clear()
    }

    @Test
    fun shouldRegister() {
        repository.register(PredefinedColor(PredefinedColorIdentity.Black, CompositeColor()))
        assertThat(repository.withIdName("black")?.name, `is`("black"))
    }

    @Test
    fun shouldProvideAll() {
        repository.register(PredefinedColor(PredefinedColorIdentity.Black, CompositeColor()))
        repository.register(PredefinedColor(PredefinedColorIdentity.White, CompositeColor()))
        assertThat(repository.provideAll().size, `is`(2))
        assertThat(repository.provideAll()[0].name, `is`("black"))
        assertThat(repository.provideAll()[1].name, `is`("white"))
    }
}