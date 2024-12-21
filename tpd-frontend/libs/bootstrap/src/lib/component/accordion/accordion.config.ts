import { InjectionToken } from '@angular/core';

export const ACCORDION_CONFIG = new InjectionToken<AccordionConfig>('ACCORDION_CONFIG');
/**
 * Configuration service, provides default values for the AccordionComponent.
 */
export interface AccordionConfig {
    /** Whether the other panels should be closed when a panel is opened */
    closeOthers: boolean;
}
