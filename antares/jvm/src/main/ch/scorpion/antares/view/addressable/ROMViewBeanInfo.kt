package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.MemoryStorableIdentification
import ch.scorpion.antares.model.addressable.ROM
import ch.scorpion.antares.view.AntaresProperties
import ch.scorpion.antares.view.DigitalComponentViewBeanInfo
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.GraphProperties
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class ROMViewBeanInfo : DigitalComponentViewBeanInfo<ROMView>() {

	companion object {
		private val addressBitWidth = AntaresProperties.bitWidth("addressWidth", "element.property.addressBitWidth")
		private val dataBitWidth = AntaresProperties.bitWidth("dataWidth", "element.property.dataBitWidth")

		private val text = GraphProperties.label(name = "text")
		private val showContents = CommandPropertySwing("showContents", "element.property.Addressable.showContents", Boolean::class.java, componentBeanProvider)
		private val contentsRowCount = CommandPropertySwing("contentRowsCount", "element.property.Addressable.rowsCount", Int::class.java, componentBeanProvider)
		private val contentsColumnsCount = CommandPropertySwing("contentColumnsCount", "element.property.Addressable.columnsCount", Int::class.java, componentBeanProvider)

		// No Parser for disassemblerConfig because content is a RegEx and not an Antares DSL script
		private val disassemblerConfig = EditProperties.script("disassemblerConfig", "element.property.ROM.disassemblerConfig",
			beanProvider = componentBeanProvider, parserFactory = null, helpId = ROM.DISASSEMBLER_HELP_ID)

		private val showDisassembler = CommandPropertySwing("showDisassembler", "element.property.ROM.disassemblerDisplay", Boolean::class.java, componentBeanProvider)
		private val highlightCurrentCellWhenNotSelected = CommandPropertySwing("highlightCurrentCellWhenNotSelected", "element.property.ROM.highlightCurrentCellWhenNotSelected", Boolean::class.java, componentBeanProvider)

		private val loadDataSource = CommandPropertySwing("loadDataSource", "element.property.ROM.loadDataSource", Boolean::class.java, componentBeanProvider)

		private val memoryStorable = CommandPropertySwing("memoryStorableId", "element.property.memoryStorable", MemoryStorableIdentification::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: ROMView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)

		properties.add(addressBitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(dataBitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(text.bind(editor, beanIdProvider(bean.id), filter = { false }))
		properties.add(loadDataSource.bind(editor, beanIdProvider(bean.id)))
		properties.add(memoryStorable.bind(editor, beanIdProvider(bean.id)))
		properties.add(showContents.bind(editor, beanIdProvider(bean.id)))
		if (bean.showContents) {
			properties.add(contentsRowCount.bind(editor, beanIdProvider(bean.id)))
			properties.add(contentsColumnsCount.bind(editor, beanIdProvider(bean.id)))
			properties.add(disassemblerConfig.bind(editor, beanIdProvider(bean.id)))
			properties.add(showDisassembler.bind(editor, beanIdProvider(bean.id)))
			properties.add(highlightCurrentCellWhenNotSelected.bind(editor, beanIdProvider(bean.id)))
		}
	}
}