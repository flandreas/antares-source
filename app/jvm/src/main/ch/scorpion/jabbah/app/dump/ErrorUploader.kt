package ch.scorpion.jabbah.app.dump

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.invocation.ErrorHandlerPlugin
import ch.scorpion.jabbah.base.invocation.InteractiveErrorHandler
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