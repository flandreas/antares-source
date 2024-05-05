package ch.scorpion.jabbah.graph.model.param

import javax.swing.JComponent
import javax.swing.JTextField

class StringGraphParamValueEditor : JTextField(), GraphParamValueEditor {

    override var paramValue: Any
        get() = text
        set(value) {
            text = value as String
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