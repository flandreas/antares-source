package io.antarescircuit.jabbah.app.dump

import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.base.invocation.ErrorHandlerPlugin
import io.antarescircuit.jabbah.base.invocation.InteractiveErrorHandler
import java.time.LocalDateTime

object ErrorUploader : AbstractDumpCreator(), ErrorHandlerPlugin {

    /** The time when the last unexpected error occurred. Used to avoid uploading the same error multiple times. */
    private var lastDateTime: LocalDateTime? = null

    override fun initialize(application: DesktopApplication) {
        super.initialize(application)
        InteractiveErrorHandler.registerPlugin(this)
    }

    override fun handleError(t: Throwable) {
        if (lastDateTime != null) {
            return
        }
        lastDateTime = LocalDateTime.now()

        uploadErrorDump(t.message ?: "", false)
    }
}