package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Point2D

/** Test utility for sending [ch.scorpion.jabbah.base.event.InputEvent]s to a [Tool] under test.*/
class ToolTestUtil(val tool: Tool, private val editor: Editor) {

	fun moveMouseTo(x: Int, y: Int, modifiers: Int = 0) {
		tool.mouseMoved(EventTestUtil.moveMouseTo(locationView(x, y), modifiers), x.toDouble(), y.toDouble())
	}

	fun pressMouseAt(x: Int, y: Int, modifiers: Int = 0) {
		tool.mousePressed(EventTestUtil.pressMouseAt(locationView(x, y), modifiers), x.toDouble(), y.toDouble())
	}

	fun dragMouseTo(x: Int, y: Int, modifiers: Int = 0) {
		tool.mouseDragged(EventTestUtil.dragMouseTo(locationView(x, y), modifiers), x.toDouble(), y.toDouble())
	}

	fun releaseMouseAt(x: Int, y: Int, modifiers: Int = 0) {
		tool.mouseReleased(EventTestUtil.releaseMouseAt(locationView(x, y), modifiers), x.toDouble(), y.toDouble())
	}

	fun clickMouseAt(x: Int, y: Int, modifiers: Int = 0) {
		tool.mouseClicked(EventTestUtil.clickMouseAt(locationView(x, y), modifiers), x.toDouble(), y.toDouble())
	}

	fun pressKey(key: Int, modifiers: Int = 0) {
		tool.keyPressed(EventTestUtil.pressKey(key, modifiers))
	}

	private fun locationView(x: Int, y: Int): Point2D = editor.view.modelToView(Point2D(x, y))
}