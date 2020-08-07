import React from "react";

import Header from "components/Header";
import GraphPage from "pages/GraphPage";
import ContainerPage from "pages/ContainerPage";
import useEditModeStore from "hooks/useEditModeStore";

const Layout: React.FC = () => {
  const editMode = useEditModeStore((state) => state.editMode);

  return (
    <>
      <Header />
      {editMode === "graph" ? <GraphPage /> : <ContainerPage />}
    </>
  );
};

export default Layout;
