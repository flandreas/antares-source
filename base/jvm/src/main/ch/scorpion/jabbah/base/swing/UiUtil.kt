package ch.scorpion.jabbah.base.swing

import java.lang.reflect.InvocationTargetException
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource


/**
 * Contains various static utility methods.
 */
object UiUtil {

    /**
     * A wrapper around [SwingUtilities.invokeAndWait] which wraps exceptions in
     * [RuntimeException]s. If called from the EDT, the runnable is simply run.
     */
    fun invokeAndWait(runnable: Runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run()
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable)
            } catch (e: InterruptedException) {
                // swallow
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e.cause)
            }

        }
    }

    /**
     * A wrapper around [SwingUtilities.invokeAndWait].
     */
    @Throws(InterruptedException::class, InvocationTargetException::class)
    fun invokeAndWaitThrowing(runnable: Runnable) {
        SwingUtilities.invokeAndWait(runnable)
    }

    /**
     * A wrapper around [SwingUtilities.invokeAndWait].
     */
    fun invokeLater(runnable: Runnable) {
        SwingUtilities.invokeLater(runnable)
    }

    fun setUIFont(f: FontUIResource) {
        val keys = UIManager.getDefaults().keys()
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            val value = UIManager.get(key)
            if (value != null && value is FontUIResource)
                UIManager.put(key, f)
        }
    }
}