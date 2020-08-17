import React, { useEffect } from "react";
import * as edit from "jabbah-edit";
import * as base from "jabbah-base";
import * as draw from "jabbah-draw";

import useEditorStore from "hooks/useEditorStore";

// const { DrawingViewImpl } = edit.ch.scorpion.jabbah.edit.view;
// const { DrawingImpl } = edit.ch.scorpion.jabbah.edit.model;
const { EditEditorModule } = edit.ch.scorpion.jabbah.edit.editor;
const { EditModuleAccess } = edit.ch.scorpion.jabbah.edit.module;
const { BaseModule } = base.ch.scorpion.jabbah.base.module;
const { DrawModule } = draw.ch.scorpion.jabbah.draw.module;

const CanvasTest: React.FC = () => {
  const setEditor = useEditorStore((state) => state.setEditor);

  useEffect(() => {
    BaseModule.properties.set_bm4g0d$("draw.view.TooltipManager.delay", 1500);
    EditModuleAccess.require();

    const canvas = DrawModule.canvasFactory.create("antares-canvas");
    const editor = EditEditorModule.createEditor(canvas.view);
    setEditor(editor);
    canvas.repaint();
  }, []);

  return <canvas id="antares-canvas" width="800" height="600"></canvas>;
};

export default CanvasTest;
