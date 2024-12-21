import { Progress } from '@devacfr/util';

export enum ApplicationState {
    Starting = 'STARTING',
    FirstRun = 'FIRST_RUN',
    Running = 'RUNNING',
    Maintenance = 'MAINTENANCE',
    Error = 'ERROR',
    Stopping = 'STOPPING',
}

export enum LifecycleState {
    Created = 'CREATED',
    Failed = 'FAILED',
    Started = 'STARTED',
    Starting = 'STARTING',
}

export interface ApplicationEvent {
    description: string;
    level: {
        level: string;
        description: string;
    };
    date: string;
    progress: number;
    exception: string;
    attributes: Record<string, unknown>;
}

export interface ApplicationStateInfo {
    displayName?: string;
    status?: ApplicationState;
}

export interface LifecyleProgress {
    progress: Progress;
    state: LifecycleState;
}
