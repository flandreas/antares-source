import React from "react";
import { Flex, IconButton, useColorMode } from "@chakra-ui/core";
import { BsBoundingBoxCircles, BsTextarea } from "react-icons/bs";

import useEditModeStore from "hooks/useEditModeStore";

const activeBg = { light: "gray.100", dark: "gray.700" };

const EditModeSelector: React.FC = () => {
  const editMode = useEditModeStore((state) => state.editMode);
  const setGraph = useEditModeStore((state) => state.setGraph);
  const setContainer = useEditModeStore((state) => state.setContainer);

  const { colorMode } = useColorMode();

  return (
    <Flex size="100%" align="center" justify="space-between">
      <IconButton
        aria-label="Select graph mode"
        onClick={setGraph}
        variant="ghost"
        color="current"
        ml="2"
        fontSize="20px"
        bg={editMode === "graph" ? activeBg[colorMode] : undefined}
        icon={<BsBoundingBoxCircles />}
      />
      <IconButton
        aria-label="Select container mode"
        onClick={setContainer}
        variant="ghost"
        color="current"
        ml="2"
        fontSize="20px"
        bg={editMode === "container" ? activeBg[colorMode] : undefined}
        icon={<BsTextarea />}
      />
    </Flex>
  );
};

export default EditModeSelector;
