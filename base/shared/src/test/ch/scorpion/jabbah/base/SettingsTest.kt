package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for [Settings].*/
class SettingsTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldGetInt() {
        val settings = Settings()
        settings.set("test", 42)
        assertThat(settings.getInt("test", 100), `is`(42));
    }

    @Test
    fun shouldGetDefaultInt() {
        val settings = Settings()
        assertThat(settings.getInt("test", 100), `is`(100));
    }
}