package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.antares.view.DigitalComponentViewBeanInfo
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class SplitterViewBeanInfo : DigitalComponentViewBeanInfo<SplitterView>() {

    companion object {
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val branchCount = CommandPropertySwing("branchCount", "element.property.branchCount", BranchCount::class.java, componentBeanProvider)
	    private val handedness = AntaresProperties.handedness(baseKey = "element.property.Splitter.handedness")
	    private val portViewSpacing = AntaresProperties.portViewSpacing()
	    private val signalRep = AntaresProperties.signalRepresentation()
    }

	override val isShowPropagationDelay: Boolean get() = false

    override fun addProperties(bean: SplitterView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model.isConnected

	    properties.add(bitWidth.bind(editor, beanIdProvider(bean.id), editable = !connected))
	    properties.add(branchCount.bind(editor, beanIdProvider(bean.id), editable = !connected, filter = { bean.model.supportedBranchCounts.contains(it)} ))
	    properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
	    properties.add(portViewSpacing.bind(editor, beanIdProvider(bean.id)))
	    properties.add(signalRep.bind(editor, beanIdProvider(bean.id), filter = { it != DigitalSignalRepresentation.FIXED_POINT }))
    }
}