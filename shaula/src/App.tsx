import React from "react";
import { ChakraProvider, CSSReset } from "@chakra-ui/core";
import theme from "@chakra-ui/theme";

// import * as base from "jabbah-base";

import Layout from "components/Layout";

const App: React.FC = () => {
  return (
    <ChakraProvider theme={theme}>
      <CSSReset />
      <Layout />
    </ChakraProvider>
  );
};

export default App;
