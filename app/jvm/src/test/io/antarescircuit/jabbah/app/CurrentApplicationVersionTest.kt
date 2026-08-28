package io.antarescircuit.jabbah.app

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.io.StoreReader
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrentApplicationVersionTest {

    @BeforeTest
    fun beforeTest() {
        Translations.addBundle("jabbah-app")
    }

    @Test
    fun shouldAcceptOlderDataVersion() {
        CurrentApplicationVersion.codeVersion = ApplicationVersion(2, 2, 0)
        CurrentApplicationVersion.dataVersion = ApplicationVersion(2, 1, 0)
        val reader = mock<StoreReader>()
        every { reader.hasAttribute(CurrentApplicationVersion.NEW_PERSISTENT_NAME) } returns true
        every { reader.readString(any())} returns "2.0.0"

        CurrentApplicationVersion.check(reader)
    }

    @Test
    fun shouldRejectNewerDataVersion() {
        val e = assertFailsWith<ApplicationTooOldException> {
            CurrentApplicationVersion.codeVersion = ApplicationVersion(2, 2, 0)
            CurrentApplicationVersion.dataVersion = ApplicationVersion(2, 1, 0)
            val reader = mock<StoreReader>()
            every { reader.hasAttribute(CurrentApplicationVersion.NEW_PERSISTENT_NAME) } returns true
            every { reader.readString(any())} returns "3.0.0"

            CurrentApplicationVersion.check(reader)
        }
        assertEquals(e.message, "Application version 2.2.0 too old\nto read data produced by version 3.0.0.\n\nFirst install the latest version of the application.")
    }
}