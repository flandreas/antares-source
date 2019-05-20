package ch.scorpion.jabbah.base.event

/**
 * An event which indicates that the user has pressed a key.
 */
interface KeyEvent : InputEvent {

    val key: Int

	val keyChar: Char

    companion object {

        /** The following key codes are target specific and must be set in the setup of the corresponding Target system.*/

        /** Key code of the non-numpad left arrow key */
        var VK_LEFT = 0x00

        /** Key code of the non-numpad right arrow key. */
        var VK_RIGHT = 0x00

        /** Key code of the escape key.*/
        var VK_ESCAPE = 0x00

	    /** Key code of the ENTER key.*/
        var VK_ENTER = 0x00

	    /** Key code for the 0 key.*/
	    var VK_0 = 0x00

	    /** Key code for the 1 key.*/
	    var VK_1 = 0x00
    }
}


interface KeyListener {
    fun keyTyped(e: KeyEvent)
    fun keyPressed(e: KeyEvent)
    fun keyReleased(e: KeyEvent)
}

/**
 * An empty implementation of the [KeyListener] interface intended to be subclassed by classes that
 * only need to implement some of the [KeyListener] event handling methods.
 */
open class KeyAdapter : KeyListener {
    override fun keyTyped(e: KeyEvent) {}
    override fun keyPressed(e: KeyEvent) {}
    override fun keyReleased(e: KeyEvent) {}
}

