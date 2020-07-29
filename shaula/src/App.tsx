import React from 'react';
import { ThemeProvider, CSSReset, ColorModeProvider } from "@chakra-ui/core";
import Layout from "./Layout";
import * as base from "jabbah-base";

const App: React.FC = () => {
  const {StringUtils} = base.ch.scorpion.jabbah.base;
  console.log(StringUtils.isEmpty("notEmpty"))

  return (
    <ThemeProvider>
      <ColorModeProvider>
        <CSSReset />
        <Layout />
      </ColorModeProvider>
    </ThemeProvider>
  );
};

export default App;
