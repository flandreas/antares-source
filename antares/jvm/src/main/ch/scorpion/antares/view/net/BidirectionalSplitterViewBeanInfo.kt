package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import com.l2fprod.common.propertysheet.Property

/**
 * TODO: Copy/Paste from [SplitterViewBeanInfo].
 */
@Suppress("unused")
class BidirectionalSplitterViewBeanInfo : DigitalComponentViewBeanInfo<BidirectionalSplitterView>() {

	companion object {
		private val bitWidth = AntaresProperties.bitWidth()
		private val branchCount = CommandPropertySwing("branchCount", "element.property.branchCount", BranchCount::class.java, componentBeanProvider)
		private val handedness = AntaresProperties.handedness(baseKey = "element.property.Splitter.handedness")
		private val portViewSpacing = AntaresProperties.portViewSpacing()
		private val signalRep = AntaresProperties.signalRepresentation()
	}

	override fun addProperties(bean: BidirectionalSplitterView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		val connected = bean.model.isConnected

		properties.add(bitWidth.bind(editor, beanIdProvider(bean.id), editable = !connected))
		properties.add(branchCount.bind(editor, beanIdProvider(bean.id), editable = !connected, filter = { bean.model.supportedBranchCounts.contains(it)} ))
		properties.add(handedness.bind(editor, beanIdProvider(bean.id)))
		properties.add(portViewSpacing.bind(editor, beanIdProvider(bean.id)))
		properties.add(signalRep.bind(editor, beanIdProvider(bean.id), filter = { it != DigitalSignalRepresentation.FIXED_POINT }))
	}
}