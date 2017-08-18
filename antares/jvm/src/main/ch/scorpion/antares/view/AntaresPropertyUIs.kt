package ch.scorpion.antares.view

import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.output.LightColor
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import ch.scorpion.jabbah.base.swing.ColorIcon
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import javax.swing.JComboBox
import javax.swing.JList

/**
 * Renders a [LightColor] in a [JList].
 */
class LightColorRenderer : EnumRenderer<LightColor>() {

    private val icon = ColorIcon()

    override fun setValue(value: LightColor) {
        icon.backgroundColor = Graphics2DJvm.toAwtColor(value.onColor)
        text = value.toString()
        setIcon(icon)
    }
}

class LightColorEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(LightColor.values())
        (editor as JComboBox<*>).renderer = LightColorRenderer()
    }
}

class HandednessEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Handedness.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Handedness>()
    }
}

class InputCountEditor(filter: (InputCount) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(InputCount.values().filter { filter.invoke(it) }.toTypedArray())
        (editor as JComboBox<*>).renderer = EnumRenderer<InputCount>()
    }
}

class InputPortNumberEditor(filter: (InputPortNumber) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(InputPortNumber.values().filter { filter.invoke(it) }.toTypedArray())
        (editor as JComboBox<*>).renderer = EnumRenderer<InputPortNumber>()
    }
}

class BitWidthEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(BitWidth.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<BitWidth>()
    }
}

class LogicEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Logic.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Logic>()
    }
}

class TriggerEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Trigger.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Trigger>()
    }
}

class DigitalSignalRepresentationEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(DigitalSignalRepresentation.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<DigitalSignalRepresentation>()
    }
}

class SevenSegmentDisplaySchemeEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(SevenSegmentDisplayScheme.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<SevenSegmentDisplayScheme>()
    }
}

class OutputAnnotationEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(OutputAnnotation.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<OutputAnnotation>()
    }
}

