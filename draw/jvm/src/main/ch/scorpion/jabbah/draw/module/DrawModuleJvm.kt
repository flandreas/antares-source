package ch.scorpion.jabbah.draw.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.base.preferences.BooleanPreference
import ch.scorpion.jabbah.base.preferences.FloatPreference
import ch.scorpion.jabbah.base.preferences.IntPreference
import ch.scorpion.jabbah.base.preferences.PreferenceGroup
import ch.scorpion.jabbah.draw.ThemePreference
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.ImageJvm
import ch.scorpion.jabbah.draw.view.AbstractZoomPanAction
import ch.scorpion.jabbah.draw.view.ContextMenuProvider
import ch.scorpion.jabbah.draw.view.TooltipManager
import ch.scorpion.jabbah.draw.view.ZoomPanController
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

        DrawModule.imageLoader = { ImageJvm(it) }

        DrawModule.require()

        fillProperties(DrawModule.properties)
	    buildPreferencesTree(BaseModuleJvm.preferencesTree)
    }

    private fun fillProperties(properties: Properties) {
        properties.set(AbstractZoomPanAction.PROP_ZOOM_STEP, 1.5f)
    }

	private fun buildPreferencesTree(root: PreferenceGroup) {
		root.add(PreferenceGroup(PREF_TREE_RENDERING))
		root.add(PreferenceGroup(PREF_TREE_VIEW))
		root.getGroup(PREF_TREE_VIEW).add(PreferenceGroup(PREF_TREE_VIEW_NAVIGATION))

		root.getGroup(PREF_TREE_RENDERING).add(ThemePreference())

		root.getGroup(PREF_TREE_RENDERING).add(BooleanPreference(
			id = DropShadow.PROP_SHADOW,
			nameKey = "draw.preferences.DropShadow.enable"))

		root.getGroup(PREF_TREE_RENDERING).add(IntPreference(
			id = DropShadow.PROP_OFFSET,
			nameKey = "draw.preferences.DropShadow.offset",
			minValue = 0,
			maxValue = 10))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = AbstractZoomPanAction.PROP_ZOOM_STEP,
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
			id = View.PROP_DEFAULT_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.defaultZoomFactor"))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = View.PROP_MIN_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.minZoomFactor"))

		root.getGroup(PREF_TREE_VIEW).add(FloatPreference(
			id = View.PROP_MAX_ZOOM_FACTOR,
			nameKey = "draw.preferences.View.maxZoomFactor"))

		root.getGroup(PREF_TREE_VIEW).add(IntPreference(
			id = TooltipManager.PROP_DELAY,
			nameKey = "draw.preference.TooltipManager.delay"
		))
	}
}
