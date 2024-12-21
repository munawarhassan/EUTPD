export interface ScmRequest {
    branch: string;
    dirty: string;
    tags: string;
    describe: string;
    shortDescribe: string;
    commitId: string;
    commitIdAbbrev: string;
    buildUserName: string;
    buildUserEmail: string;
    buildTime: string;
    commitUserName: string;
    commitUserEmail: string;
    commitMessageFull: string;
    commitMessageShort: string;
    commitTime: string;
}
