package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.edit.CommandManagerMock
import ch.scorpion.jabbah.io.Storable
import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.matcher.eq
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.*

class ApplicationDataViewControllerTest {

	private val commandManagerMock = CommandManagerMock()
	private val eventBus: EventBus = EventBusImpl()
	private val storableProvider = NewStorableProvider()
	private val repositoryBuilder = ApplicationDataRepositoryMockBuilder()
	private val controller = ApplicationDataViewController(commandManagerMock.build(), storableProvider::provide, repositoryBuilder.build(), eventBus)
	private val viewMock = ApplicationDataViewMockBuilder(controller)
	private var applicationDataEvent: ApplicationDataEvent? = null
	private var currentSavableEvent: CurrentSavableEvent? = null

	init {
		AppTestRule.configure()
		eventBus.register(ApplicationDataEvent::class) { applicationDataEvent = it }
		eventBus.register(CurrentSavableEvent::class) { currentSavableEvent = it }
	}

	private class NewStorableProvider {
		var providedStorable: Storable? = null
		fun provide(): Storable {
			providedStorable = mock(MockMode.autofill)
			return providedStorable!!
		}
	}

	@Test
	fun shouldInitiallyHaveNoData() {
		assertNull(controller.data)
	}

	/** ---- Tests for setting new [ApplicationData] */

	@Test
	fun shouldCreateNewData() {
		commandManagerMock.cannotUndo()

		controller.newData()

		assertSame(storableProvider.providedStorable, controller.data!!.content)
		assertSame(repositoryBuilder.providedSavable, controller.data!!.savable)

		assertNull(applicationDataEvent!!.oldData)
		assertSame(storableProvider.providedStorable, applicationDataEvent!!.newData!!.content)
		assertSame(repositoryBuilder.providedSavable, currentSavableEvent!!.savable)
		verify(exactly(1)) { commandManagerMock.build().reset() }
	}

	@Test
	fun shouldNotCreateNewDataIfCanceledByUser() {
		commandManagerMock.canUndo()
		viewMock.withSaveUnchangedDataDecision(SaveUnchangedDataDecision.Cancel)

		controller.newData()

		assertNull(controller.data)
	}

	@Test
	fun shouldCreateNewDataAfterChangedDataHasSaved() {
		commandManagerMock.cannotUndo()
		controller.newData()

		commandManagerMock.canUndo()
		viewMock
			.withSaveUnchangedDataDecision(SaveUnchangedDataDecision.Yes)
			.withSavableForStoring(DefaultSavable.withIdentification("test"))

		controller.newData()
		val newStorable = storableProvider.providedStorable

		assertSame(newStorable, controller.data!!.content)
		verify(exactly(1)) { repositoryBuilder.repository.store(any(), any()) }
		verify(exactly(3)) { commandManagerMock.build().reset() }
	}

	/** ---- Tests for closing the current [ApplicationData] */

	@Test
	fun shouldNotCloseIfCanceledByUser() {
		controller.newData()
		commandManagerMock.canUndo()
		viewMock.withSaveUnchangedDataDecision(SaveUnchangedDataDecision.Cancel)
		val changedStorable = storableProvider.providedStorable!!

		controller.closeData()

		assertSame(changedStorable, controller.data!!.content)
		verify(exactly (0)) { repositoryBuilder.repository.store(any(), any()) }
	}

	@Test
	fun shouldCloseDataWithSaving() {
		val newSavable = DefaultSavable.withIdentification("test")
		controller.newData()
		commandManagerMock.canUndo()
		viewMock
			.withSaveUnchangedDataDecision(SaveUnchangedDataDecision.Yes)
			.withSavableForStoring(newSavable)
		val changedData = controller.data!!.content

		controller.closeData()

		assertNull(controller.data)
		verify(exactly(1)) { repositoryBuilder.repository.store(eq(newSavable), eq(changedData)) }
		verify(exactly(3)) { commandManagerMock.build().reset() }
	}

	@Test
	fun shouldCloseDataWithoutSaving() {
		controller.newData()
		commandManagerMock.canUndo()
		viewMock.withSaveUnchangedDataDecision(SaveUnchangedDataDecision.No)

		controller.closeData()

		assertNull(controller.data)
		verify(exactly(0)) { repositoryBuilder.repository.store(any(), any()) }
	}

	@Test
	fun shouldNotCloseDataWithCanceledSaving() {
		controller.newData()
		commandManagerMock.canUndo()
		viewMock
			.withSaveUnchangedDataDecision(SaveUnchangedDataDecision.Yes)
			.withSavableForStoring(null)
		val changedStorable = storableProvider.providedStorable!!

		controller.closeData()

		assertSame(changedStorable, controller.data!!.content)
		verify(exactly(0)) { repositoryBuilder.repository.store(any(), any()) }
	}

	/** ---- Tests for opening [ApplicationData] */

	@Test
	fun shouldOpenData() {
		commandManagerMock.cannotUndo()
		val data = ApplicationData(content = mock(), savable = mock(MockMode.autofill), eventBus)

		controller.open { data }

		assertSame(data, controller.data)
		assertNull(applicationDataEvent!!.oldData)
		assertSame(applicationDataEvent!!.newData, data)
	}

	@Test
	fun shouldNotOpenDataIfCanceledByUser() {
		controller.newData()
		commandManagerMock.canUndo()
		viewMock.withSaveUnchangedDataDecision(SaveUnchangedDataDecision.Cancel)
		val changedStorable = storableProvider.providedStorable!!
		val data = ApplicationData(content = mock(), savable = mock(), eventBus)

		controller.open { data }

		assertSame(changedStorable, controller.data!!.content)
	}

	@Test
	fun shouldOpenFromSavable() {
		val existingSavable = DefaultSavable.withIdentification("test")
		val existingStorable = mock<Storable>()
		viewMock.withSavableForLoading(existingSavable)
		repositoryBuilder.withLoadedStorable(existingStorable)

		controller.open()

		assertSame(existingSavable, controller.data!!.savable)
		assertSame(existingStorable, controller.data!!.content)
	}

	/** ---- Tests for saving the current [ApplicationData] */

	@Test
	fun shouldSave() {
		val savable = DefaultSavable.withIdentification("test")
		val data = ApplicationData(content = mock(), savable = savable, eventBus)
		controller.data = data
		commandManagerMock.canUndo()

		controller.save()

		verify { repositoryBuilder.build().store(eq(savable), eq(data.content)) }
		verify(exactly(1)) { commandManagerMock.build().reset() }
	}

	@Test
	fun shouldSaveWithDefiningSavable() {
		val newSavable = DefaultSavable.withIdentification("test")
		controller.newData()
		commandManagerMock.canUndo()
		viewMock.withSavableForStoring(newSavable)

		controller.save()

		verify { repositoryBuilder.build().store(eq(newSavable), eq(controller.data!!.content)) }
		verify(exactly(2)) { commandManagerMock.build().reset() }
	}

	@Test
	fun shouldDisableSaveWithoutData() {
		controller.newData()
		controller.data = null
		assertFalse(controller.saveAction.enabled)
	}
}