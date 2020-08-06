import React from "react";
import { ThemeProvider, CSSReset, ColorModeProvider } from "@chakra-ui/core";

import * as base from "jabbah-base";

import Layout from "./Layout";
import theme from "./styles/theme";

const App: React.FC = () => {
  const { StringUtils } = base.ch.scorpion.jabbah.base;
  console.log(StringUtils.isEmpty("notEmpty"));

  return (
    <ThemeProvider theme={theme}>
      <ColorModeProvider>
        <CSSReset />
        <Layout />
      </ColorModeProvider>
    </ThemeProvider>
  );
};

export default App;
