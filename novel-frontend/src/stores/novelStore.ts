// 小说数据状态管理（zustand）：缓存当前小说、章节、角色和大纲数据
import { create } from 'zustand';
import type { Novel, ChapterBrief, Character, PlotOutline } from '../types';

// NovelState 接口定义：当前上下文小说及其关联数据的增删改方法
interface NovelState {
  currentNovel: Novel | null;
  chapters: ChapterBrief[];
  characters: Character[];
  outlines: PlotOutline[];
  setCurrentNovel: (novel: Novel | null) => void;
  setChapters: (chapters: ChapterBrief[]) => void;
  setCharacters: (characters: Character[]) => void;
  setOutlines: (outlines: PlotOutline[]) => void;
  addChapter: (chapter: ChapterBrief) => void;
  updateChapter: (id: number, data: Partial<ChapterBrief>) => void;
  removeChapter: (id: number) => void;
  addCharacter: (character: Character) => void;
  updateCharacter: (id: number, data: Partial<Character>) => void;
  removeCharacter: (id: number) => void;
}

// 创建全局小说 store：初始状态均为空，由页面组件按需加载填充
export const useNovelStore = create<NovelState>((set) => ({
  currentNovel: null,
  chapters: [],
  characters: [],
  outlines: [],

  // 批量设置方法：直接替换整个数组或当前小说对象
  setCurrentNovel: (novel) => set({ currentNovel: novel }),
  setChapters: (chapters) => set({ chapters }),
  setCharacters: (characters) => set({ characters }),
  setOutlines: (outlines) => set({ outlines }),

  // 添加章节：追加后按章节号升序排列
  addChapter: (chapter) => set((state) => ({
    chapters: [...state.chapters, chapter].sort((a, b) => a.chapterNumber - b.chapterNumber)
  })),

  // 更新单章：按 id 匹配后合并数据
  updateChapter: (id, data) => set((state) => ({
    chapters: state.chapters.map(ch => ch.id === id ? { ...ch, ...data } : ch)
  })),

  // 删除章节：按 id 过滤
  removeChapter: (id) => set((state) => ({
    chapters: state.chapters.filter(ch => ch.id !== id)
  })),

  // 添加角色：直接追加到数组末尾
  addCharacter: (character) => set((state) => ({
    characters: [...state.characters, character]
  })),

  // 更新角色：按 id 匹配后合并数据
  updateCharacter: (id, data) => set((state) => ({
    characters: state.characters.map(ch => ch.id === id ? { ...ch, ...data } : ch)
  })),

  // 删除角色：按 id 过滤
  removeCharacter: (id) => set((state) => ({
    characters: state.characters.filter(ch => ch.id !== id)
  })),
}));
