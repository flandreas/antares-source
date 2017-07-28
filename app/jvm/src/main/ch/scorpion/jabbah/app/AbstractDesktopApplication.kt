package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import org.apache.commons.cli.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import java.nio.file.Files
import javax.swing.SwingUtilities

/** Abstract base implementation of the [DesktopApplication] interface. */
abstract class AbstractDesktopApplication(
    args: Array<String>,
    eventBus: EventBus
) : AbstractApplication(eventBus), DesktopApplication {

    private val LOG by logger(AbstractDesktopApplication::class)

    private val commandLine: CommandLine by lazy {
        val options = Options()
        defineOptions(options)
        var cmdLine: CommandLine? = null
        try {
            cmdLine = DefaultParser().parse(options, args)!!
            consumeCommandLine(cmdLine)
        } catch (x: ParseException) {
            LOG.error("Error while parsing options: ${x.message}")
            HelpFormatter().printHelp(displayName, options)
            System.exit(1)
        }
        cmdLine!!
    }

    override lateinit var mainFrame: AbstractApplicationFrame

    init {
        loadProperties()
    }

    /** ---- [Application] */

    override val applicationDataChanged: Boolean get() = mainFrame.applicationDataChanged

    override fun start() {
        SwingUtilities.invokeLater {
            init()
            mainFrame.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    quit()
                }
            })
            mainFrame.isVisible = true
        }
    }

    /** ---- [AbstractApplication] */

    override fun init() {
        mainFrame = createMainFrame()
        mainFrame.jMenuBar = createMenuBarBuilder().menuBar
        DrawViewModule.viewManager.activeView = mainFrame.editor.view
        BusyHandler.register(mainFrame, null)
        SwingUtilities.invokeLater {
            if (commandLine.argList.size == 0) {
                newFile()
            } else {
                openFile(commandLine.argList[0])
            }
        }
    }

    override fun createNewSavable(): Savable {
        return FileSavable.undefined()
    }

    /** ---- [DesktopApplication] */

    override fun quit() {
        if (canReplaceSavable("file.action.quit.name")) {
            shutdown()
        }
    }

    override fun saveAs(): Boolean {
        val fileChooser = JFileChooser()
        fileChooser.isAcceptAllFileFilterUsed = false
        fileChooser.fileFilter = ApplicationFileFilter(this)
        if (savable is FileSavable) {
            if (!(savable as FileSavable).filePath.isNullOrEmpty()) {
                fileChooser.selectedFile = File((savable as FileSavable).filePath)
            }
        }

        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            saveFile(fileChooser.selectedFile.absolutePath)
            return true
        }

        return false
    }

    override fun saveFile(filePath: String) {
        var lFilePath = filePath
        if (!lFilePath.endsWith(fileExtension)) {
            lFilePath = lFilePath + "." + fileExtension
        }
        FileOutputStream(filePath).use {
            try {
                val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
                storeWriter.writeStorable(applicationData!!)
                savable = FileSavable.withPath(lFilePath)
            } catch (e: Throwable) {
                LOG.error("Error while saving '$lFilePath': ${e.message}")
            }
        }
    }

    override fun openFile(filePath: String) {
        if (canReplaceSavable("file.action.open.name")) {
            FileInputStream(filePath).use {
                try {
                    val storeReader = StoreXmlReader(ElectricXmlReader(it))
                    val drawing = storeReader.readStorable()
                    applicationData = drawing
                    savable = FileSavable.withPath(filePath)
                } catch (e: Throwable) {
                    LOG.error("Error while opening '$filePath': ${e.message}")
                }
            }
        }
    }

    /** ---- [AbstractDesktopApplication] */

    protected open fun createMainFrame(): AbstractApplicationFrame {
        val canvas: Canvas = CanvasJvm({ DrawingViewImpl<Drawing<Component>>(DrawingImpl<Component>(), it) })
        val editor: Editor = EditEditorModule.createEditor(canvas.view as DrawingView<Drawing<Component>>)
        return SimpleApplicationFrame(this, editor, emptyList())
    }

    protected open fun createMenuBarBuilder(): MenuBarBuilder {
        return MenuBarBuilder(this, eventBus)
    }

    protected fun shutdown() {
        LOG.info("Shutting $displayName down")
        // TODO Provide service for loading/storing Properties
        mainFrame.dispose()
        storeProperties()
        System.exit(0)
    }

    /**
     * Defines the command line argument [Options] for this [DesktopApplication].
     * This implementation defines an option for the 'home directory'. Subclasses can overwrite this method
     * to add additional options, or to replace it with others.
     */
    protected open fun defineOptions(options: Options) {
        options.addOption(Option.builder("d")
            .required(false)
            .longOpt("directory")
            .desc("Home directory")
            .hasArg()
            .build())
    }

    /**
     * Called by this [AbstractDesktopApplication] after the options have been parsed.
     * This implementation does nothing. Subclasses can overwrite this method in order to consume and use
     * the provided [Options].
     */
    protected open fun consumeCommandLine(@Suppress("UNUSED_PARAMETER") commandLine: CommandLine) {
        // empty
    }

    override fun canReplaceSavable(actionKey: String): Boolean {
        if (!applicationDataChanged) {
            return true
        }

        val answer =JOptionPane.showConfirmDialog(
            mainFrame,
            Translations.getString("application.unsavedData.question"),
            Translations.getString(actionKey),
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE)

        when(answer) {
            JOptionPane.NO_OPTION -> return true
            JOptionPane.CANCEL_OPTION -> return false
            JOptionPane.YES_OPTION -> return savable?.save(this) ?: true
            else -> throw IllegalStateException("unsupported answer")
        }
    }

    private fun getPropertiesPath(): Path {
        return FileSystems.getDefault().getPath(getHomeDirectoryPath().toString(), systemName + ".ini")
    }

    /** Ensures that the application home directory exists by creating it if it doesn't. */
    private fun ensureHomeDirectory() {
        val path = getHomeDirectoryPath()
        if (Files.notExists(path)) {
            LOG.debug("Creating home directory '$path'")
            Files.createDirectories(path)
        }
    }

    /** Returns the fully qualified path of the application's home directory.*/
    protected fun getHomeDirectoryPath(): Path {
        if (commandLine.hasOption("d")) {
            return FileSystems.getDefault().getPath(commandLine.getOptionValue("d"))
        }
        return FileSystems.getDefault().getPath(System.getProperty("user.home"), systemName)
    }

    private fun loadProperties() {
        val path = getPropertiesPath()
        LOG.debug("Loading properties from '$path'")
        FileInputStream(path.toString()).use {
            try {
                val properties = java.util.Properties()
                properties.load(it)
                for (key in properties.keys) {
                    BaseModule.properties.set(key as String, properties[key]!!)
                }
            } catch (x: Throwable) {
                LOG.error("Could not load properties: ${x.message}")
            }
        }
    }

    private fun storeProperties() {
        val path = getPropertiesPath()
        LOG.debug("Storing properties in $path")
        FileOutputStream(path.toString()).use {
            try {
                ensureHomeDirectory()
                val properties = java.util.Properties()
                for (key in BaseModule.properties.getUserPropertyKeys()) {
                    properties.setProperty(key, BaseModule.properties.get(key).toString())
                }
                properties.store(it, null)
            } catch (x: Throwable) {
                LOG.error("Error while storing properties: ${x.message}")
            }
        }
    }
}