package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.StringUtils
import com.l2fprod.common.beans.editor.LongPropertyEditor
import javax.swing.JTextField

/**
 * A [LongPropertyEditor] that allows to enter a `null` value by clearing
 * the text field.
 */
@Suppress("unused") // Reflection
class LongOptionalPropertyEditor(
    private val isOptional: Boolean = false
) : LongPropertyEditor() {

    override fun getValue(): Any? {
        val text = (editor as JTextField).text
        if (isOptional && StringUtils.isBlank(text)) {
            return null
        }
        return super.getValue()
    }

    override fun setValue(value: Any?) {
        if (isOptional && value == null) {
            (editor as JTextField).text = ""
        } else {
            super.setValue(value)
        }
    }
}