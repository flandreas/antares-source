package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.base.module.BaseModuleJvm
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
        repository.register(PredefinedColor("a", "predefinedColor.a", CompositeColor()))
        assertThat(repository.withName("a")?.name, `is`("a"))
    }

    @Test
    fun shouldProvideAll() {
        repository.register(PredefinedColor("a", "predefinedColor.a", CompositeColor()))
        repository.register(PredefinedColor("b", "predefinedColor.b", CompositeColor()))
        assertThat(repository.provideAll().size, `is`(2))
        assertThat(repository.provideAll()[0].name, `is`("a"))
        assertThat(repository.provideAll()[1].name, `is`("b"))
    }
}