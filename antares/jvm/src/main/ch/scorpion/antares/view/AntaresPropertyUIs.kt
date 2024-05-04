package ch.scorpion.antares.view

import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.NetSignalApplierStrategy
import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.input.JoystickDeflection
import ch.scorpion.antares.view.net.TransistorViewSymbol
import ch.scorpion.antares.view.net.TunnelFlowDirection
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.VideoRamColorModel
import ch.scorpion.antares.view.port.DigitalPortViewStyle
import ch.scorpion.antares.view.EnterBehaviorEditor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.sound.WaveformType
import ch.scorpion.jabbah.base.swing.ColorIcon
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.base.swing.ToStringRenderer
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

class PortCountEditor(filter: (PortCount) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(PortCount.values().filter { filter.invoke(it) }.toTypedArray())
        (editor as JComboBox<*>).renderer = EnumRenderer<PortCount>()
    }
}

class InputPortNumberEditor(filter: (InputPortNumber) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(InputPortNumber.values().filter { filter.invoke(it) }.toTypedArray())
        (editor as JComboBox<*>).renderer = EnumRenderer<InputPortNumber>()
    }
}

class BranchCountEditor(filter: (BranchCount) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(BranchCount.PREDEFINED.filter { filter.invoke(it) }.toTypedArray())
		(editor as JComboBox<*>).renderer = ToStringRenderer<BranchCount>()
	}
}

class LogicEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Logic.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Logic>()
    }
}

class EnterBehaviorEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(EnterBehavior.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<EnterBehavior>()
    }
}

class TriggerEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Trigger.values())
        (editor as JComboBox<*>).renderer = EnumRenderer<Trigger>()
    }
}

class DigitalSignalRepresentationEditor(filter: (DigitalSignalRepresentation) -> Boolean = { true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(DigitalSignalRepresentation.values().filter { filter.invoke(it)}.toTypedArray())
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

class TransistorSymbolEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(TransistorViewSymbol.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<TransistorViewSymbol>()
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

class WaveformTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(WaveformType.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<WaveformType>()
	}
}

class VideoRamColorModelEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(VideoRamColorModel.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<VideoRamColorModel>()
	}
}

class TunnelFlowDirectionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(TunnelFlowDirection.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<TunnelFlowDirection>()
	}
}

class NetSignalApplierChoiceEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(NetSignalApplierStrategy.values())
		(editor as JComboBox<*>).renderer = EnumRenderer<NetSignalApplierStrategy>()
	}
}
