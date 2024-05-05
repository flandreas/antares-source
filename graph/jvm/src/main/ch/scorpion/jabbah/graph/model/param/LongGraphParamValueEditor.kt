package ch.scorpion.jabbah.graph.model.param

import java.text.DecimalFormat
import javax.swing.JComponent
import javax.swing.JFormattedTextField

class LongGraphParamValueEditor : JFormattedTextField(DecimalFormat.getIntegerInstance()), GraphParamValueEditor {

    override var paramValue: Any
        get() = super.getValue()
        set(value) {
            super.setValue(value)
        }

    override var changeHandler: (() -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                addActionListener { value.invoke() }
            }
        }

    override var editorEnabled: Boolean
        get() = isEnabled
        set(value) {
            isEnabled = value
        }

    override val editor: JComponent get() = this
}