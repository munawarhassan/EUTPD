export class RegisterUser {
    constructor(
        public username?: string,
        public displayName?: string,
        public email?: string,
        public password?: string,
        public confirmPassword?: string
    ) {}
}
