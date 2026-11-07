package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.base.sound.WaveformType
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class BuzzerViewBeanInfo : DigitalComponentViewBeanInfo<BuzzerView>() {

	companion object {
		val waveformType = CommandPropertySwing("waveformType", BuzzerView.BASE_KEY_WAVEFORM, WaveformType::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: BuzzerView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(waveformType.bind(editor, beanIdProvider(bean.id)))
	}
}