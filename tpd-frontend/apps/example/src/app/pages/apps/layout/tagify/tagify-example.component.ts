import { Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { WhiteListObserver } from '@devacfr/forms';
import Tagify from '@yaireo/tagify';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

interface User {
    value: string;
    name: string;
    avatar: string;
    email: string;
}
@Component({
    selector: 'app-tagify-example',
    templateUrl: 'tagify-example.component.html',
})
export class TagifyExampleComponent {
    public usersList: User[] = [
        { value: '1', name: 'Emma Smith', avatar: 'avatars/150-1.jpg', email: 'e.smith@kpmg.com.au' },
        { value: '2', name: 'Max Smith', avatar: 'avatars/150-26.jpg', email: 'max@kt.com' },
        { value: '3', name: 'Sean Bean', avatar: 'avatars/150-4.jpg', email: 'sean@dellito.com' },
        { value: '4', name: 'Brian Cox', avatar: 'avatars/150-15.jpg', email: 'brian@exchange.com' },
        { value: '5', name: 'Francis Mitcham', avatar: 'avatars/150-8.jpg', email: 'f.mitcham@kpmg.com.au' },
        { value: '6', name: 'Dan Wilson', avatar: 'avatars/150-6.jpg', email: 'dam@consilting.com' },
        { value: '7', name: 'Ana Crown', avatar: 'avatars/150-7.jpg', email: 'ana.cf@limtel.com' },
        { value: '8', name: 'John Miller', avatar: 'avatars/150-17.jpg', email: 'miller@mapple.com' },
    ];

    public blacklist: string[] = ['1', '2', '6'];

    public tagsString = 'tag1, tag2';

    public tagUsers: User[] | undefined;

    public tagAsyncUsers: User[] | undefined;

    private addAllSuggestionsElm: HTMLElement | undefined;

    public usersAsyncList: WhiteListObserver = (obs) => {
        return obs.pipe(
            switchMap((term) => {
                const data = this.usersList.filter((v) => v.name.toLowerCase().includes(term.toLowerCase()));
                // console.log('term: ' + term + '. data: ' + data.map((v) => v.name));
                return of({ searchTerm: term, data });
            })
        );
    };

    public submit(form: NgForm): void {
        if (form.invalid) return;
    }

    public onDropdownShow(event: CustomEvent<Tagify.DropDownShowEventData>) {
        const tagify = event.detail.tagify;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const div = event.detail.tagify.DOM.dropdown as any;
        const dropdownContentElm = div.content;

        if (tagify.suggestedListItems && tagify.suggestedListItems.length > 1) {
            this.addAllSuggestionsElm = this.getAddAllSuggestionsElm(tagify);
            if (!this.addAllSuggestionsElm) return;

            // insert "addAllSuggestionsElm" as the first element in the suggestions list
            dropdownContentElm.insertBefore(this.addAllSuggestionsElm, dropdownContentElm.firstChild);
        }
    }

    public onSelectSuggestion(event: CustomEvent<Tagify.DropDownSelectEventData>) {
        const tagify = event.detail.tagify;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const detail = event.detail as any;
        if (detail.elm == this.addAllSuggestionsElm) tagify.dropdown.selectAll.call(tagify);
    }

    // create a "add all" custom suggestion element every time the dropdown changes
    public getAddAllSuggestionsElm(tagify: Tagify): HTMLElement | undefined {
        const whitelist = tagify.settings?.whitelist as Tagify.TagData[];
        if (!whitelist) return undefined;
        // suggestions items should be based on "dropdownItem" template
        return tagify.parseTemplate('dropdownItem', [
            {
                class: 'addAll',
                name: 'Add all',
                email:
                    whitelist.reduce((remainingSuggestions: number, item: Tagify.TagData) => {
                        return tagify.isTagDuplicate(item.value) ? remainingSuggestions : remainingSuggestions + 1;
                    }, 0) + ' Members',
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
            } as any,
        ]);
    }

    public tagTemplate(this: Tagify, tagData: Tagify.TagData): string {
        return `
            <tag title="${tagData.title || tagData.email}"
                    contenteditable='false'
                    spellcheck='false'
                    tabIndex="-1"
                    class="${this.settings?.classNames?.tag} ${tagData.class ? tagData.class : ''}"
                    ${this.getAttributes(tagData)}>
                <x title='' class='tagify__tag__removeBtn' role='button' aria-label='remove tag'></x>
                <div class="d-flex align-items-center">
                    <div class='tagify__tag__avatar-wrap ps-0'>
                        <img onerror="this.style.visibility='hidden'" class="rounded-circle w-25px me-2" src="assets/media/${
                            tagData.avatar
                        }">
                    </div>
                    <span class='tagify__tag-text'>${tagData.name}</span>
                </div>
            </tag>
        `;
    }

    public dropdownItemTemplate(this: Tagify, tagData: Tagify.TagData): string {
        return `
        <div ${this.getAttributes(tagData)}
            class='tagify__dropdown__item d-flex align-items-center ${tagData.class ? tagData.class : ''}'
            tabindex="0"
            role="option">

            ${
                tagData.avatar
                    ? `
                    <div class='tagify__dropdown__item__avatar-wrap me-2'>
                        <img onerror="this.style.visibility='hidden'"  class="rounded-circle w-50px me-2" src="assets/media/${tagData.avatar}">
                    </div>`
                    : ''
            }

            <div class="d-flex flex-column">
                <strong>${tagData.name}</strong>
                <span>${tagData.email}</span>
            </div>
        </div>
    `;
    }
}
