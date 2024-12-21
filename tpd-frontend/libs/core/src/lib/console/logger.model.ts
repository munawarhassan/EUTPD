export interface LoggerRequest {
    name: string;
    level: string;
}

export interface LogEvent {
    timestamp: number;
    level: string;
    threadName: string;
    message: string;
    throwable: string;
}

export interface LogEvents {
    events: LogEvent[];
}
