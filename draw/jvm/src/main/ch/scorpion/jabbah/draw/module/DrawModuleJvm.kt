package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.module.BaseModuleJvm.PREF_TREE_VIEW
import ch.scorpion.jabbah.base.preferences.*
import ch.scorpion.jabbah.draw.ThemePreference
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.ImageLoaderJvm
import ch.scorpion.jabbah.draw.graphics.RasterImageJvm
import ch.scorpion.jabbah.draw.view.*
import org.apache.commons.lang3.SystemUtils
import javax.swing.JPopupMenu

/**
 * Setup of the [ch.scorpion.jabbah.draw] module for the JVM target.
 */
object DrawModuleJvm : AbstractModule() {

	const val PREF_TREE_VIEW_ZOOM_PAN = "draw.preferences.group.view.zoomPan"
	const val PREF_TREE_VIEW_NAVIGATION = "draw.preferences.group.view.navigation"

	private const val MIN_ZOOM_FACTOR = 0.01f
	private const val MAX_ZOOM_FACTOR = 100f

	var contextMenuProvider: ContextMenuProvider = object : ContextMenuProvider {
		override var applicationName: String = ""

		override fun fillContextMenu(view: View<*>, x: Double, y: Double, menu: JPopupMenu) {
			menu.removeAll()
		}
	}

    override fun initialize() {
        BaseModuleJvm.require()

	    DrawModule.rasterImageFactory = { w, h -> RasterImageJvm(w, h) }

		DrawModule.imageLoader = ImageLoaderJvm()

        DrawModule.require()

	    buildPreferencesTree(BaseModuleJvm.preferencesTree)

	    if (SystemUtils.IS_OS_WINDOWS) {
			// Under Windows, the standard ALT key transfers focus to the main menu
			DrawModule.mouseWheelPanModifier = KeyEvent.VK_CTRL
	    }
    }

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.getGroup(PREF_TREE_VIEW).add(PreferenceGroup(PREF_TREE_VIEW_ZOOM_PAN))
		root.getGroup(PREF_TREE_VIEW).add(PreferenceGroup(PREF_TREE_VIEW_NAVIGATION))

		// View

		root.getGroup(PREF_TREE_VIEW).add(BooleanPreference(
			id = TooltipHandler.PROP_TOOLTIPS_ENABLED,
			nameKey = "draw.preference.componentTooltip.enable"))

		root.getGroup(PREF_TREE_VIEW).add(IntPreference(
			id = TooltipManager.PROP_DELAY,
			nameKey = "draw.preference.TooltipManager.delay",
			minValue = 100,
			maxValue = 3000))

		// Rendering

		root.getGroup(BaseModuleJvm.PREF_TREE_RENDERING).add(ThemePreference())

		root.getGroup(BaseModuleJvm.PREF_TREE_RENDERING).add(BooleanPreference(
			id = DropShadow.PROP_SHADOW,
			nameKey = "draw.preferences.DropShadow.enable",
			needsRestart = true))

		root.getGroup(BaseModuleJvm.PREF_TREE_RENDERING).add(IntPreference(
			id = DropShadow.PROP_OFFSET,
			nameKey = "draw.preferences.DropShadow.offset",
			minValue = 1,
			maxValue = 10))

		// Zoom / Pan

		root.getGroup(PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(EnumPreference(
			id = PanMethod.PROP_PAN_METHOD,
			nameKey = "draw.preferences.panMethod",
			values = PanMethod.entries.toTypedArray(),
			withName = PanMethod::withName))

		root.getGroup(PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(IntPreference(
			id = ZoomPanController.PROP_WHEEL_PAN_STEP,
			nameKey = "draw.preferences.ZoomPanController.wheelPanStep",
			minValue = 1))

		root.getGroup(PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(FloatPreference(
			id = AbstractZoomPanAction.PROP_ZOOM_STEP,
			nameKey = "draw.preferences.ViewAction.zoomStep",
			minValue = 0.01f))

		root.getGroup(PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(FloatPreference(
			id = ZoomPanController.PROP_WHEEL_ZOOM_STEP,
			nameKey = "draw.preferences.ZoomPanController.wheelZoomStep",
			minValue = 0.01f))

		root.getGroup(PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(FloatPreference(
			id = View.PROP_DEFAULT_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.defaultZoomFactor",
			minValue = MIN_ZOOM_FACTOR,
			maxValue = MAX_ZOOM_FACTOR))

		root.getGroup(PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(FloatPreference(
			id = View.PROP_MIN_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.minZoomFactor",
			minValue = MIN_ZOOM_FACTOR,
			maxValue = MAX_ZOOM_FACTOR))

		root.getGroup(PREF_TREE_VIEW).getGroup(PREF_TREE_VIEW_ZOOM_PAN).add(FloatPreference(
			id = View.PROP_MAX_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.maxZoomFactor",
			minValue = MIN_ZOOM_FACTOR,
			maxValue = MAX_ZOOM_FACTOR))
	}
}
