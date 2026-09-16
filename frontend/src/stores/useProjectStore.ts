import { create } from 'zustand';

export interface ProjectContext {
  projectId: string;
  projectCode: string;
  projectName: string;
  machineType: string;
  activeBranch: string;
  currentRevision: string;
  status: 'IN_WORK' | 'RELEASED' | 'LOCKED' | 'ARCHIVED';
  spindleType: string;
  maxRpm: number;
}

interface ProjectState {
  currentProject: ProjectContext;
  updateProject: (patch: Partial<ProjectContext>) => void;
}

export const useProjectStore = create<ProjectState>((set) => ({
  currentProject: {
    projectId: 'PRJ-VMC850',
    projectCode: 'VMC-850-5AXIS',
    projectName: '高刚度立式五轴加工中心 (正向研发总体工程)',
    machineType: '立式五轴联动加工中心 (AC双摆台)',
    activeBranch: 'main',
    currentRevision: 'Rev.C-2026.09',
    status: 'IN_WORK',
    spindleType: 'DIRECT_DRIVE',
    maxRpm: 12000,
  },
  updateProject: (patch) => {
    set((state) => ({
      currentProject: { ...state.currentProject, ...patch },
    }));
  },
}));
