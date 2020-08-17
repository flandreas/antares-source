import create from "zustand";

const [useStore] = create((set) => ({
  editMode: "graph",
  setGraph: () => set({ editMode: "graph" }),
  setContainer: () => set({ editMode: "container" }),
}));

export default useStore;
