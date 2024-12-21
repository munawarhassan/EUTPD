/**
 *
 * @author christophe friederich
 */
export interface Principal {
    username: string;
    authorities: string[] | null;
    token?: string;
}
