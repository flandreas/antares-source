package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/** Unit tests for [Settings].*/
class SettingsTest {

    @BeforeTest
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldGetInt() {
        val settings = Settings()
        settings.set("test", 42)
        assertEquals(42, settings.getInt("test", 100))
    }

    @Test
    fun shouldGetDefaultInt() {
        val settings = Settings()
        assertEquals(100, settings.getInt("test", 100))
    }
}