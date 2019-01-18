package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.app.CopyPasteUtility
import ch.scorpion.jabbah.edit.app.DeleteAction
import ch.scorpion.jabbah.edit.editor.PasteCommand
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.io.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.awt.Toolkit
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CopyPasteUtilityFx : CopyPasteUtility {

	private val LOG by logger(CopyPasteUtilityFx::class)

	private val DEFAULT_DISTANCE_FACTOR = 3

	/** Remembers the first copies [Component] in order to repeat dislocations for consecutive pasts. */
	private var origAnchorComponent: Component? = null

	/** The pasted [Component] that corresponds with [origAnchorComponent]. */
	private var pastedAnchorComponent: Component? = null

	/** Tracks the number of consecutive pasts without an intermediate copy. Used to produce equal dislocations. */
	private var pasteCount: Int = 0

	/** ---- [CopyPasteUtility] */

	override fun cut(view: DrawingView<Drawing<Component>>, components: Collection<Component>, typeMap: TypeMap, commandManager: CommandManager) {
		val componentsToDelete = DeleteAction.getComponentsToDelete(components)
		if (componentsToDelete.isNotEmpty()) {
			copy(view.drawing, componentsToDelete, typeMap)
			EditModule.drawingService.delete(componentsToDelete, view, "edit.command.cut")
		}

		// Don't do 'components.size != componentsToDelete.size for checking whether everything has been deleted,
		// because non-deletable (by user selection!) Components might have been deleted as a side effect
		// of deleting other Components.
		if (components.any { view.drawing.contains(it) }) {
			BaseModule.eventBus.post(ComponentMessage(
				ComponentMessageType.Info,
				null,
				"edit.action.undeletable.msg"
			))
		}
	}

	override fun copy(drawing: Drawing<*>, components: Collection<Component>, typeMap: TypeMap) {
		ByteArrayOutputStream().use {
			try {
				val xmlWriter = ElectricXmlWriter(it)
				val writer = StoreXmlWriter(
					xmlWriter,
					typeMap,
					GlobalIdentityCreator()
				) { c -> components.contains(c) }
				writer.writeStorable(drawing)

				val content = ClipboardContent()
				content.putString(String(it.toByteArray()))
				Clipboard.getSystemClipboard().setContent(content)

				origAnchorComponent = components.iterator().next()
				pastedAnchorComponent = null
				pasteCount = 1
			} catch(e: Exception) {
				LOG.error("Error while copying Components to clipboard: ${e.message}")
				throw RuntimeException(e)
			}
		}
	}

	override fun paste(view: DrawingView<Drawing<Component>>, storableCreator: StorableCreator, typeMap: TypeMap, commandManager: CommandManager) {
		// Read the contents from the clipboard
		val content = Clipboard.getSystemClipboard().string
		if (StringUtils.isEmpty(content)) {
			Toolkit.getDefaultToolkit().beep()
		}

		ByteArrayInputStream(content.toByteArray()).use {
			try {
				val xmlReader = ElectricXmlReader(it)
				val reader = StoreXmlReader(xmlReader, typeMap, storableCreator)
				val copy = reader.readStorable()
				val dislocation: Point2D = if (pastedAnchorComponent != null) {
					pasteCount++
					pastedAnchorComponent!!.location.subtract(origAnchorComponent!!.location).multiply(pasteCount.toDouble())
				} else {
					Point2D(
						DEFAULT_DISTANCE_FACTOR * view.grid.distance,
						DEFAULT_DISTANCE_FACTOR * view.grid.distance)
				}

				if (copy is Drawing<*>) {
					val components = mutableListOf<Component>()
					for (cv in copy.backToFrontIterator()) {
						if (pastedAnchorComponent == null && origAnchorComponent!!.location == cv.location) {
							pastedAnchorComponent = cv
						}
						cv.moveBy(dislocation.x, dislocation.y)
						components.add(cv)
					}
					commandManager.execute(PasteCommand(view, components))
				}
			} catch(e: Exception) {
				LOG.error("Error while reading Components from clipboard: ${e.message}")
				throw RuntimeException(e)
			}
		}
	}
}