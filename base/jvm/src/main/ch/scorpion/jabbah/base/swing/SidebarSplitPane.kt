package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Settings
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSplitPane

/**
 * Combines a [SidebarPane] with a [JSplitPane] to allow the user to distribute space between the open
 * [SidebarPane] mainContent and the permanent main mainContent.
 */
class SidebarSplitPane(
	private val location: SidebarPane.Location,
	private val mainContent: JComponent,
	settingBaseName: String,
	providedInitialOpenIndex: Int = -1,
	contents: List<SidebarPaneContent>,
	private val isOpenChangeHandler: (() -> Unit)? = null
) : JPanel() {

	companion object {

		/** The name in [Settings] (extending `propertyBaseName`) of the [JSplitPane] divider position.*/
		private const val SPLIT_POS = "splitPos"

		/** The name in [Settings] (extending `propertyBaseName`) of the index of the open sidebar mainContent.*/
		private const val OPEN_INDEX = "openIndex"

		private const val DEF_SIDEBAR_SIZE = 200

		private fun getSplitPaneOrientation(location: SidebarPane.Location): Int {
			return when (location) {
				SidebarPane.Location.Bottom -> JSplitPane.VERTICAL_SPLIT
				SidebarPane.Location.Right -> JSplitPane.HORIZONTAL_SPLIT
				SidebarPane.Location.Left -> JSplitPane.HORIZONTAL_SPLIT
			}
		}

		private fun getSidebarDirection(location: SidebarPane.Location): String {
			return when(location) {
				SidebarPane.Location.Bottom -> BorderLayout.SOUTH
				SidebarPane.Location.Right -> BorderLayout.EAST
				SidebarPane.Location.Left -> BorderLayout.WEST
			}
		}
	}

	private val sidebarPane = SidebarPane(location) { sidebarPaneChanged() }

	private val sidebarSplitPane = JSplitPane(getSplitPaneOrientation(location))

	private val dividerLocationSettingName = "$settingBaseName.$SPLIT_POS"

	private val openIndexSettingName = "$settingBaseName.$OPEN_INDEX"

	/** Holds the location of [sidebarSplitPane]'s divider for re-establishing it the next time it opens.*/
	private var sidebarDividerLocation: Int = BaseModule.settings.getInt(dividerLocationSettingName, -1)

	private var initialOpenIndex: Int = if (providedInitialOpenIndex >= 0) providedInitialOpenIndex else BaseModule.settings.getInt(openIndexSettingName, -1)

	init {
		buildUI()
		contents.forEach { sidebarPane.add(it) }
		sidebarPane.open(initialOpenIndex)
	}

	fun dispose() {
		BaseModule.settings.set(dividerLocationSettingName, sidebarSplitPane.dividerLocation)
		BaseModule.settings.set(openIndexSettingName, sidebarPane.openIndex)
	}

	private fun buildUI() {
		layout = BorderLayout()

		sidebarSplitPane.border = null

		add(sidebarPane, getSidebarDirection(location))
		add(mainContent, BorderLayout.CENTER)
	}

	/** Fills [sidebarPane] and [mainContent] into this [JPanel] in the order specified by the [location] property.*/
	private fun fillSplitPane() {
		when (location) {
			SidebarPane.Location.Right, SidebarPane.Location.Bottom -> {
				sidebarSplitPane.add(mainContent)
				sidebarSplitPane.add(sidebarPane)
				sidebarSplitPane.resizeWeight = 1.0
			}
			SidebarPane.Location.Left -> {
				sidebarSplitPane.add(sidebarPane)
				sidebarSplitPane.add(mainContent)
				sidebarSplitPane.resizeWeight = 0.0
			}
		}
	}

	private fun getDividerLocation(): Int {
		return if (sidebarDividerLocation > 0) {
			sidebarDividerLocation
		} else {
			when (location) {
				SidebarPane.Location.Right, SidebarPane.Location.Bottom -> {
					mainContent.width - DEF_SIDEBAR_SIZE
				}
				SidebarPane.Location.Left -> {
					DEF_SIDEBAR_SIZE
				}
			}
		}
	}

	/** Called when [sidebarPane] has been opened or closed, or when another entry is displayed.*/
	private fun sidebarPaneChanged() {
		if (sidebarPane.isOpen) {
			removeAll()
			sidebarSplitPane.remove(sidebarPane)
			sidebarSplitPane.remove(mainContent)
			fillSplitPane()
			sidebarSplitPane.dividerLocation = getDividerLocation()
			sidebarDividerLocation = sidebarSplitPane.dividerLocation
			add(sidebarSplitPane, BorderLayout.CENTER)
		} else {
			sidebarDividerLocation = sidebarSplitPane.dividerLocation
			removeAll()
			sidebarSplitPane.remove(sidebarPane)
			sidebarSplitPane.remove(mainContent)
			add(mainContent, BorderLayout.CENTER)
			add(sidebarPane, getSidebarDirection(location))
		}
		isOpenChangeHandler?.invoke()

		revalidate()
		repaint()
	}
}