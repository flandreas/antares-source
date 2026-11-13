package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.draw.DrawTestRule
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.drawable.DrawableMockBuilder
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.module.DrawModule
import java.awt.image.BufferedImage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Tests [ViewImpl.draw] against a real AWT graphics context. */
class ViewImplDrawJvmTest {

	private val view = ViewImpl<InputEventContext>(
		affineTransformFactory = { System.createAffineTransform() },
		viewPainterFactory = { SimpleViewPainter(it) },
		applicationContextHolder = null,
	)

	@BeforeTest
	fun before() {
		DrawTestRule.configure()
		CanvasMockBuilder()
			.withDimension(Dimension2D(100, 100))
			.withView(view)
	}

	@Test
	fun shouldDrawRemainingDrawablesAndRestoreGraphicsWhenDrawableFails() {
		var drawn = false
		view.addDrawable(DrawableMockBuilder().withDrawLogic { error("broken drawable") }.build())
		view.addDrawable(DrawableMockBuilder().withDrawLogic { drawn = true }.build())
		val awtGraphics = BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics()
		val g = Graphics2DJvm(awtGraphics)

		view.draw(DrawModule.drawContextFactory(g, null, null))

		assertTrue(drawn)
		assertSame(awtGraphics, g.g)
	}
}
