import Tagify from '@yaireo/tagify';
import { Observable } from 'rxjs';

export interface TagifyTagContext {
    tagify: Tagify;
    item: Record<string, unknown>;
}

export type WhiteListResult = { searchTerm: string; data: string[] | Tagify.TagData[] };
export type WhiteListObserver = (obs: Observable<string>) => Observable<WhiteListResult>;
