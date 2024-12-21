export type BuildInfo = {
    buildProperties: {
        'scm.build.user.name': string;
        'scm.build.user.email': string;
        'scm.build.time': string;
        'scm.tags': string;
        'scm.commit.short-version': string;
        'scm.commit.id.abbrev': string;
        'scm.commit.id': string;
        'scm.commit.user.email': string;
        'scm.commit.message.full': string;
        'scm.commit.version': string;
        'scm.commit.user.name': string;
        'scm.commit.message.short': string;
        'scm.commit.time': string;
        'scm.branch': string;
        'scm.dirty': boolean;
        'revision.version': string;
    };
    minimumUpgradableBuildNumber: string;
    minimumUpgradableVersion: string;
    version: string;
    currentBuildNumber: string;
    currentBuildDate: Date;
    buildInformation: string;
    currentLongVersion: string;
    commitId: string;
    versionNumbers: number[];
};
export interface GeneralInfo {
    displayName: string;
    version: string;
    defaultLocale: string;
    environment: string;
    buildDate: Date;
    buildNumber: string;
    baseUrl: string;
    homeDirectory: string;
    applicationName: string;
    buildUtilsInfo?: BuildInfo;
}
