package ch.scorpion.antares.view

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Theme].
 */
class ThemeTest {

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun defaultShouldBeCurrent() {
        Theme.current.word
    }
}