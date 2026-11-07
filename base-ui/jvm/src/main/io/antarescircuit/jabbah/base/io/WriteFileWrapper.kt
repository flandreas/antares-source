package io.antarescircuit.jabbah.base.io

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import java.awt.Frame
import java.io.IOException
import javax.swing.JOptionPane

object WriteFileWrapper {

    private val LOG by logger(WriteFileWrapper::class)

    fun wrap(name: String, function: () -> Unit) {
        try {
            function()
        }  catch (@Suppress("unused") e: IOException) {
            LOG.error("Error while writing file in action '$name'", e)
            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                Translations.getString("base.writeFile.ioError.msg"),
                name,
                JOptionPane.ERROR_MESSAGE
            )
        } catch (e: Throwable) {
            LOG.error("Error while writing file in action '$name'", e)
            JOptionPane.showMessageDialog(
                Frame.getFrames()[0],
                Translations.getString("base.writeFile.generalError.msg", e.message ?: ""),
                name,
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
}