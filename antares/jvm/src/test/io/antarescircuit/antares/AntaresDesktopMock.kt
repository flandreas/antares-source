package io.antarescircuit.antares

import io.antarescircuit.jabbah.app.Environment
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

class AntaresDesktopMock {

    private val antaresDesktop = mock<AntaresDesktop>(MockMode.autofill)

    init {
        every { antaresDesktop.environment } returns Environment.Development
    }

        fun build(): AntaresDesktop = antaresDesktop
}