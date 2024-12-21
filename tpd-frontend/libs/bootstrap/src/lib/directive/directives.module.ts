import { NgModule } from '@angular/core';
import { AutoResizeDirective, FocusDirective } from '.';
import { CollapseDirective } from './collapse.directive';
import { DrawerDirective } from './drawer.directive';
import { HrefPreventDefaultDirective } from './href-prevent-default.directive';
import { ScrollDirective } from './scroll.directive';
import { StickyDirective } from './sticky.directive';
import { StickyfulDirective } from './stickyful.directive';
import { ToggleDirective } from './toggle.directive';
import { TooltipDirective } from './tooltip.directive';

@NgModule({
    imports: [],
    exports: [
        DrawerDirective,
        ToggleDirective,
        ScrollDirective,
        AutoResizeDirective,
        StickyDirective,
        StickyfulDirective,
        TooltipDirective,
        CollapseDirective,
        HrefPreventDefaultDirective,
        FocusDirective,
    ],
    declarations: [
        DrawerDirective,
        ToggleDirective,
        ScrollDirective,
        AutoResizeDirective,
        StickyDirective,
        StickyfulDirective,
        TooltipDirective,
        CollapseDirective,
        HrefPreventDefaultDirective,
        FocusDirective,
    ],
    providers: [],
})
export class DirectivesModule {}
