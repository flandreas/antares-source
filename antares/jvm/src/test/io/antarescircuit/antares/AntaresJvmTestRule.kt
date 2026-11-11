package io.antarescircuit.antares

import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.graph.module.GraphModuleUiJvm

object AntaresJvmTestRule {

    private val desktopBuilder = AntaresDesktopMock()

    fun configure(desktop: AntaresDesktop = desktopBuilder.build()) {
        GraphModuleUiJvm.reset()
        BaseModuleJvm.require()
        AntaresTestRule.configure()

        AntaresModuleJvm(desktop).require()
    }
}