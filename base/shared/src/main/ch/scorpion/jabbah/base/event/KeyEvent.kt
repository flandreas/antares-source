package ch.scorpion.jabbah.base.event

enum class KeyEventType {
	TYPED, PRESSED, RELEASED, UNKNOWN
}
/**
 * An event which indicates that the user has pressed a key.
 */
interface KeyEvent : InputEvent {

	val type: KeyEventType

    val key: Int

	val keyChar: Char

    companion object {

        /**
         * The following key codes are target specific and must be set in the setup of the corresponding Target system.
         * The codes given here are completely arbitrary and only used for testing of 'common' code.
         */

        var VK_LEFT = 1
        var VK_RIGHT = 2
	    var VK_UP = 3
	    var VK_DOWN = 4
        var VK_ESCAPE = 5
        var VK_ENTER =6
	    var VK_ALT = 7
	    var VK_DELETE = 8
	    var VK_SPACE = 9
	    var VK_SHIFT = 10
	    var VK_META = 11
	    var VK_CTRL = 12
	    var VK_ALT_GRAPH = 13
	    var VK_0 = 48
	    var VK_1 = 49
	    var VK_2 = 50
	    var VK_3 = 51
	    var VK_4 = 52
	    var VK_5 = 53
	    var VK_6 = 54
	    var VK_7 = 55
	    var VK_8 = 56
	    var VK_9 = 57
	    var VK_A = 65
	    var VK_B = 66
	    var VK_C = 67
	    var VK_D = 68
	    var VK_E = 69
	    var VK_F = 70
	    var VK_X = 88
	    var VK_Z = 90
	    var VK_NUMPAD_0 = 96
	    var VK_NUMPAD_1 = 97
	    var VK_NUMPAD_2 = 98
	    var VK_NUMPAD_3 = 99
	    var VK_NUMPAD_4 = 100
	    var VK_NUMPAD_5 = 101
	    var VK_NUMPAD_6 = 102
	    var VK_NUMPAD_7 = 103
	    var VK_NUMPAD_8 = 104
	    var VK_NUMPAD_9 = 105
    }
}

/**
 * A platform-independent empty implementation of [KeyEvent] to be used when applying the "null pattern",
 * or for testing.
 */
data class KeyEventImpl(
	override val type: KeyEventType,
	override val event: Any = "",
	override val key: Int,
	override val keyChar: Char,
	override val source: Any = "",
	override val modifiers: Int = 0
) : KeyEvent {

	private var consumed: Boolean = false

	override fun consume() {
		consumed = true
	}

	override fun isConsumed(): Boolean = consumed
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

