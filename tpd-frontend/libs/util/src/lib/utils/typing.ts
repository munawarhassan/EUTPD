// global css namespace
export type BsColor =
    | 'primary'
    | 'secondary'
    | 'success'
    | 'info'
    | 'warning'
    | 'danger'
    | 'gray-100'
    | 'gray-200'
    | 'gray-300'
    | 'gray-400'
    | 'gray-500'
    | 'gray-600'
    | 'gray-700'
    | 'gray-800'
    | 'gray-900'
    | 'muted'
    | 'dark'
    | 'white';

export type Breakpoint = 'default' | 'sm' | 'md' | 'lg' | 'xl';

export type BreakpointValue<T = string> = T | PartialRecord<Breakpoint, T>;

export interface OffsetModel {
    top: number;
    left: number;
}

export interface ViewPortModel {
    width: number;
    height: number;
}

export type BsTheme = 'light' | 'dark';

export type BsDeviceMode = 'sm' | 'md' | 'lg' | 'xl' | 'xxl';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type PartialRecord<K extends keyof any, T> = {
    [P in K]?: T;
};
