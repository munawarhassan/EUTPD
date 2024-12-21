import { ElementStyleUtil, EventHandlerUtil, getCSS } from '@devacfr/util';

interface BlockUIOption {
    zIndex: false;
    overlayClass: string;
    overflow: string;
    message: string;
}

const DefaulBlockUItOptions: BlockUIOption = {
    zIndex: false,
    overlayClass: '',
    overflow: 'hidden',
    message: '<span class="spinner-border text-primary"></span>',
};

export class BlockUI {
    private _el: HTMLElement | string | undefined;
    private _element: HTMLElement | undefined;
    private _options;

    private _overlayElement: HTMLElement | undefined;
    private _blocked = false;
    private _positionChanged = false;
    private _overflowChanged = false;

    constructor(element?: HTMLElement | string, option?: Partial<BlockUIOption>) {
        this._el = element;
        this._options = Object.assign({}, DefaulBlockUItOptions, option || {});
    }

    public block(): BlockUI {
        if (this._element) {
            return this;
        }
        if (!this._el) {
            this._element = document.body;
        } else {
            let el: HTMLElement | null = null;
            if (typeof this._el === 'string') {
                el = document.getElementById(this._el);
                if (!el) {
                    el = document.querySelector(this._el);
                }
            }
            this._element = el == null ? document.body : el;
        }

        if (EventHandlerUtil.trigger(this._element, 'lt.blockui.block') === false) {
            return this;
        }

        const isPage = this._element.tagName === 'BODY';

        const position = getCSS(this._element, 'position');
        const overflow = getCSS(this._element, 'overflow');
        let zIndex = isPage ? '10000' : '1';

        if (this._options.zIndex > 0) {
            zIndex = this._options.zIndex;
        } else {
            if (getCSS(this._element, 'z-index') != 'auto') {
                zIndex = getCSS(this._element, 'z-index');
            }
        }

        this._element.classList.add('blockui');

        if (position === 'absolute' || position === 'relative' || position === 'fixed') {
            ElementStyleUtil.set(this._element, 'position', 'relative');
            this._positionChanged = true;
        }

        if (this._options.overflow === 'hidden' && overflow === 'visible') {
            ElementStyleUtil.set(this._element, 'overflow', 'hidden');
            this._overflowChanged = true;
        }

        this._overlayElement = document.createElement('DIV');
        this._overlayElement.setAttribute('class', 'blockui-overlay ' + this._options.overlayClass);

        this._overlayElement.innerHTML = this._options.message;

        ElementStyleUtil.set(this._overlayElement, 'z-index', zIndex);

        this._element.append(this._overlayElement);
        this._blocked = true;

        EventHandlerUtil.trigger(this._element, 'lt.blockui.after.blocked') === false;
        return this;
    }

    public release(): BlockUI {
        if (!this._element) return this;
        if (EventHandlerUtil.trigger(this._element, 'lt.blockui.release') === false) {
            return this;
        }

        this._element.classList.add('blockui');

        if (this._positionChanged) {
            ElementStyleUtil.set(this._element, 'position', '');
        }

        if (this._overflowChanged) {
            ElementStyleUtil.set(this._element, 'overflow', '');
        }

        if (this._overlayElement) {
            this._overlayElement.remove();
        }

        this._blocked = false;

        EventHandlerUtil.trigger(this._element, 'lt.blockui.released');
        this._element = undefined;
        return this;
    }
}
