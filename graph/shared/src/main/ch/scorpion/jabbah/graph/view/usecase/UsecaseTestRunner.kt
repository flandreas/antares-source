package ch.scorpion.jabbah.graph.view.usecase

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorImpl
import ch.scorpion.jabbah.execution.actor.SimpleActorData
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.dsl.GraphDslModule
import ch.scorpion.jabbah.base.dsl.ScriptMetaData
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import kotlin.math.max

class UsecaseTestRunner(
	usecases: List<Usecase>,
	val graphView: GraphView,
	private val scheduler: Scheduler,
	private val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val throwFailureException: Boolean = false
) {

	companion object {
		private val LOG by logger(UsecaseTestRunner::class)
	}

	private var _usecase: Usecase? = null

	private lateinit var scriptMetaData: ScriptMetaData

	private var maxAssertionTime: Long = 0

	private val nextUsecases: MutableList<Usecase> = usecases.toMutableList()

	private val issues = mutableListOf<Issue>()

	private val totalUsecaseCount = usecases.size

	fun run() {
		runNext()
	}

	private fun runNext() {
		if (nextUsecases.isNotEmpty()) {
			val usecase = nextUsecases.first()
			nextUsecases.removeAt(0)

			LOG.debug("Running test of usecase '${usecase.name.value}'")

			_usecase = usecase
			scriptMetaData = ScriptMetaData(usecase.name.value, Translations.getString("usecaseTest.issueContext.name"))

			val usecaseActionRunner = UsecaseRunner(usecase, graphView, scheduler, applicationModeHolder)
			applicationModeHolder.setMode(ApplicationMode.EXEC_USECASE) {
				GraphDslModule.usecaseActionExternalFunctions.bind(usecaseActionRunner, _usecase!!.name.value, "Usecase Logic")
				GraphDslModule.usecaseTestExternalFunctions.bind(this, _usecase!!.name.value, "Usecase Test")
				_usecase!!.run()
				_usecase!!.runTest()
				scheduler.requestActingAfter(FinishTestActor(), maxAssertionTime + 1, SimpleActorData())
			}
		}
	}

	/** ---- Methods used by the DSL gateway */

	fun assert(time: Long, description: String? = null, condition: () -> Boolean) {
		maxAssertionTime = max(maxAssertionTime, time)
		scheduler.requestActingAfter(UsecaseTestActor(_usecase!!, condition), delay(time), SimpleActorData(description))
	}

	private fun delay(time: Long): Long {
		return time - scheduler.executionTime
	}

	private inner class UsecaseTestActor(
		private val usecase: Usecase,
		private val condition: () -> Boolean
	) : ActorImpl() {
		override fun act(signalHandler: SignalHandler, data: ActorData) {
			if (!condition.invoke()) {
				LOG.debug("Test '${usecase.name.value}' failed")
				rememberTestFailure(data.dataToString())
				if (throwFailureException) {
					throw UsecaseTestFailureException(usecase)
				}
			}
			super.act(signalHandler, data)
		}
	}

	private inner class FinishTestActor : ActorImpl() {
		override fun act(signalHandler: SignalHandler, data: ActorData) {
			scheduler.isActive = false
			if (nextUsecases.isEmpty()) {
				if (issues.isEmpty()) {
					val messageKey = if (totalUsecaseCount == 1) "usecaseTest.dsl.testSucceeded.name" else "usecaseTest.dsl.allTestsSucceeded.name"
					val messageParam = if (totalUsecaseCount == 1) null else totalUsecaseCount
					eventBus.post(ComponentMessage(
						type = ComponentMessageType.Info,
						source = null,
						messageKey = messageKey,
						messageParam = messageParam))
				} else {
					postIssues()
					eventBus.post(ComponentMessage(
						type = ComponentMessageType.Error,
						source = null,
						messageKey = "usecaseTest.dsl.testFailed.name"))
				}
			} else {
				runNext()
			}
		}
	}

	private fun rememberTestFailure(description: String?) {
		issues.add(IssueImpl(
			severity = IssueSeverity.Warning,
			name = Translations.getString("usecaseTest.dsl.testFailed.name"),
			description = description,
			origin = scriptMetaData.origin,
			context = scriptMetaData.context
		))
	}

	private fun postIssues() {
		issues.forEach { eventBus.post(it) }
	}
}

data class UsecaseTestFailureException(val usecase: Usecase) : Throwable()
