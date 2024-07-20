package ch.scorpion.antares.model.hdl.verilog

import ch.scorpion.antares.hdl.HDLExportTestBenchParams
import ch.scorpion.antares.hdl.HDLModel
import ch.scorpion.antares.hdl.verilog.VerilogRenaming
import ch.scorpion.antares.hdl.verilog.VerilogTestBenchCreator
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.standardlibrary.AbstractStandardLibraryBasedCircuitTest
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.StringCodePrinter
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphView
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.Test

class VerilogTestBenchCreatorIntegrationTest : AbstractStandardLibraryBasedCircuitTest() {

    override fun createCircuit(): GraphView =
        LibraryModule.libraryHolder
            .getMetaGraph(UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82"))
            .graph
            .graphView

    @Test
    fun shouldCreateTestBench() {
        val model = HDLModel(getCircuitView().graph as DigitalGraph, VerilogRenaming())
            .create()
            .apply { renameLabels() }

        val params = HDLExportTestBenchParams(
            VerilogRenaming(),
            "half_adder_tb",
            (getCircuitView().graph as DigitalGraph).testcases.testcases.first(),
            Paths.get("notUsed"),
            30)

        val out = StringCodePrinter()
        VerilogTestBenchCreator(out, model, "half_adder", params).print()
        out.close()

        assertEquals("""
            // Test bench for half_adder
            `timescale 1ns/1ns

            module half_adder_tb;
              reg A;
              reg B;
              wire S;
              wire C;

              Half_Adder Half_Adder0 (
                .A(A),
                .B(B),
                .S(S),
                .C(C)
              );

              reg [3:0] patterns[0:3];
              integer i;

              initial begin
                patterns[0] = 4'b0_0_0_0;
                patterns[1] = 4'b0_1_1_0;
                patterns[2] = 4'b1_0_1_0;
                patterns[3] = 4'b1_1_0_1;

                for (i = 0; i < 4; i = i + 1)
                begin
                  A = patterns[i][3];
                  B = patterns[i][2];
                  #30;
                  if (patterns[i][1] !== 1'hx)
                  begin
                    if (S !== patterns[i][1])
                    begin
                      ${'$'}display("%d:S: Assertion failed, expected %h, actual is %h", i, patterns[i][1], S);
                      ${'$'}finish;
                    end
                  end
                  if (patterns[i][0] !== 1'hx)
                  begin
                    if (C !== patterns[i][0])
                    begin
                      ${'$'}display("%d:C: Assertion failed, expected %h, actual is %h", i, patterns[i][0], C);
                      ${'$'}finish;
                    end
                  end
                end

                ${'$'}display("All tests passed.");
              end
            endmodule

        """.trimIndent(), out.toString())
    }
}