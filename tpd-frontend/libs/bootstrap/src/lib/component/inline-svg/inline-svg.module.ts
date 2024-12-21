import { ModuleWithProviders, NgModule } from '@angular/core';
import { SVGCacheService } from './svg-cache.service';
import { InlineSvgComponent } from './inline-svg.component';
import { InlineSVGContainerComponent } from './inline-svg-container.component';
import { InlineSVGConfig } from './inline-svg.config';
import { InlineSVGDirective } from './inline-svg.directive';
import { InlineSVGService } from './inline-svg.service';
import { CommonModule } from '@angular/common';
import { SvgIcons } from './svg-icons';
import { InlineSvgFileComponent } from './inline-svg-file.component';

@NgModule({
    imports: [CommonModule],
    exports: [InlineSVGDirective, InlineSvgComponent, InlineSvgFileComponent],
    declarations: [InlineSVGDirective, InlineSVGContainerComponent, InlineSvgComponent, InlineSvgFileComponent],
})
export class InlineSVGModule {
    static forRoot(config?: InlineSVGConfig): ModuleWithProviders<InlineSVGModule> {
        return {
            ngModule: InlineSVGModule,
            providers: [{ provide: InlineSVGConfig, useValue: config }, InlineSVGService, SVGCacheService, SvgIcons],
        };
    }
}
