import { Component, EventEmitter, Input, Output } from '@angular/core';
import { WhiteListObserver } from '@devacfr/forms';
import Tagify from '@yaireo/tagify';

const GROUP_SVG = `
<!--begin::Svg Icon | path: assets/media/icons/duotune/communication/com014.svg-->
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
<path d="M16.0173 9H15.3945C14.2833 9 13.263 9.61425 12.7431 10.5963L12.154 11.7091C12.0645 11.8781 12.1072 12.0868 12.2559 12.2071L12.6402 12.5183C13.2631 13.0225 13.7556 13.6691 14.0764 14.4035L14.2321 14.7601C14.2957 14.9058 14.4396 15 14.5987 15H18.6747C19.7297 15 20.4057 13.8774 19.912 12.945L18.6686 10.5963C18.1487 9.61425 17.1285 9 16.0173 9Z" fill="black"/>
<rect opacity="0.3" x="14" y="4" width="4" height="4" rx="2" fill="black"/>
<path d="M4.65486 14.8559C5.40389 13.1224 7.11161 12 9 12C10.8884 12 12.5961 13.1224 13.3451 14.8559L14.793 18.2067C15.3636 19.5271 14.3955 21 12.9571 21H5.04292C3.60453 21 2.63644 19.5271 3.20698 18.2067L4.65486 14.8559Z" fill="black"/>
<rect opacity="0.3" x="6" y="5" width="6" height="6" rx="3" fill="black"/>
</svg>
<!--end::Svg Icon-->`;

@Component({
    selector: 'app-tagify-group',
    templateUrl: 'tagify-group.component.html',
})
export class TagifyGroupComponent {
    @Input()
    public groups!: WhiteListObserver;

    public get value(): Tagify.TagData[] | undefined {
        return this._value;
    }
    @Input()
    public set value(value: Tagify.TagData[] | undefined) {
        if (this._value !== value) {
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
                label: 'Add all',
                name:
                    whitelist.reduce((remainingSuggestions: number, item: Tagify.TagData) => {
                        return tagify.isTagDuplicate(item.value) ? remainingSuggestions : remainingSuggestions + 1;
                    }, 0) + ' Members',
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
            } as any,
        ]);
    }

    public tagTemplate(this: Tagify, tagData: Tagify.TagData): string {
        return `
            <tag title="${tagData.title || tagData.name}"
                    contenteditable='false'
                    spellcheck='false'
                    tabIndex="-1"
                    class="${this.settings?.classNames?.tag} ${tagData.class ? tagData.class : ''}"
                    ${this.getAttributes(tagData)}>
                <x title='' class='tagify__tag__removeBtn' role='button' aria-label='remove tag'></x>
                <div class="d-flex align-items-center">
                    <span class="svg-icon svg-icon-1 me-1">
                    ${GROUP_SVG}
                    </span>
                    <span class='tagify__tag-text'>${tagData.name}</span>
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
                !tagData.label
                    ? `
            <span class="svg-icon svg-icon-2hx me-1">
            ${GROUP_SVG}
            </span>
            <span>${tagData.name}</span>`
                    : `<div class="d-flex flex-column">
            <strong>${tagData.label}</strong>
            <span>${tagData.name}</span>
        </div>`
            }
        </div>
    `;
    }
}
