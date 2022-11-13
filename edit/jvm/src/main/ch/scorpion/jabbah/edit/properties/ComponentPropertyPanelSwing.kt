package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.Component
import javax.swing.JPanel

/**
 * A [JPanel] for editing the properties of the currently selected [Component].
 */
class ComponentPropertyPanelSwing(
	controller: ComponentPropertyPanelController,
	scope: String,
	sheetFactory: PropertySheetPanelFactory
) : AbstractPropertyPanelSwing(controller, scope, sheetFactory), ComponentPropertyPanel {

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

	override fun createBeanInfo(bean: Any, classPath: String): AbstractBeanInfo<Any> =
		if (bean is MultiSelection) {
			var delegateBean: Any = bean.selection.first()
			if (delegateBean is Component) {
				delegateBean = delegateBean.propertyOwner
			}
			val delegateBeanInfo = super.createBeanInfo(delegateBean, createBeanClassPath(delegateBean))
			val beanInfo = MultiSelectionBeanInfo(delegateBeanInfo) as AbstractBeanInfo<Any>
			delegateBeanInfo.beanIdProvider = { bean.selection.map { it.id.toString() }}
			beanInfo
		} else {
			super.createBeanInfo(bean, classPath)
		}

	override fun getReadSourceBean(bean: Any): Any =
		if (bean is MultiSelection) {
			bean.selection.first().propertyOwner
		} else {
			bean
		}

	override fun createBeanClassPath(bean: Any): String {
		return if (bean is Component) {
			if (bean.beanInfoClassName != null) {
				bean.beanInfoClassName!!
			} else {
				super.createBeanClassPath(bean)
			}
		} else {
			super.createBeanClassPath(bean)
		}
	}

	/** ---- [ComponentPropertyPanelSwing] */

	private fun loadComponentProperties(component: Component) {
		super.loadProperties(component.propertyOwner)
	}
}