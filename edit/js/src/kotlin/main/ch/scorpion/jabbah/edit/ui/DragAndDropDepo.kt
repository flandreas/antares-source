package ch.scorpion.jabbah.edit.ui

/**
 * Because JS supports only textual data to be transferred by drag and drop, this is the central
 * place to put complex (object) data to be transferred.
 */
object DragAndDropDepo {

	const val ID = "JabbahDragAndDropId"

	var data: Any? = null
		private set

	fun set(data: Any) {
		DragAndDropDepo.data = data
	}

	fun clear() {
		data = null
	}
}