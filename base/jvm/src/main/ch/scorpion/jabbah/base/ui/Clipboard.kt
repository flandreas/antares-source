package ch.scorpion.jabbah.base.ui

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

actual object Clipboard {

	actual fun getStringContents(): String? {
		val transferable = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
		if (transferable == null) {
			Toolkit.getDefaultToolkit().beep()
			return null
		}
		return transferable.getTransferData(DataFlavor.stringFlavor) as String
	}

	actual fun setStringContents(contents: String) {
		Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(contents), null)
	}
}