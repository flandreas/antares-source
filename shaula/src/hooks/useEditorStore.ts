import create from "zustand";

import * as edit from "jabbah-edit";

const [useStore] = create((set) => ({
  editor: null,
  setEditor: (editor: edit.ch.scorpion.jabbah.edit.Editor) => set({ editor: editor }),
}));

export default useStore;
