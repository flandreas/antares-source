package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.io.Storable


/** A test [Application] implementation used for integration testing. */
class TestGraphApplication(
	newStorableProvider: () -> Storable,
	repository: ApplicationDataRepository<Savable> = UnimplementedApplicationDataRepository(),
	eventBus: EventBus
) : AbstractApplication(ApplicationDataViewController(
	newStorableProvider = newStorableProvider,
	repository = repository,
	eventBus = eventBus)
) {

	override val displayName: String = "Test"

	override fun start() { }

}