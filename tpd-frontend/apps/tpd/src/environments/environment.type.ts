export type EnvironmentType = {
    production: boolean;
    features: {
        chat: {
            enable: boolean;
        };
        signup: {
            enable: boolean;
        };
    };
};
