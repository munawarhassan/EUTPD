import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Select2Directive } from './select2.directive';

@NgModule({
    imports: [CommonModule, FormsModule],
    exports: [Select2Directive],
    declarations: [Select2Directive],
    providers: [],
})
export class Select2Module {
    public static forRoot(): ModuleWithProviders<Select2Module> {
        $.fn.select2.defaults.set('theme', 'bootstrap5');
        $.fn.select2.defaults.set('width', '100%');
        $.fn.select2.defaults.set('selectionCssClass', ':all:');

        return {
            ngModule: Select2Module,
            providers: [],
        };
    }
}
