package io.antarescircuit.antares.view.net.tunnel

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.text.description.BASE_KEY_NAME
import io.antarescircuit.jabbah.graph.view.GraphView
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class TunnelViewBeanInfo : DigitalComponentViewBeanInfo<TunnelView>() {

    companion object {
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val flowDirection = AntaresProperties.tunnelFlowDirection()
		private val isGlobal = AntaresProperties.tunnelIsGlobal()
    }

	// Use special property for tunnel name
	override val isShowName: Boolean get() = false

    override fun addProperties(bean: TunnelView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

	    val tunnelNameProperty = TunnelNameProperty((editor.drawing as GraphView).graph!! as DigitalGraph, "tunnelName", BASE_KEY_NAME, componentBeanProvider)

	    properties.add(tunnelNameProperty.bind(editor, beanIdProvider(bean.id)))
	    properties.add(bitWidth.bind(editor, beanIdProvider(bean.id)))
	    if (TunnelView.face == TunnelViewFace.ARROW) {
		    properties.add(flowDirection.bind(editor, beanIdProvider(bean.id)))
	    }
		properties.add(isGlobal.bind(editor, beanIdProvider(bean.id)))
    }
}