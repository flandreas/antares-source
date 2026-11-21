package io.antarescircuit.antares.hdl

/**
 * The korte templates of built-in components with HDL support, keyed by file name (e.g. "vhdl/VHDL_RAM.template").
 * Kept as Kotlin constants to be available on all platforms.
 */
internal object HDLTemplates {

	fun get(fileName: String): String? = templates[fileName]

	private val templates = mapOf(
		"vhdl/VHDL_LookupTable.template" to """library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

{%- if vertice.name == '' || vertice.name == null %}
{{ vhdl.error('antares.vhdl.missingLabel.error', 'ROM') }}
{%- else %}
{%- set entityName = vhdl.setEntityName('VHDL_LookupTable_' + vertice.name) %}
{%- endif %}

entity {{ entityName }} is
  port (
    A: in {{ vhdl.type(vertice.addressWidth.width) }};
    D: out {{ vhdl.type(vertice.dataWidth.width) }});
end {{ entityName }};

architecture Behavioral of {{ entityName }} is
  type memory is array (0 to {{ vertice.memory.nonZeroLength - 1 }}) of {{ vhdl.type(vertice.dataWidth.width) }};
  constant lut : memory := (
{%- for i in 0..vertice.memory.nonZeroLength - 1 %}
    "{{ vertice.dataAsPaddedBinaryAt(i) }}"{% if !loop.last %},{%- endif %}
{%- endfor %});
begin
  process (A)
  begin
    if A >= {{ vhdl.value(vertice.memory.nonZeroLength, vertice.addressWidth) }} then
      D <= {{ vhdl.zero(vertice.dataWidth) }};
    else
      D <= lut(to_integer(unsigned(A)));
    end if;
  end process;
end Behavioral;
""",
		"vhdl/VHDL_RAM.template" to """library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity VHDL_RAM is
  generic (
    addrBitWidth: integer;
    dataBitWidth: integer);
  port (
    A: in std_logic_vector((addrBitWidth - 1) downto 0);{{ vhdl.registerGenericValue('addrBitWidth', vertice.addressWidth.width) }}
    CLK: in std_logic;
    CS: in std_logic;
    WR: in std_logic;
    CLR: in std_logic;
    D: inout std_logic_vector((dataBitWidth - 1) downto 0));{{ vhdl.registerGenericValue('dataBitWidth', vertice.dataWidth.width) }}
end VHDL_RAM;

architecture Behavioral of VHDL_RAM is
  type memoryType is array(0 to (2**addrBitWidth) - 1) of std_logic_vector((dataBitWidth - 1) downto 0);
  signal memory: memoryType;
begin
  process (CLK, CLR)
  begin
    if rising_edge(CLR) then
      memory <= (others => (others => '0'));
    elsif rising_edge(CLK) and (CS='1') and (WR='1') then
      memory(to_integer(unsigned(A))) <= D;
    end if;
  end process;
  D <= memory(to_integer(unsigned(A))) when CS='1' and WR='0' else (others => 'Z');
end Behavioral;
""",
		"vhdl/VHDL_ROM.template" to """library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

{%- if vertice.name == '' || vertice.name == null %}
{{ vhdl.error('antares.vhdl.missingLabel.error', 'ROM') }}
{%- else %}
{%- set entityName = vhdl.setEntityName('VHDL_ROM_' + vertice.name) %}
{%- endif %}

entity {{ entityName }} is
  port (
    D: out {{ vhdl.type(vertice.dataWidth.width) }};
    A: in {{ vhdl.type(vertice.addressWidth.width) }};
    CS: in std_logic);
end {{ entityName }};

architecture Behavioral of {{ entityName }} is
  type memory is array (0 to {{ vertice.memory.nonZeroLength - 1 }}) of {{ vhdl.type(vertice.dataWidth.width) }};
  constant rom : memory := (
{%- for i in 0..vertice.memory.nonZeroLength - 1 %}
    "{{ vertice.dataAsPaddedBinaryAt(i) }}"{% if !loop.last %},{%- endif %}
{%- endfor %});
begin
  process (A, CS)
  begin
    if CS='0' then
      D <= {% if vertice.dataWidth.width > 1 %}(others => 'Z'){% else %}'Z'{% endif %};
    elsif A >= {{ vhdl.value(vertice.memory.nonZeroLength, vertice.addressWidth) }} then
      D <= {{ vhdl.zero(vertice.dataWidth) }};
    else
      D <= rom(to_integer(unsigned(A)));
    end if;
  end process;
end Behavioral;
""",
		"vhdl/VHDL_TriStateBufferGate.template" to """library ieee;
use ieee.std_logic_1164.all;

{%- if vertice.bitWidth.width == 1 %}
    {%- set entityName = vhdl.setEntityName('VHDL_TriStateBufferGate') %}
{%- else %}
    {%- set entityName = vhdl.setEntityName('VHDL_TriStateBufferGate_MultiBit') %}
{%- endif %}

entity {{ entityName }} is
{%- if vertice.bitWidth.width > 1 %}
  generic (bitWidth : integer);{{ vhdl.registerGenericValue('bitWidth', vertice.bitWidth.width) }}
{%- endif %}
  port (
    p1: in {{ vhdl.genericType('bitWidth', vertice.bitWidth.width) }};
    EN: in std_logic;
    p3: out {{ vhdl.genericType('bitWidth', vertice.bitWidth.width) }});
end {{ entityName }};

architecture Behavioral of {{ entityName }} is
begin
  p3 <= p1 when EN = {% if vertice.isNegative == true %}'0'{% else %}'1'{% end %} else {% if vertice.bitWidth.width == 1 %}'Z'{% else %}(others => 'Z'){% endif %};
end Behavioral;
""",
		"verilog/Verilog_LookupTable.template" to """{%- if vertice.name == '' || vertice.name == null -%}
{%- set moduleName = verilog.setModuleName('Verilog_LookupTable') -%}
{%- else -%}
{%- set moduleName = verilog.setModuleName('Verilog_LookupTable_' + verilog.identifier(vertice.name)) -%}
{%- endif -%}
module {{ moduleName }} (
  input {{ verilog.range(vertice.addressWidth.width) }}A,
  output reg {{ verilog.range(vertice.dataWidth.width) }}D);
  always @(*) begin
    case (A)
{%- if vertice.memory.nonZeroLength > 0 %}
{%- for i in 0..vertice.memory.nonZeroLength - 1 %}
      {{ vertice.addressWidth.width }}'d{{ i }}: D = {{ vertice.dataWidth.width }}'b{{ vertice.dataAsPaddedBinaryAt(i) }};
{%- endfor %}
{%- endif %}
      default: D = {{ verilog.zero(vertice.dataWidth) }};
    endcase
  end
endmodule
""",
		"verilog/Verilog_RAM.template" to """{%- if vertice.hasClock == false -%}
{{ verilog.error('antares.verilog.ramWithoutClock.error') }}
{%- endif -%}
{%- if vertice.separateDataPorts == true -%}
{%- set moduleName = verilog.setModuleName('Verilog_RAM_SeparateDataPorts') -%}
{%- else -%}
{%- set moduleName = verilog.setModuleName('Verilog_RAM') -%}
{%- endif -%}
module {{ moduleName }} #(
  parameter addrBitWidth = 8,{{ verilog.registerParameter('addrBitWidth', vertice.addressWidth.width) }}
  parameter dataBitWidth = 8{{ verilog.registerParameter('dataBitWidth', vertice.dataWidth.width) }}
) (
  input [addrBitWidth-1:0] A,
  input CLK,
  input CS,
  input WR,
  input CLR,
{%- if vertice.separateDataPorts == true %}
  input [dataBitWidth-1:0] DI,
  output [dataBitWidth-1:0] D);
{%- else %}
  inout [dataBitWidth-1:0] D);
{%- endif %}
  reg [dataBitWidth-1:0] memory [0:(1 << addrBitWidth) - 1];
  integer i;
  always @(posedge CLK or posedge CLR) begin
    if (CLR) begin
      for (i = 0; i < (1 << addrBitWidth); i = i + 1)
        memory[i] <= {dataBitWidth{1'b0}};
    end else if (CS && WR) begin
      memory[A] <= {% if vertice.separateDataPorts == true %}DI{% else %}D{% endif %};
    end
  end
{%- if vertice.separateDataPorts == true %}
  assign D = CS ? memory[A] : {dataBitWidth{1'bz}};
{%- else %}
  assign D = (CS && !WR) ? memory[A] : {dataBitWidth{1'bz}};
{%- endif %}
endmodule
""",
		"verilog/Verilog_ROM.template" to """{%- if vertice.name == '' || vertice.name == null -%}
{%- set moduleName = verilog.setModuleName('Verilog_ROM') -%}
{%- else -%}
{%- set moduleName = verilog.setModuleName('Verilog_ROM_' + verilog.identifier(vertice.name)) -%}
{%- endif -%}
module {{ moduleName }} (
  input {{ verilog.range(vertice.addressWidth.width) }}A,
  input CS,
  output {{ verilog.range(vertice.dataWidth.width) }}D);
  reg {{ verilog.range(vertice.dataWidth.width) }}data;
  always @(*) begin
    case (A)
{%- if vertice.memory.nonZeroLength > 0 %}
{%- for i in 0..vertice.memory.nonZeroLength - 1 %}
      {{ vertice.addressWidth.width }}'d{{ i }}: data = {{ vertice.dataWidth.width }}'b{{ vertice.dataAsPaddedBinaryAt(i) }};
{%- endfor %}
{%- endif %}
      default: data = {{ verilog.zero(vertice.dataWidth) }};
    endcase
  end
  assign D = CS ? data : {{ verilog.highZ(vertice.dataWidth) }};
endmodule
""",
		"verilog/Verilog_TriStateBufferGate.template" to """{%- if vertice.isNegative == true -%}
{%- set moduleName = verilog.setModuleName('Verilog_TriStateBufferGate_ActiveLow') -%}
{%- else -%}
{%- set moduleName = verilog.setModuleName('Verilog_TriStateBufferGate') -%}
{%- endif -%}
module {{ moduleName }} #(
  parameter bitWidth = 1{{ verilog.registerParameter('bitWidth', vertice.bitWidth.width) }}
) (
  input [bitWidth-1:0] p1,
  input EN,
  output [bitWidth-1:0] p3);
  assign p3 = (EN == 1'b{% if vertice.isNegative == true %}0{% else %}1{% endif %}) ? p1 : {bitWidth{1'bz}};
endmodule
""",
	)
}
