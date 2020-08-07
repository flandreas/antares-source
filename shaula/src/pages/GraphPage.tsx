import React from "react";
import { Box, useColorMode } from "@chakra-ui/core";

import SideNav from "components/SideNav";
import CanvasTest from "components/CanvasTest";

const GraphPage: React.FC = () => {
  const { colorMode } = useColorMode();

  return (
    <Box>
      <SideNav display={["none", null, "block"]} maxWidth="18rem" width="full" />
      <Box pl={[0, null, "18rem"]} mt="4rem">
        <Box
          as="section"
          backgroundColor={colorMode === "light" ? "gray.100" : "gray.900"}
          minHeight="calc(100vh - 4rem)"
        >
          <Box>
            <CanvasTest />
          </Box>
        </Box>
      </Box>
    </Box>
  );
};

export default GraphPage;
