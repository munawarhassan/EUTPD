import { EnvironmentType } from './environment.type';

export const environment: EnvironmentType = {
    production: true,
    features: {
        chat: {
            enable: false,
        },
        signup: {
            enable: false,
        },
    },
};
