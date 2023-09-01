package ch.scorpion.antares.hdl.vhdl

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.graph.library.LibraryModule
import java.awt.Frame
import java.io.File
import java.io.FileOutputStream
import javax.swing.JFileChooser
import javax.swing.JOptionPane

class ExportVHDLAction : AbstractViewAction("antares.vhdl.action") {

	override fun calculateEnabled(): Boolean {
		if (!super.calculateEnabled()) {
			return false
		}
		val drawable = view!!.mainContent.drawable
		return drawable is DigitalGraphView
	}

	override fun execute(event: ActionEvent) {
		view?.let {
			val mainContent = it.mainContent
			val fileChooser = JFileChooser()
			fileChooser.dialogTitle = name
			fileChooser.selectedFile = File("${mainContent.name}.vhdl")

			var retry = true

			while (retry) {
				if (fileChooser.showSaveDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
					if (fileChooser.selectedFile.exists()) {
						when (JOptionPane.showConfirmDialog(
							Frame.getFrames()[0],
							Translations.getString("antares.vhdl.fileExists.msg"),
							name,
							JOptionPane.YES_NO_CANCEL_OPTION
						)) {
							JOptionPane.CANCEL_OPTION -> return
							JOptionPane.NO_OPTION -> retry = true
							JOptionPane.YES_OPTION -> retry = false
						}
					} else {
						retry = false
					}
				} else {
					return
				}
			}

			export(fileChooser.selectedFile, (it.mainContent.drawable as DigitalGraphView).graph as DigitalGraph)
		}
	}

	private fun export(file: File, circuit: DigitalGraph) {
		InvocationHandler.invoke {
			try {
				FileOutputStream(file).use {
					val printer = CodePrinter(it)
					VHDLGenerator(LibraryModule.libraryHolder.library, printer, generateComment = true).generate(circuit)
					printer.close()
				}
				JOptionPane.showMessageDialog(
					Frame.getFrames()[0],
					Translations.getString("antares.vhdl.success.msg", file.absolutePath),
					name,
					JOptionPane.INFORMATION_MESSAGE)
			} catch (e: HDLException) {
				JOptionPane.showMessageDialog(
					Frame.getFrames()[0],
					e.message,
					name,
					JOptionPane.ERROR_MESSAGE)
			}
		}
	}
}