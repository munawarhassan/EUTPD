import { ElementRef } from '@angular/core';

export function isNgValidationClass(cl: string): boolean {
    return cl.startsWith('ng-');
}

export function synchronizeStatusChange(control: ElementRef, element: HTMLElement): void {
    const el = control.nativeElement as HTMLElement;
    const controlClasses: string[] = [];
    const elementClasses: string[] = [];
    if (el && el.classList) {
        el.classList.forEach((cl) => (isNgValidationClass(cl) ? controlClasses.push(cl) : void 0));
    }
    if (element && element.classList) {
        element.classList.forEach((cl) => (isNgValidationClass(cl) ? elementClasses.push(cl) : void 0));
        element.classList.remove(...elementClasses);
        element.classList.add(...controlClasses);
    }
}
