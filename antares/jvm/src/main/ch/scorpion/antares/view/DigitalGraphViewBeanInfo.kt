package ch.scorpion.antares.view

import ch.scorpion.antares.model.net.NetSignalApplierChoice
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.graph.GraphViewImplBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class DigitalGraphViewBeanInfo : GraphViewImplBeanInfo<DigitalGraphView>() {

	companion object {
		private val defaultLightColor = CommandPropertySwing("defaultLightColor", "element.property.DigitalGraphView.lightColor", LightColor::class.java, drawingBeanProvider)
		private val netSignalApplierChoice = CommandPropertySwing("netSignalApplierChoice", NetSignalApplierChoice.BASE_KEY, NetSignalApplierChoice::class.java, drawingBeanProvider)
	}

	override fun addProperties(bean: DigitalGraphView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(defaultLightColor.bind(editor, listOf(), optional = true))
		properties.add(netSignalApplierChoice.bind(editor, listOf()))
	}
}