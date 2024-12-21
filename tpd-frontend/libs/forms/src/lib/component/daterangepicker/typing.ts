export type DaterangepickerType = {
    startDate?: Date;
    endDate?: Date;
    label?: string;
};

export type ChangedValueEvent = {
    range: DaterangepickerType;
    option?: {
        emitEvent: boolean;
    };
};
