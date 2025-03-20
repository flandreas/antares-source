package ch.scorpion.jabbah.graph.model.param

import ch.scorpion.jabbah.base.LongValue
import ch.scorpion.jabbah.base.LongValueImpl
import java.text.DecimalFormat
import javax.swing.JComponent
import javax.swing.JFormattedTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LongValueGraphParamValueEditor : JFormattedTextField(DecimalFormat.getIntegerInstance()), GraphParamValueEditor {

    override var paramValue: Any
        get() = LongValueImpl((super.getValue() as Number).toLong())
        set(value) {
            super.setValue((value as LongValue).value)
        }


    override var changeHandler: (() -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                document.addDocumentListener(documentListener)
            }
        }

    override var editorEnabled: Boolean
        get() = isEnabled
        set(value) {
            isEnabled = value
        }

    override val editor: JComponent get() = this

    private val documentListener = object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) { update() }

        override fun removeUpdate(e: DocumentEvent?) { update() }

        override fun changedUpdate(e: DocumentEvent?) { update() }

        private fun update() {
            changeHandler?.invoke()
        }
    }
}