package ch.scorpion.jabbah.app.dump

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.invocation.ErrorHandlerPlugin
import ch.scorpion.jabbah.base.invocation.InteractiveErrorHandler

object ErrorUploader : AbstractDumpCreator(), ErrorHandlerPlugin {

    override fun initialize(application: DesktopApplication) {
        super.initialize(application)
        InteractiveErrorHandler.registerPlugin(this)
    }

    override fun handleError(t: Throwable) {
        uploadErrorDump(t.message ?: "", false)
    }
}