package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyImpl
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ConcentratorViewBeanInfo : DigitalComponentBeanInfo<ConcentratorView>() {

    companion object {
	    private val bitWidth = AntaresProperties.bitWidth()
	    private val branchCount = PropertyImpl("branchCount", "element.property.branchCount", BranchCount::class.java, componentBeanProvider)
	    private val handedness = AntaresProperties.handedness(baseKey = "element.property.Splitter.handedness")
	    private val signalRep = AntaresProperties.signalRepresentation()
    }

	override val isShowPropagationDelay: Boolean get() = false

	override fun addProperties(bean: ConcentratorView, editor: Editor, properties: MutableList<Property>) {
        super.addProperties(bean, editor, properties)

        val connected = bean.model.isConnected

	    properties.add(bitWidth.bind(editor, bean.id, editable = !connected))
	    properties.add(branchCount.bind(editor, bean.id, editable = !connected, filter = { bean.model.supportedBranchCounts.contains(it) }))
	    properties.add(handedness.bind(editor, bean.id, editable = !connected))
	    properties.add(signalRep.bind(editor, bean.id))
    }
}