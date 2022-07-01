package ch.scorpion.antares.view.net

import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.antares.view.app.OutputCountPropertySwing
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class WireTapViewBeanInfo : DigitalComponentBeanInfo<WireTapView>() {

	companion object {
		private val outputCount = OutputCountPropertySwing("tapCount", componentBeanProvider)
		private val handedness = AntaresProperties.handedness(baseKey = "library.element.WireTap.handedness")
		private val bitWidth = AntaresProperties.bitWidth("bitWidth", "library.element.WireTap.inputBitWidth")
		private val outputBitWidth = AntaresProperties.bitWidth("narrowSideBitWidth", "library.element.WireTap.narrowSideBitWidth")
		private val portViewSpacing = AntaresProperties.portViewSpacing()
		private val tapPosition = Array(8) { index ->
			CommandPropertySwing("tapPosition$index", "library.element.WireTap.tapPosition$index", Int::class.java, componentBeanProvider)
		}
	}

	override fun addProperties(bean: WireTapView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val connected = bean.model.isConnected

		properties.add(bitWidth.bind(editor, bean.id, editable = !connected))
		properties.add(outputBitWidth.bind(editor, bean.id, editable = !connected, filter = { it.width <= bean.bitWidth.width }))
		properties.add(handedness.bind(editor, bean.id, editable = !connected))
		properties.add(portViewSpacing.bind(editor, bean.id))
		properties.add(outputCount.bind(editor, bean.id, filter = { it.count >= 1 }))

		for (i in 0 until bean.tapCount.count) {
			properties.add(tapPosition[i].bind(editor, bean.id))
		}
	}
}