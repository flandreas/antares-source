package ch.scorpion.antares

import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.ReferenceResolverImpl
import ch.scorpion.jabbah.io.StoreXmlReader
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Ensures that circuits prior to introduction of [RichText] containing
 * custom component names with parens can still be loaded.
 */
class LegacyFormattedTextTest {

	private val data = """
		<?xml version='1.0' encoding='UTF-8'?>
		<metaGraph _id='0' manualContainer='true'>
		  <graph>
		    <graphStorable _id='1'>
		      <model>
		        <graph _id='2' type='digital'>
		          <elements>
		            <circuitInOut _id='3' id='1' name='A(1)' type='input' bitWidth='1'/>
		          </elements>
		        </graph>
		      </model>
		      <view>
		        <graphView _id='4'>
		          <netViews/>
		          <components>
		            <circuitInOutView _id='5' id='1' rot='0' filled='true' stroked='true' modelId='3' orientation='east' representation='binary'>
		              <location>
		                <point x='-49.0' y='-49.0'/>
		              </location>
		            </circuitInOutView>
		          </components>
		        </graphView>
		      </view>
		    </graphStorable>
		  </graph>
		  <container>
		    <containerDrawing _id='6'>
		      <components>
		        <originIndicator _id='7' id='4'>
		          <location>
		            <point x='-14.0' y='0.0'/>
		          </location>
		        </originIndicator>
		      </components>
		      <model>
		        <subGraphVertice _id='8' id='0' delay='0' uuid='469d91ed-11c9-4d9e-a28a-c6555b8cdd4f'>
		          <name>
		            <translation lang='en' text='Legacy FormattedText'/>
		          </name>
		          <ports/>
		        </subGraphVertice>
		      </model>
		    </containerDrawing>
		  </container>
		</metaGraph>		
	""".trimIndent()

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldReadLegacyFormat() {
		val reader = StoreXmlReader(
			ElectricXmlReader(ByteArrayInputStream(data.toByteArray(), 0, data.length)),
			IOModule.typeMap,
			ReferenceResolverImpl())
		val metaGraph = reader.readStorable<MetaGraph>()

		assertEquals(
			"A(1)",
			metaGraph.graph.graphView.getVerticeViews()
				.filterIsInstance<DigitalCircuitInOutView>()
				.first()
				.label
				.text
		)
	}
}