import { Observable } from 'rxjs';
import { Select2Event } from '../select2';

export interface FormSelectOptionType {
    value: string;
    name: string;
    disabled?: boolean;
}

export type FormSelectOptions = FormSelectOptionType[];

export type FormSelectObserver<T = FormSelectOptions> = (event: Observable<Select2Event>) => Observable<T | T[]>;
