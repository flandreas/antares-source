package io.antarescircuit.antares

import io.antarescircuit.jabbah.base.module.BaseModuleJvm

object AntaresJvmTestRule {

    private val desktopBuilder = AntaresDesktopMock()

    fun configure(desktop: AntaresDesktop = desktopBuilder.build()) {
        BaseModuleJvm.require()
        AntaresTestRule.configure()

        AntaresModuleJvm(desktop).require()
    }
}