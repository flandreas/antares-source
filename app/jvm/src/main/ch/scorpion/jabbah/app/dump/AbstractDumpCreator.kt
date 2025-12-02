package ch.scorpion.jabbah.app.dump

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.UserActionTrail
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.output.StringBuilderWriter
import java.io.File

abstract class AbstractDumpCreator {

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
        BaseModuleJvm.unexpectedErrorService.sendUnexpectedError(writer.toString())
    }

    @OptIn(DelicateCoroutinesApi::class)
    protected fun uploadErrorDump(description: String, includeWorkspace: Boolean) {
        if (EditAuthModule.userHolder.user.isDeveloper) {
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val path = storeDumpFile(includeWorkspace)
            if (!BaseModuleJvm.unexpectedErrorService.sendErrorDump(path)) {
                // Uploading error dump can fail if dump is too large
                sendError(description)
            }
        }
    }

    private fun storeDumpFile(includeWorkspace: Boolean): String {
        val file = File.createTempFile("dump", ".zip")
        file.deleteOnExit()

        SystemDumpService().createDump(application, file.toPath(), includeWorkspace)

        return file.absolutePath
    }
}