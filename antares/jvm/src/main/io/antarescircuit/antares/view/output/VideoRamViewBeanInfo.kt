package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.view.AntaresProperties
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.EditProperties
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class VideoRamViewBeanInfo : VerticeViewBeanInfo<VideoRamView>() {

	companion object {
		private val dataBitWidth = AntaresProperties.bitWidth("dataWidth", "element.property.dataBitWidth")
		private val width = CommandPropertySwing("columnsCount", "element.property.VideoRam.width", Int::class.java, beanProvider = componentBeanProvider)
		private val height = CommandPropertySwing("rowsCount", "element.property.VideoRam.height", Int::class.java, beanProvider = componentBeanProvider)
		private val pixelSize = EditProperties.size(baseKey = "element.property.VideoRam.pixelSize")
		private val colorModel = CommandPropertySwing("colorModel", VideoRamColorModel.BASE_KEY, VideoRamColorModel::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: VideoRamView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(dataBitWidth.bind(editor, beanIdProvider(bean.id)))
		properties.add(width.bind(editor, beanIdProvider(bean.id)))
		properties.add(height.bind(editor, beanIdProvider(bean.id)))
		properties.add(pixelSize.bind(editor, beanIdProvider(bean.id)))
		properties.add(colorModel.bind(editor, beanIdProvider(bean.id)))
	}
}