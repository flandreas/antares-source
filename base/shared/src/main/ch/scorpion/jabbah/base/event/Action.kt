package ch.scorpion.jabbah.base.event

class ActionEvent(
        override val event: Any?,
        override val source: Any,
        override val modifiers: Int,
        val action: String,
        val time: Long
) : InputEvent {

    override fun consume() {
        // empty
    }
}

interface ActionListener {
    fun actionPerformed(event: ActionEvent)
}
