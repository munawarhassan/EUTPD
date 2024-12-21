import { NgModule } from '@angular/core';
import { BytesPipe } from './bytes.pipe';
import { CharactersPipe } from './characters.pipe';
import { FilterPipe } from './filter.pipe';
import { FirstLetterPipe } from './first-letter.pipe';
import { JoinPipe } from './join.pipe';
import { ObjectPathPipe } from './object-path.pipe';
import { OrderByPipe } from './order-by.pipe';
import { SplitPipe } from './split.pipe';

@NgModule({
    imports: [],
    exports: [CharactersPipe, FilterPipe, OrderByPipe, FirstLetterPipe, BytesPipe, ObjectPathPipe, JoinPipe, SplitPipe],
    declarations: [
        CharactersPipe,
        FilterPipe,
        OrderByPipe,
        FirstLetterPipe,
        BytesPipe,
        ObjectPathPipe,
        JoinPipe,
        SplitPipe,
    ],
    providers: [],
})
export class PipesModule {}
