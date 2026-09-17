package io.antarescircuit.jabbah.edit.model

import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveComponent
import io.antarescircuit.jabbah.edit.model.curve.QuadCurveComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.group.GroupComponent
import io.antarescircuit.jabbah.edit.model.group.GroupComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.polyline.PolylineComponent
import io.antarescircuit.jabbah.edit.model.polyline.PolylineComponentBeanInfo
import io.antarescircuit.jabbah.edit.model.rectangle.*
import io.antarescircuit.jabbah.edit.model.text.*
import io.antarescircuit.jabbah.edit.properties.AbstractBeanInfo
import dev.mokkery.answering.returns
import dev.mokkery.mock
import dev.mokkery.every
import kotlin.test.assertEquals
import org.junit.Test

class DrawBeanInfoTest {

	private val view = DrawingViewMockBuilder().build<Component, Drawing<Component>>()
	private val editor = mock<Editor>()

	init {
		BaseModuleJvm.require()
		EditTestRule.configure()

		every { editor.active } returns true
		every { editor.view } returns view
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
		read(PolylineComponent().also { it.addPoint(0, 0) }, PolylineComponentBeanInfo())
	}

	@Test
	fun shouldReadRectangleComponent() {
		read(RectangleComponent(), RectangleComponentBeanInfo())
	}

	@Test
	fun shouldPlaceLocationAfterId() {
		val names = RectangleComponentBeanInfo()
			.getProperties(RectangleComponent(), editor)
			.map { it.name }

		assertEquals(listOf("id", "location"), names.take(2))
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
