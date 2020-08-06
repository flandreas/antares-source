import React, { useState } from "react";
import { Box, IconButton, Flex, useColorMode } from "@chakra-ui/core";

// TODO: Better aria desc.

type DrawingMode = "cursor" | "rectangle" | "round" | "text" | "bezier" | "polyline";

interface ToolbarButtonProps {
  drawingMode: DrawingMode;
  isActive: boolean;
  onChange: () => void;
}

const ToolbarButton: React.FC<ToolbarButtonProps> = ({ drawingMode, isActive, onChange }) => {
  const { colorMode } = useColorMode();
  const activeBg = { light: "gray.100", dark: "gray.700" };

  return (
    <IconButton
      aria-label={`Select ${drawingMode} mode`}
      onClick={onChange}
      variant="ghost"
      color="current"
      ml="2"
      fontSize="20px"
      icon={drawingMode}
      bg={isActive ? activeBg[colorMode] : null}
    />
  );
};

const DrawingToolbar: React.FC = () => {
  const [activeDrawingMode, setActiveDrawingMode] = useState<DrawingMode | null>();

  return (
    <Box d="block">
      <Flex size="100%" align="center" justify="space-between">
        <ToolbarButton
          drawingMode="cursor"
          isActive={activeDrawingMode === "cursor"}
          onChange={() => setActiveDrawingMode("cursor")}
        />
        <ToolbarButton
          drawingMode="rectangle"
          isActive={activeDrawingMode === "rectangle"}
          onChange={() => setActiveDrawingMode("rectangle")}
        />
        <ToolbarButton
          drawingMode="round"
          isActive={activeDrawingMode === "round"}
          onChange={() => setActiveDrawingMode("round")}
        />
        <ToolbarButton
          drawingMode="text"
          isActive={activeDrawingMode === "text"}
          onChange={() => setActiveDrawingMode("text")}
        />
        <ToolbarButton
          drawingMode="bezier"
          isActive={activeDrawingMode === "bezier"}
          onChange={() => setActiveDrawingMode("bezier")}
        />
        <ToolbarButton
          drawingMode="polyline"
          isActive={activeDrawingMode === "polyline"}
          onChange={() => setActiveDrawingMode("polyline")}
        />
      </Flex>
    </Box>
  );
};

export default DrawingToolbar;
