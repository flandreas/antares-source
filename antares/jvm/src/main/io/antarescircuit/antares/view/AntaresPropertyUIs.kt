// Raw generic type of JComboBox in the library
@file:Suppress("UNCHECKED_CAST")

package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.*
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalType
import io.antarescircuit.antares.model.fsm.FSMStateType
import io.antarescircuit.antares.model.input.SwitchConfiguration
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.NetSignalApplierStrategy
import io.antarescircuit.antares.model.net.PullDirection
import io.antarescircuit.antares.model.net.TransistorType
import io.antarescircuit.antares.model.output.SevenSegmentDisplayScheme
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.view.input.JoystickDeflection
import io.antarescircuit.antares.view.net.TransistorViewSymbol
import io.antarescircuit.antares.view.net.tunnel.TunnelFlowDirection
import io.antarescircuit.antares.view.output.LEDShape
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.VideoRamColorModel
import io.antarescircuit.antares.view.port.DigitalPortViewStyle
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.sound.WaveformType
import io.antarescircuit.jabbah.base.swing.EnumRenderer
import io.antarescircuit.jabbah.base.swing.ToStringRenderer
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor
import javax.swing.JComboBox

class LightColorEditor(optional: Boolean = false) : ComboBoxPropertyEditor() {
    init {
	    val list = mutableListOf<LightColor?>()
	    if (optional) {
		    list.add(null)
	    }
	    list.addAll(LightColor.PREDEFINED.toTypedArray())
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

class DigitalSignalRepresentationRenderer : EnumRenderer<DigitalSignalRepresentation>(
	Translations.getString("element.property.DigitalSignalRepresentation.none")
)

class DigitalSignalRepresentationEditor(
	optional: Boolean = false,
	filter: (DigitalSignalRepresentation) -> Boolean = { true }
) : ComboBoxPropertyEditor() {
    init {
		val list = mutableListOf<DigitalSignalRepresentation?>()
		if (optional) {
			list.add(null)
		}
		list.addAll(DigitalSignalRepresentation.entries.filter { filter.invoke(it)})
        setAvailableValues(list.toTypedArray())
        (editor as JComboBox<DigitalSignalRepresentation>).renderer = DigitalSignalRepresentationRenderer()
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

class LEDShapeEditor : ComboBoxPropertyEditor() {
	init {
		setAvailableValues(LEDShape.entries.toTypedArray())
		(editor as JComboBox<LEDShape>).renderer = EnumRenderer()
	}
}
