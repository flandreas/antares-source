package ch.scorpion.antares.view

import ch.scorpion.antares.model.*
import ch.scorpion.antares.model.analog.AnalogOscilloscopeSignalType
import ch.scorpion.antares.model.fsm.FSMStateType
import ch.scorpion.antares.model.input.SwitchConfiguration
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.NetSignalApplierStrategy
import ch.scorpion.antares.model.net.PullDirection
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.output.SevenSegmentDisplayScheme
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.input.JoystickDeflection
import ch.scorpion.antares.view.net.TransistorViewSymbol
import ch.scorpion.antares.view.net.tunnel.TunnelFlowDirection
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.VideoRamColorModel
import ch.scorpion.antares.view.port.DigitalPortViewStyle
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
		    icon.backgroundColor = Graphics2DJvm.toAwtColor(LightColor.getSystemDefault().executeColor(true))
		    text = Translations.getString("element.color.none")
	    } else {
		    icon.backgroundColor = Graphics2DJvm.toAwtColor(value.executeColor(true))
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
	    list.addAll(LightColor.entries.toTypedArray())
        setAvailableValues(list.toTypedArray())
        (editor as JComboBox<LightColor>).renderer = LightColorRenderer()
    }
}

class HandednessEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Handedness.entries.toTypedArray())
        (editor as JComboBox<Handedness>).renderer = EnumRenderer()
    }
}

class PortCountEditor(filter: (PortCount) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(PortCount.entries.filter { filter.invoke(it) }.toTypedArray())
        (editor as JComboBox<PortCount>).renderer = EnumRenderer()
    }
}

class InputPortNumberEditor(filter: (InputPortNumber) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(InputPortNumber.entries.filter { filter.invoke(it) }.toTypedArray())
        (editor as JComboBox<InputPortNumber>).renderer = EnumRenderer()
    }
}

class BranchCountEditor(filter: (BranchCount) -> Boolean = { _ -> true }) : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(BranchCount.PREDEFINED.filter { filter.invoke(it) }.toTypedArray())
		(editor as JComboBox<BranchCount>).renderer = ToStringRenderer<BranchCount>()
	}
}

class LogicEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Logic.entries.toTypedArray())
        (editor as JComboBox<Logic>).renderer = EnumRenderer()
    }
}

class EnterBehaviorEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(EnterBehavior.entries.toTypedArray())
        (editor as JComboBox<EnterBehavior>).renderer = EnumRenderer()
    }
}

class TriggerEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(Trigger.entries.toTypedArray())
        (editor as JComboBox<Trigger>).renderer = EnumRenderer()
    }
}

class DigitalSignalRepresentationEditor(filter: (DigitalSignalRepresentation) -> Boolean = { true }) : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(DigitalSignalRepresentation.entries.filter { filter.invoke(it)}.toTypedArray())
        (editor as JComboBox<DigitalSignalRepresentation>).renderer = EnumRenderer()
    }
}

class SevenSegmentDisplaySchemeEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(SevenSegmentDisplayScheme.entries.toTypedArray())
        (editor as JComboBox<SevenSegmentDisplayScheme>).renderer = EnumRenderer()
    }
}

class OutputAnnotationEditor : ComboBoxPropertyEditor() {
    init {
        setAvailableValues(OutputAnnotation.entries.toTypedArray())
        (editor as JComboBox<OutputAnnotation>).renderer = EnumRenderer()
    }
}

class PullDirectionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PullDirection.entries.toTypedArray())
		(editor as JComboBox<PullDirection>).renderer = EnumRenderer()
	}
}

class TransistorTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(TransistorType.entries.toTypedArray())
		(editor as JComboBox<TransistorType>).renderer = EnumRenderer()
	}
}

class TransistorSymbolEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(TransistorViewSymbol.entries.toTypedArray())
		(editor as JComboBox<TransistorViewSymbol>).renderer = EnumRenderer()
	}
}

class JoystickDeflectionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(JoystickDeflection.entries.toTypedArray())
		(editor as JComboBox<JoystickDeflection>).renderer = EnumRenderer()
	}
}

class DigitalPortViewStyleEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(DigitalPortViewStyle.entries.toTypedArray())
		(editor as JComboBox<DigitalPortViewStyle>).renderer = EnumRenderer()
	}
}

class PortViewSpacingEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(PortViewSpacing.entries.toTypedArray())
		(editor as JComboBox<PortViewSpacing>).renderer = EnumRenderer()
	}
}

class WaveformTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(WaveformType.entries.toTypedArray())
		(editor as JComboBox<WaveformType>).renderer = EnumRenderer()
	}
}

class VideoRamColorModelEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(VideoRamColorModel.entries.toTypedArray())
		(editor as JComboBox<VideoRamColorModel>).renderer = EnumRenderer()
	}
}

class TunnelFlowDirectionEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(TunnelFlowDirection.entries.toTypedArray())
		(editor as JComboBox<TunnelFlowDirection>).renderer = EnumRenderer()
	}
}

class NetSignalApplierChoiceEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(NetSignalApplierStrategy.entries.toTypedArray())
		(editor as JComboBox<NetSignalApplierStrategy>).renderer = EnumRenderer()
	}
}

class SwitchConfigurationEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(SwitchConfiguration.entries.toTypedArray())
		(editor as JComboBox<SwitchConfiguration>).renderer = EnumRenderer()
	}
}

class AnalogOscilloscopeSignalTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(AnalogOscilloscopeSignalType.entries.toTypedArray())
		(editor as JComboBox<AnalogOscilloscopeSignalType>).renderer = EnumRenderer()
	}
}

class FSMStateTypeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(FSMStateType.entries.toTypedArray())
		(editor as JComboBox<FSMStateType>).renderer = EnumRenderer()
	}
}