package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.net.NetSignalApplierStrategy
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.drawingBeanProvider
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImplBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class DigitalGraphViewBeanInfo : GraphViewImplBeanInfo<DigitalGraphView>() {

	companion object {
		private val defaultLightColor = DefaultLightColorProperty()
		private val defaultSignalRepresentation = DefaultDigitalSignalRepresentationProperty()
		private val netSignalApplierStrategy = CommandPropertySwing("netSignalApplierStrategy", NetSignalApplierStrategy.BASE_KEY, NetSignalApplierStrategy::class.java, drawingBeanProvider)
		private val defaultLogicGateSize = CommandPropertySwing("defaultLogicGateSize", "element.property.defaultLogicGateSize",
			Size::class.java, drawingBeanProvider)
	}

	override fun addProperties(bean: DigitalGraphView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(defaultLightColor.bind(editor, listOf(), optional = true))
		properties.add(defaultSignalRepresentation.bind(editor, listOf(), optional = true))
		properties.add(netSignalApplierStrategy.bind(editor, listOf()))
		properties.add(defaultLogicGateSize.bind(editor, listOf()))
	}
}