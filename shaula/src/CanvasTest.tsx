import React, { useEffect } from "react";
import * as edit from "jabbah-edit";
import * as base from "jabbah-base";
import * as draw from "jabbah-draw";

// const { Drawing, Component } = edit.ch.scorpion.jabbah.edit;
const { DrawingViewImpl } = edit.ch.scorpion.jabbah.edit.view;
const { DrawingImpl } = edit.ch.scorpion.jabbah.edit.model;
const { EditEditorModule } = edit.ch.scorpion.jabbah.edit.editor;
const { EditModuleJs } = edit.ch.scorpion.jabbah.edit.module;
const { BaseModule } = base.ch.scorpion.jabbah.base.module;
const { CanvasJs } = draw.ch.scorpion.jabbah.draw.view;

const CanvasTest: React.FC = () => {
  useEffect(() => {
    BaseModule.properties.set_bm4g0d$("draw.view.TooltipManager.delay", 1500);
    EditModuleJs.require();

    const canvas = new CanvasJs("antares-canvas", (it) => new DrawingViewImpl(new DrawingImpl(), it));
    EditEditorModule.createEditor_p5bcg4$(canvas.view);
    canvas.repaint();
  }, []);

  return <canvas id="antares-canvas" width="800" height="600"></canvas>;
};

export default CanvasTest;
