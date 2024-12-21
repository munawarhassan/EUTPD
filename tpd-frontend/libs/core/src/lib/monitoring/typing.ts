export interface ThreadDumps {
    threads?: ThreadDump[];
}

export interface ThreadStackStrace {
    methodName: string;
    fileName: string;
    lineNumber: number;
    className: string;
    nativeMethod: boolean;
}

export interface ThreadDump {
    threadName?: string;
    threadId?: number;
    blockedTime?: number;
    blockedCount?: number;
    waitedTime?: number;
    waitedCount?: number;
    lockName?: string;
    lockOwnerId?: number;
    lockOwnerName?: string;
    inNative?: boolean;
    suspended?: boolean;
    threadState?: string;
    stackTrace?: ThreadStackStrace[];
    lockedMonitors?: unknown[];
    lockedSynchronizers?: unknown[];
    lockInfo?: {
        className?: string;
        identityHashCode?: number;
    };
}

export interface Timers {
    count: number;
    duration_units: string;
    m1_rate: number;
    m5_rate: number;
    m15_rate: number;
    max: number;
    mean: number;
    mean_rate: number;
    min: number;
    p50: number;
    p75: number;
    p95: number;
    p98: number;
    p99: number;
    p999: number;
    rate_units: string;
    stddev: number;
}

export interface HealthCheckItem {
    type: string;
    product: string;
    error: string;
    status: 'UP' | 'DOWN' | 'OUT_OF_SERVICE';
}
