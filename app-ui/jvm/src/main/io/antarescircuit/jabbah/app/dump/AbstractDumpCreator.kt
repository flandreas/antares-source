package io.antarescircuit.jabbah.app.dump

import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.base.UserActionTrail
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModuleUiJvm
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.output.StringBuilderWriter
import java.io.File

abstract class AbstractDumpCreator {

    companion object {
        private val LOG by logger(AbstractDumpCreator::class)
    }

    protected lateinit var application: DesktopApplication

    open fun initialize(application: DesktopApplication) {
        this.application = application
    }

    private suspend fun sendError(description: String) {
        val versionId = application.aboutInfo.version.toString()
        val writer = StringBuilderWriter()
        if (versionId.isNotBlank()) {
            writer.appendLine("Version: $versionId")
        }
        writer.append(UserActionTrail.toString())
        writer.append(description)
        BaseModuleUiJvm.unexpectedErrorService.sendUnexpectedError(writer.toString())
    }

    @OptIn(DelicateCoroutinesApi::class)
    protected fun uploadErrorDump(description: String, includeWorkspace: Boolean) {
        if (EditAuthModule.userHolder.user.isDeveloper) {
            return
        }

        // Capture the model on the calling (EDT) thread; only file and network I/O happen in the background.
        val currentStorable = try {
            SystemDumpService().serializeCurrentStorable(application)
        } catch (e: Exception) {
            LOG.error("Could not serialize current state for error dump", e)
            null
        }

        GlobalScope.launch(Dispatchers.IO) {
            val path = storeDumpFile(includeWorkspace, currentStorable)
            if (!BaseModuleUiJvm.unexpectedErrorService.sendErrorDump(path)) {
                // Uploading error dump can fail if dump is too large
                sendError(description)
            }
        }
    }

    private fun storeDumpFile(includeWorkspace: Boolean, currentStorable: String?): String {
        val file = File.createTempFile("dump", ".zip")
        file.deleteOnExit()

        SystemDumpService().createDump(application, file.toPath(), includeWorkspace, currentStorable)

        return file.absolutePath
    }
}
