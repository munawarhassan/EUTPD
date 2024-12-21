import { Component, EventEmitter, Input, Output } from '@angular/core';
import { WhiteListObserver } from '@devacfr/forms';
import Tagify from '@yaireo/tagify';

@Component({
    selector: 'app-tagify-user',
    templateUrl: 'tagify-user.component.html',
})
export class TagifyUserComponent {
    @Input()
    public users!: WhiteListObserver;

    public get value(): Tagify.TagData[] | undefined {
        return this._value;
    }
    @Input()
    public set value(value: Tagify.TagData[] | undefined) {
        if (value !== this._value) {
            this._value = value;
            this.valueChange.emit(this._value);
        }
    }

    @Input()
    public blacklist: string[] | undefined;

    private addAllSuggestionsElm: HTMLElement | undefined;

    @Output()
    public valueChange = new EventEmitter<Tagify.TagData[] | undefined>();

    private _value: Tagify.TagData[] | undefined;
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
                displayName: 'Add all',
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
                    ${
                        tagData.avatarUrl
                            ? `<div class='tagify__tag__avatar-wrap ps-0'>
                        <img onerror="this.style.visibility='hidden'" class="rounded-circle w-25px me-2" src="${tagData.avatarUrl}">
                    </div>`
                            : ''
                    }
                    <span class='tagify__tag-text'>${tagData.displayName}</span>
                </div>
            </tag>
        `;
    }

    public dropdownItemTemplate(this: Tagify, tagData: Tagify.TagData): string {
        return `
        <div ${this.getAttributes(tagData)} class='tagify__dropdown__item d-flex align-items-center ${
            tagData.class ? tagData.class : ''
        }'
            tabindex="0"
            role="option">

            ${
                tagData.avatarUrl
                    ? `
                    <div class='tagify__dropdown__item__avatar-wrap me-2'>
                        <img onerror="this.style.visibility='hidden'"  class="rounded-circle w-50px me-2" src="${tagData.avatarUrl}">
                    </div>`
                    : ''
            }

            <div class="d-flex flex-column">
                <strong>${tagData.displayName}</strong>
                <span>${tagData.email}</span>
            </div>
        </div>
    `;
    }
}
