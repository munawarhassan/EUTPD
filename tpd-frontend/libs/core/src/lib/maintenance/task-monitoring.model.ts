export interface TaskMonitoring {
    cancelToken: string;
    id: string;
    ownerNodeId: string;
    ownerSessionId: string;
    startTime: Date;
    state: string;
    type?: string;
}
