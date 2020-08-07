import React from "react";
import { useColorMode, Box, Flex, IconButton, Stack, StackDivider } from "@chakra-ui/core";
import { MoonIcon, SunIcon } from "@chakra-ui/icons";

import DrawingToolbar from "components/DrawingToolbar";
import EditModeSelector from "components/EditModeSelector";

const Header: React.FC = () => {
  const bg = { light: "white", dark: "gray.800" };
  const { colorMode, toggleColorMode } = useColorMode();

  return (
    <Box
      pos="fixed"
      as="header"
      top="0"
      zIndex={4}
      bg={bg[colorMode]}
      left="0"
      right="0"
      borderBottomWidth="1px"
      width="full"
      height="4rem"
    >
      <Box width="full" mx="auto" px={6} pr={[1, 6]} height="100%">
        <Flex w="100%" h="100%" p={[0, 6]} pl={[0, 4]} align="center" justify="space-between" color="gray.500">
          <Stack direction={["column", "row"]} spacing="12px" divider={<StackDivider />}>
            <EditModeSelector />
            <DrawingToolbar />
          </Stack>
          <IconButton
            aria-label={`Switch to ${colorMode === "light" ? "dark" : "light"} mode`}
            variant="ghost"
            color="current"
            ml="2"
            fontSize="20px"
            icon={colorMode === "light" ? <MoonIcon /> : <SunIcon />}
            onClick={toggleColorMode}
          />
        </Flex>
      </Box>
    </Box>
  );
};

export default Header;
