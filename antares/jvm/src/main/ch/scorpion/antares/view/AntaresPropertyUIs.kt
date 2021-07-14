package ch.scorpion.antares.view

import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.input.JoystickDeflection
import ch.scorpion.antares.view.port.DigitalPortViewStyle
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.ColorIcon
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.JComboBox
import javax.swing.JList

/**
 * Renders a [LightColor] in a [JList].
 */
open class LightColorRenderer : EnumRenderer<LightColor>() {

    private val icon = ColorIcon()

    override fun setValue(value: LightColor?) {
	    if (value == null) {
		    icon.backgroundColor = Graphics2DJvm.toAwtColor(LightColor.getSystemDefault().onColor)
		    text = Translations.getString("element.color.none")
	    } else {
		    icon.backgroundColor = Graphics2DJvm.toAwtColor(value.onColor)
		    text = value.toString()
	    }
	    setIcon(icon)
    }
}

class LightColorEditor(optional: Boolean = false) : ComboBoxPropertyEditor() {
    init {
	    val list = mutableListOf<LightColor?>()
	    if (optional) {
		    list.add(null)
	    }
	    list.addAll(LightColor.values())
        setAvailableValues(list.toTypedArray())
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

class BranchCountEditor(filter: (BranchCount) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(BranchCount.values().filter { filter.invoke(it) }.toTypedArray())
		(editor as JComboBox<*>).renderer = EnumRenderer<BranchCount>()
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

class PullDirectionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PullDirection.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<PullDirection>()
	}
}

class TransistorTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(TransistorType.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<TransistorType>()
	}
}

class JoystickDeflectionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(JoystickDeflection.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<JoystickDeflection>()
	}
}

class DigitalPortViewStyleEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(DigitalPortViewStyle.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<DigitalPortViewStyle>()
	}
}

class PortViewSpacingEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PortViewSpacing.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<PortViewSpacing>()
	}
}

