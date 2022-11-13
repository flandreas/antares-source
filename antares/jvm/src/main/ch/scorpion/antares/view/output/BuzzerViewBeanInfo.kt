package ch.scorpion.antares.view.output

import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.base.sound.WaveformType
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class BuzzerViewBeanInfo : DigitalComponentBeanInfo<BuzzerView>() {

	companion object {
		val waveformType = CommandPropertySwing("waveformType", BuzzerView.BASE_KEY_WAVEFORM, WaveformType::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: BuzzerView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(waveformType.bind(editor, beanIdProvider(bean.id)))
	}
}