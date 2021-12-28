package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.EditProperties
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewBeanInfo
import com.l2fprod.common.propertysheet.Property

@Suppress("unused")
class VideoRamViewBeanInfo : VerticeViewBeanInfo<VideoRamView>() {

	companion object {
		private val width = CommandPropertySwing("columnsCount", "element.property.VideoRam.width", Int::class.java, beanProvider = componentBeanProvider)
		private val height = CommandPropertySwing("rowsCount", "element.property.VideoRam.height", Int::class.java, beanProvider = componentBeanProvider)
		private val pixelSize = EditProperties.size(baseKey = "element.property.VideoRam.pixelSize")
		private val colorModel = CommandPropertySwing("colorModel", VideoRamColorModel.BASE_KEY, VideoRamColorModel::class.java, componentBeanProvider)
	}

	override fun addProperties(bean: VideoRamView, editor: Editor, properties: MutableList<Property>) {
		super.addProperties(bean, editor, properties)
		properties.add(width.bind(editor, bean.id))
		properties.add(height.bind(editor, bean.id))
		properties.add(pixelSize.bind(editor, bean.id))
		properties.add(colorModel.bind(editor, bean.id))
	}
}