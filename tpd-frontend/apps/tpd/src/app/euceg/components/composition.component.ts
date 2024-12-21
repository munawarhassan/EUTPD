import { Component, Input } from '@angular/core';
import { ProductIdentification, ProductIdentificationType } from '@devacfr/euceg';
import { objectPath, Path } from '@devacfr/util';

@Component({
    selector: 'app-composition',
    templateUrl: 'composition.component.html',
})
export class CompositionComponent {
    @Input()
    public title: string | undefined;

    @Input()
    public label: string | undefined;

    @Input()
    public rows = 1;

    @Input()
    public disabled = false;

    // eslint-disable-next-line @angular-eslint/no-input-rename
    @Input('enforce-confidential')
    public enforceConfidential: boolean | undefined;

    @Input()
    public maxlength: number | null = null;

    @Input()
    public path: Path = '';

    @Input()
    public value: unknown | undefined;

    public handleAdd() {
        if (!this.value) {
            this.value = [];
        }
        this.addToArray(this.value, this.path, {
            confidential: false,
            type: 'TEXT',
            value: '',
        });
    }

    public handleRemove(value: ProductIdentification) {
        if (!this.value) {
            return;
        }
        this.removeFromArray(this.value, this.path, value);
    }

    public handleTypeChange(value: ProductIdentification, type: string | (string | undefined)[] | undefined) {
        if (typeof type === 'string') {
            value.type = type as ProductIdentificationType;
        }
    }

    public handleConfidentialChange(value: ProductIdentification, confidential: boolean | undefined) {
        value.confidential = confidential ?? false;
    }

    public handleTextareaChange(value: ProductIdentification, event: Event): void {
        const val: string | null = (event.target as HTMLTextAreaElement).value;
        value.value = val;
    }

    public addToArray(obj: unknown, path: Path, item: ProductIdentification): void {
        objectPath.push(obj, path, item);
    }

    public removeFromArray(obj: unknown, path: Path, item: ProductIdentification) {
        objectPath.remove(obj, path, item);
    }

    public getValue(): ProductIdentification[] {
        return objectPath.get(this.value, this.path, []);
    }
}
