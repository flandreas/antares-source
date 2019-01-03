package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.FloatPreference
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.polyline.PolylineShapeJvm
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.draw.view.ContextMenuProvider
import ch.scorpion.jabbah.draw.view.ZoomPanController
import java.awt.font.FontRenderContext
import javax.swing.JPopupMenu

/**
 * Setup of the [ch.scorpion.jabbah.draw] module for the JVM target.
 */
object DrawModuleJvm : AbstractModule() {

	const val PREF_TREE_RENDERING = "draw.preferences.group.rendering"
	const val PREF_TREE_VIEW = "draw.preferences.group.view"
	const val PREF_TREE_VIEW_NAVIGATION = "draw.preferences.group.view.navigation"

	var contextMenuProvider: ContextMenuProvider = object : ContextMenuProvider {
		override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
			menu.removeAll()
		}
	}

    override fun initialize() {
        BaseModuleJvm.require()

        DrawModule.polylineShapeFactory = ::PolylineShapeJvm

        DrawModule.textRenderInfoFactory = TextRenderInfoFactoryJvm()

        DrawModule.imageLoader = { ImageJvm(it) }

        DrawModule.require()

        fillProperties(DrawModule.properties)
	    buildPropertyTree(BaseModuleJvm.preferencesTree)
    }

    private fun fillProperties(properties: Properties) {
        properties.set(AbstractViewAction.PROP_ZOOM_STEP, 1.5f)
    }

	private fun buildPropertyTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_RENDERING))
		root.add(PreferenceGroup(PREF_TREE_VIEW))
		root.getGroup(PREF_TREE_VIEW).add(PreferenceGroup(PREF_TREE_VIEW_NAVIGATION))

		root.getGroup(PREF_TREE_RENDERING).add(BooleanPreference(
			id = DropShadow.PROP_SHADOW,
			nameKey = "draw.preferences.DropShadow.enable"))

		root.getGroup(PREF_TREE_RENDERING).add(IntPreference(
			id = DropShadow.PROP_OFFSET,
			nameKey = "draw.preferences.DropShadow.offset",
			minValue = 0,
			maxValue = 10))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = AbstractViewAction.PROP_ZOOM_STEP,
			nameKey = "draw.preferences.ViewAction.zoomStep",
			minValue = 1.01f))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = ZoomPanController.PROP_WHEEL_ZOOM_STEP,
			nameKey = "draw.preferences.ZoomPanController.wheelZoomStep",
			minValue = 1.01f))

		root.getGroup(PREF_TREE_VIEW).add(IntPreference(
			id = ZoomPanController.PROP_WHEEL_PAN_STEP,
			nameKey = "draw.preferences.ZoomPanController.wheelPanStep",
			minValue = 1))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = View.PROP_MIN_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.minZoomFactor"))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = View.PROP_MAX_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.maxZoomFactor"))
	}
}

private class TextRenderInfoFactoryJvm : TextRenderInfoFactory {

    override fun measureSingleLineText(text: String, font: Font): TextRenderInfo {
        val awtFont = java.awt.Font(font.family.javaName, Graphics2DJvm.fromFontStyle(font), font.size)
        val context = FontRenderContext(awtFont.transform, true, true)
        val rect = awtFont.getStringBounds(text, context)
        val lm = awtFont.getLineMetrics(text, context)
        return TextRenderInfo(Rectangle2D(rect.x, rect.y, rect.width, rect.height), lm.ascent.toDouble())
    }

    override fun measureHtmlText(text: String, font: Font, width: Int): TextRenderInfo {
        return Graphics2DJvm.measureHtmlText(text, Graphics2DJvm.toAwtFont(font), width)
    }
}