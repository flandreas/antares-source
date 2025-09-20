package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.LightColorRenderer
import ch.scorpion.jabbah.graph.model.param.GraphParamValueEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JComponent

class LightColorGraphParamValueEditor : JComboBox<LightColor>(), GraphParamValueEditor {

    init {
        model = DefaultComboBoxModel(LightColor.PREDEFINED.toTypedArray())
        renderer = LightColorRenderer()
    }

    override var paramValue: Any
        get() = selectedItem as LightColor
        set(value) {
            selectedItem = value
        }

    override var changeHandler: (() -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                addActionListener { value.invoke() }
            }
        }

    override var editorEnabled: Boolean
        get() = editor.isEnabled
        set(value) { editor.isEnabled = value }

    override val editor: JComponent get() = this
}