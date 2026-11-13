# Antares improvements from competitor research

This local change set improves simulation throughput, waveform recording, memory workflows, truth-table editing, and command discovery. CircuitVerse, Logisim-evolution, and Digital supplied the behavioral and architectural reference points below. The implementations are original Kotlin changes within Antares' existing module boundaries.

## Research and delivered changes

| Competitor evidence | Antares improvement |
| --- | --- |
| Logisim-evolution's [ordered simulation events](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/circuit/Propagator.java) and Digital's [reused update collections](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/core/Model.java) | Stable binary heap instead of sorting on every insertion; a timestamp index coalesces scheduler slots without a queue scan. Same-time ordering, premature execution, and reentrant scheduling retain focused coverage. |
| Logisim-evolution's [simulation deadlines](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/circuit/Simulator.java) | Timer callbacks yield after approximately 8 ms between scheduler steps. JVM elapsed-time measurement uses monotonic `System.nanoTime()` without allocating `Date` objects. One expensive step can still exceed the budget. |
| Logisim-evolution's [bounded signal storage](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/gui/log/Signal.java) and CircuitVerse's [timing-diagram rendering and download](https://github.com/CircuitVerse/CircuitVerse/blob/master/simulator/src/plotArea.js) | Waveform retention uses a deque, with amortized constant-time extrema, binary-search sample lookup, and direct range iterators. Clearing recordings resets both time modes. VCD export makes retained digital traces usable in external waveform viewers. VCD is an Antares extension of the trace-sharing workflow. |
| Logisim-evolution's [Find Action and memory navigation release notes](https://github.com/logisim-evolution/logisim-evolution/releases/tag/v5.0.0) | Searchable command palette, keyboard navigation through circuit search matches, and paged memory navigation with validated address jumps. |
| Logisim-evolution's [raw memory images](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/resources/doc/en/html/guide/mem/mem-v2raw.html) and [plain and addressed word images](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/resources/doc/en/html/guide/mem/mem-v3word.html) | Import for all three word formats, raw and addressed export, sparse zero runs, transactional validation, and source-independent import redo. Oversized data words are rejected to preserve Antares' width-validation behavior. |
| Digital's [table editing and clipboard workflow](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/gui/components/table/TableDialog.java) and [primitive table storage](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/analyse/quinemc/BoolTableByteArray.java) | Multi-cell fills and clipboard paste form one undo step. Inputs are calculated from row indices, outputs use one byte per cell, and minterms use constant-time arithmetic. |

CircuitVerse's [testbench](https://github.com/CircuitVerse/CircuitVerse/blob/master/simulator/src/testbench.js) was also examined. Antares already has extensive testcase support, so the selected changes improve existing analysis and debugging workflows instead of adding a second test runner.

## Using the additions

- **Find Action:** choose Help → Find Action, or press Ctrl/Cmd+Shift+A. Search action names, menu paths, or acronyms. Disabled actions remain disabled and checkbox commands keep their normal behavior.
- **Circuit search:** open the existing search bar. Partial matching is the default. Enter and Shift+Enter visit the next and previous matches; arrow buttons and the result count expose the same workflow. Replacing the drawing reapplies the visible query.
- **Memory navigation:** open memory contents, enter an address, and choose Go. The selected radix is used unless the address has an explicit `0x`, `0b`, or `0d` prefix. Ctrl/Cmd+G focuses the address field. Previous/next controls and Ctrl/Cmd+PageUp/PageDown move between bounded pages; Show Current reveals the active address. All four layouts can reach every supported nonnegative Int address. Snapshot replacement rebuilds and clamps the displayed page.
- **Memory interchange:** Import detects Antares, `v2.0 raw`, `v3.0 hex words plain`, and `v3.0 hex words addressed` images. Export's format selector offers Antares, raw, and addressed output. Antares preserves cell comments; Logisim exports contain values only. Unsupported byte-oriented formats report an error.
- **Truth tables:** select output cells and press 0, 1, or X to fill them. Copy exports the selected rectangle as tab-separated text. Paste accepts tab-separated, comma-separated, or space-separated 0/1/X values. Invalid rectangles are rejected before editing; a single pasted bit fills the selected output cells. Input columns remain read-only.
- **Waveform export:** connect digital oscilloscope probes, run the simulation, then pause and choose File → Export → Export Waveforms (VCD). Stopping clears recordings. Exports contain retained history only; earlier missing values are unknown. Realtime timestamps are nanoseconds; clocked-mode exports identify their synthetic time axis in a VCD comment. Undefined bits export as `z`, errors as `x`.

## Additional correctness fixes

Sparse memory reads no longer allocate empty segments. Clearing the last cell reclaims its segment, and empty ranges cannot hide later cells from iteration or export. Memory clear/import undo uses sparse snapshots, preserving comments without expanding gaps. Compressed circuit persistence also writes zero gaps as runs and skips them while reading, so sparse high addresses do not require walking every intervening cell. Failed imports leave both memory and the previous data-source path intact; redo uses the validated snapshot even if the source file is edited or deleted.

Paged memory models leave partial-row padding blank and read-only, address comments and disassembly relative to the current page, and keep comments editable in disassembly layouts. Search and paging state refresh after drawing or memory snapshot replacement. English and German labels cover the new controls.

## Verification

Use JDK 25 and the repository's JVM tasks. The implementation includes focused tests for heap ordering and complexity, scheduler coalescing and reentrancy, timer fairness, history retention and extrema, memory interchange and atomicity, sparse undo/redo, address paging and snapshot restoration, clipboard validation, truth-table persistence and selection behavior, command discovery and accessibility, and search navigation.

Validation completed with `JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./gradlew jvmTest`: **2,571 passed, 21 skipped, no failures or errors** across all JVM modules. `git diff --check` passes. A generated VCD was also parsed successfully with the independent PyVCD reader, including a numeric probe label and mixed defined, unknown, and high-impedance bus bits.

Sol-loop review also added coverage for cancelling stale memory cell editors on snapshot restoration and preserving validation errors through command-manager rollback of rejected imports. GPT-6.1 Sol at xhigh effort approved both fixes in round 2, with no open findings. One reviewer ran in each round.

An existing UI test fixture also receives proper teardown: its leaked controller could intercept later library-open requests when test discovery order changed. The explicit previously failing order passes after disposing those controllers.

## Second pass

A second round reviewed the first change set independently, fixed what it found, and added further improvements from the same three projects plus an audit of Antares itself. All changes are local commits; nothing was pushed.

### Fixes to the first pass

- RAM, ROM and lookup tables threw inside the simulation step for addresses at or above 2^31 (e.g. 0x80000000), because the new sparse memory rejects negative Int addresses. Out-of-range addresses now produce undefined (RAM) or error (ROM, LUT) outputs.
- RAM writes passed data through `Int`, truncating words wider than 32 bits and sign-extending 32-bit values with the top bit set. They now keep the full value.
- In hexadecimal mode the address field treated `0B…`/`0D…` as binary/decimal prefixes, although they are hex digits shown in the row headers. Only `0x` is a prefix in hex mode.
- ROMs loaded from a memory library item through a dense text dump, which could exhaust memory for sparse high addresses. They now copy sparsely.
- Memory import decodes leniently instead of rejecting non-UTF-8 comments; truth-table paste accepts `Z`, the text copy produces for undefined cells; VCD export validates before overwriting the target file.
- `AutoAdjustBitWidthTest` failed intermittently: the test thread raced the deferred editor switch on the EDT. It now waits for the EDT. The same fix re-enables `ContainerUIIntegrationTest`, which had been ignored as brittle.

### Simulation engine

| Competitor evidence | Antares improvement |
| --- | --- |
| Digital's primitive `long` values ([ObservableValue](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/core/ObservableValue.java)) and Logisim's interned values ([Value](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/data/Value.java)) | Signal operations reuse cached values; per-value lazy delegates (synchronized in `Word`) are replaced by plain caches or one eager pass; multi-bit gates use long arithmetic when all inputs are defined; `BitWidth.of` is an index lookup. Fixed `Word.bitsAt` reading one bit too many, the `Word`/`DefinedWord` `hashCode` contract, and `DefinedWord.equals` throwing for other types. |
| Logisim's simulator thread catching exceptions ([Simulator](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/circuit/Simulator.java)) | A component that throws while acting is reported as an issue and dropped. Previously its request stayed "acting" forever and froze the whole simulation. |
| Digital's oscillation detection ([Model](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/core/Model.java)) and Logisim's oscillation points ([Propagator](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/circuit/Propagator.java)) | Zero-delay feedback loops that never settle (more than 10,000 waves at one simulation time) are reported once, naming the components involved; with "Pause on Issue" the simulation pauses. Testcase runs and circuit analysis that exceed their iteration limit now list the components still being scheduled. |
| — (scheduler review) | A request made while an actor was still acting (e.g. a zero-delay re-request during a signal animation) was silently dropped by the following `actingDone`. It is now kept and executed in the next wave. |
| — | `FeedbackLoopChecker` enumerated all paths, which is exponential for rejoining paths and could freeze the UI before circuit analysis; it is now a linear depth-first search. Probed signal changes no longer scan all graph elements for the oscilloscope or format and parse probe IDs. |

### Analysis, synthesis and testing

| Competitor evidence | Antares improvement |
| --- | --- |
| Digital's and Logisim's [Karnaugh map views](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/analyze/gui/KarnaughMapPanel.java) | A "Karnaugh Map" tab next to each truth table: 1–6 inputs (5–6 as side-by-side maps), Gray-code headers, colored implicant rectangles including wrap-around groups, the minimized expression, and click-to-cycle editing with undo. |
| Digital's NAnd/NOr/two-input transformations and Logisim's [BuildCircuitButton](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/analyze/gui/BuildCircuitButton.java) | Circuit synthesis offers AND/OR, OR/AND (product of sums), NAND-only and NOR-only circuits, plus a "Two-input gates only" option that builds balanced trees and lifts the 16-input limit. Constant-0 outputs were previously synthesized as constant 1. |
| Digital's [test language](https://github.com/hneemann/Digital/tree/master/src/main/java/de/neemann/digital/testing/parser) (`loop`, expressions) | Test scripts support `loop i 0..15 { … }` (nestable, inside or around `run { }`) and parenthesized integer expressions such as `(i >> 1)` or `(i & 1)`, masked to the port width. Expansions are limited to 1,000,000 vectors. |
| Digital's test-case export from truth tables | "Create Circuit" from a truth table can add a test case generated from that table to the new circuit. |

### HDL export

- **Verilog export** (File → Export → Export Verilog), as offered by Digital, Logisim-evolution and CircuitVerse. It reuses Antares' language-neutral HDL model and supports gates, constants, clocks, tunnels, sub-circuits, buses, splitters, RAM, ROM, lookup tables and tri-state buffers, plus testbenches generated from circuit tests. Generated code for 59 standard-library circuits and 58 testbenches parsed and elaborated cleanly with the slang Verilog front end; Icarus Verilog tests run when `iverilog` is installed.
- Fixes shared with VHDL export: sub-circuit instances used names from before identifier renaming; in/out pins were never bound to their nets; the VHDL ROM swapped address and data widths; the VHDL RAM clear assignment was invalid VHDL. VHDL test benches now drive and check in/out pins like the Verilog ones, and all instances of a VHDL entity get their own generic values.

### Editor

- **Find unconnected ports** (type "All") also reports tunnels whose name occurs only once (per circuit for local tunnels; across the library for global ones, counting each sub-circuit instance), unnamed tunnels, and wires with an open end, inspired by Digital's unconnected-tunnel error. It no longer reports the hidden internal port of every tunnel.
- Pasted or clashing circuit inputs/outputs keep their base name and count up (A3 → A4, Clk → Clk2) instead of becoming I7/O3; a pasted group keeps its order with one common offset (D0, D1 into a circuit with D0..D3 → D4, D5), like Digital's label renamer.

### Editor, persistence and stability (audit)

- Re-importing a library with "replace" could delete the existing library and then fail or purge the new copy. All checks now run before anything is deleted.
- Saving, loading and cloning used equality-based lists for object identities and references (quadratic, and two equal but distinct objects could share an ID). They now use identity maps; saved output is byte-identical for all sample projects.
- Library files are written atomically via a temporary file in the target directory; temporary files and import folders are cleaned up; dictionary save errors are no longer swallowed. ZIP extraction rejects entries outside the target directory before creating anything.
- Large selections: drawing, dragging, deselecting and deleting 2,000 selected components went from 0.2–4.5 s to well under 0.1 s each (lazy container bounding boxes, O(1) selection-model lookup, clipped backdrop layer). Text measurement reuses cached fonts.
- Undo history keeps at most 50 snapshots (`edit.commandManager.maxSnapshots`); unsaved-change detection stays correct after history is discarded.
- Poster viewer and history dialog no longer leak drawing views and event handlers; the graphics save/restore stack is per instance and a throwing drawable no longer stops painting of other layers; login and rating requests no longer block the EDT; `EventBus` vetoable and two-phase posts copy their handler lists.

### Compatibility notes

- Test scripts: `loop` is now a keyword (a pin named `loop` must be quoted as `'loop'`); an unspaced `X`/`Z` followed by a letter or digit (e.g. `1X0`) now reads as a name; scripts that expand to more than 1,000,000 vectors (e.g. 20 or more `X` inputs in one row) are rejected. None of the 189 scripts in this repository are affected.
- HDL export rejects an in/out pin that shares its net with another circuit pin instead of silently leaving it unconnected.
- Undo history is capped at 50 snapshots (about 500 commands by default).

### Verification

`./gradlew jvmTest` with JDK 25 passes on the final state (see the commit log for the exact counts per run). An independent review of the second pass found no critical defects; its confirmed findings (a stale animation callback that could still drop a re-armed scheduler request, actor state shown as idle while re-armed, empty loop bodies bypassing the vector limit, port names with line breaks breaking paste, generated test cases for undefined outputs, and Verilog testbench roles on in/out nets) are fixed.
