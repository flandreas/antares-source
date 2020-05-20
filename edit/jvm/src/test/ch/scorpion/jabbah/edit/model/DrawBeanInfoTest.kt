package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponent
import ch.scorpion.jabbah.edit.model.curve.QuadCurveComponentBeanInfo
import ch.scorpion.jabbah.edit.model.group.GroupComponent
import ch.scorpion.jabbah.edit.model.group.GroupComponentBeanInfo
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponent
import ch.scorpion.jabbah.edit.model.polyline.PolylineComponentBeanInfo
import ch.scorpion.jabbah.edit.model.rectangle.*
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class DrawBeanInfoTest {

	companion object {
		init {
			BaseModuleJvm.require()
			EditTestRule.configure()
		}
	}

	private val editor = mockk<Editor>()

	init {
		every { editor.active } returns true
	}

	private fun <T: Component> read(component: T, beanInfo: AbstractBeanInfo<T>) {
		beanInfo
			.getProperties(component, editor)
			.forEach { it.readFromObject(component) }
	}

	@Test
	fun shouldReadQuadCurveComponent() {
		read(QuadCurveComponent(), QuadCurveComponentBeanInfo())
	}

	@Test
	fun shouldReadGroupComponent() {
		read(GroupComponent(), GroupComponentBeanInfo())
	}

	@Test
	fun shouldReadPolylineComponent() {
		read(PolylineComponent(), PolylineComponentBeanInfo())
	}

	@Test
	fun shouldReadRectangleComponent() {
		read(RectangleComponent(), RectangleComponentBeanInfo())
	}

	@Test
	fun shouldReadEllipseComponent() {
		read(EllipseComponent(), EllipseComponentBeanInfo())
	}

	@Test
	fun shouldReadRoundRectangleComponent() {
		read(RoundRectangleComponent(), RoundRectangleComponentBeanInfo())
	}

	@Test
	fun shouldReadTextComponentJvm() {
		read(TextComponentJvm(), TextComponentJvmBeanInfo())
	}
}