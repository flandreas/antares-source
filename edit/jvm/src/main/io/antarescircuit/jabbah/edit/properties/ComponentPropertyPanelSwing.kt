package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.Component
import javax.swing.JPanel

/**
 * A [JPanel] for editing the properties of the currently selected [Component].
 */
class ComponentPropertyPanelSwing(
	controller: ComponentPropertyPanelController,
	scope: String,
	sheetFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller, scope, sheetFactory), ComponentPropertyPanel {

	companion object {
		private val LOG by logger(ComponentPropertyPanelSwing::class)
	}

	init {
		controller.view = this
	}

    /** ---- [AbstractPropertyPanelSwing] */

	override fun handleBeanReplaced() {
		clearProperties()

		controller.bean?.let {
			if (it is Component) {
				loadComponentProperties(it)
			} else {
				loadProperties(it)
			}
		}
	}

	override fun createBeanInfo(bean: Any): AbstractBeanInfo<Any>? {
		return if (bean is MultiSelection) {
			try {
				val delegateBeanInfo = createBeanInfo(getBeanInfoClass(bean.commonType))
				val beanInfo = MultiSelectionBeanInfo(delegateBeanInfo) as AbstractBeanInfo<Any>
				delegateBeanInfo.beanIdProvider = { bean.selection.map { it.id.toString() } }
				beanInfo
			} catch (e: Throwable) {
				LOG.trace("No BeanInfo found for MultiSelection commonType ${bean.commonType.qualifiedName}")
				null
			}
		} else {
			super.createBeanInfo(bean)
		}
	}

	override fun getReadSourceBean(bean: Any): Any =
		if (bean is MultiSelection) {
			bean.selection.first().propertyOwner
		} else {
			bean
		}

	override fun getBeanInfoBean(bean: Any): Any =
		if (bean is Component) bean.propertyOwner else bean

	/** ---- [ComponentPropertyPanelSwing] */

	private fun loadComponentProperties(component: Component) {
		super.loadProperties(component.propertyOwner)
	}
}