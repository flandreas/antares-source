package io.antarescircuit.jabbah.edit.properties

import com.l2fprod.common.beans.editor.StringPropertyEditor
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValueParser
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit

class MagnitudeValuePropertyEditor(
    private val errorCallback: (IllegalArgumentException) -> Unit,
    vararg units: SIUnit
) : StringPropertyEditor() {

    val units: Array<SIUnit> = arrayOf(*units)

    override fun getValue(): Any {
        return try {
            MagnitudeValueParser.parseWithUnits(super.value as String, *this.units)
        } catch (e: IllegalArgumentException) {
            errorCallback(e)
        }
    }
}