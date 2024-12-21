import {
    DataUtil,
    ElementStyleUtil,
    EventHandlerUtil,
    getAttributeValueByBreakpoint,
    getUniqueIdWithPrefix,
} from '@devacfr/util';

export interface FeedbackOptions {
    width?: number;
    placement?: string;
    content?: string;
    type?: string;
}

const defaultFeedbackOptions: FeedbackOptions = {
    width: 100,
    placement: 'top-center',
    content: '',
    type: 'popup',
};

class FeedbackComponent {
    element: HTMLElement;
    options: FeedbackOptions;
    instanceUid: string;
    shown: boolean;

    // Create Instances
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public static createInstances(selector: string) {
        throw new Error('not implemented');
    }

    // Static methods
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public static hasInstace(element: HTMLElement) {
        throw new Error('not implemented');
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public static getInstance(element: HTMLElement) {
        throw new Error('not implemented');
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    public static bootstrap(attr: string = '[data-Feedback]') {
        throw new Error('not implemented');
    }

    constructor(_element: HTMLElement, options: FeedbackOptions) {
        this.element = _element;
        this.options = Object.assign({}, defaultFeedbackOptions, options);
        this.instanceUid = getUniqueIdWithPrefix('feedback');
        this.shown = false;

        // Event handlers
        this._handlers(); // will add in the show popup
        DataUtil.set(this.element, 'feedback', this);
    }

    private _handlers = () => {
        this.element.addEventListener('click', (e: Event) => {
            e.preventDefault();
            this._go();
        });
    };

    private _go = () => {
        // noop
    };

    private showPopup = () => {
        this.element = document.createElement('DIV');

        this.element.classList.add('feedback feedback-popup');
        this.element.innerHTML = this.options.content || '';

        if (this.options.placement === 'top-center') {
            this.setPopupTopCenterPosition();
        }

        document.body.appendChild(this.element);
        this.element.classList.add('feedback-shown');
        this.shown = true;
    };

    private setPopupTopCenterPosition = () => {
        const width = getAttributeValueByBreakpoint(this.options.width?.toString() || '0') as string;
        const height = ElementStyleUtil.get(this.element, 'height');
        this.element.classList.add('feedback-top-center');
        ElementStyleUtil.set(this.element, 'width', width);
        ElementStyleUtil.set(this.element, 'left', '50%');
        ElementStyleUtil.set(this.element, 'top', '-' + height);
    };

    private hidePopup = () => {
        this.element.remove();
    };

    ///////////////////////
    // ** Public API  ** //
    ///////////////////////
    public show = () => {
        if (EventHandlerUtil.trigger(this.element, 'kt.feedback.show') === false) {
            return;
        }

        if (this.options.type === 'popup') {
            this.showPopup();
        }

        EventHandlerUtil.trigger(this.element, 'kt.feedback.shown');

        return this;
    };

    public hide = () => {
        if (EventHandlerUtil.trigger(this.element, 'kt.feedback.hide') === false) {
            return;
        }

        if (this.options.type === 'popup') {
            this.hidePopup();
        }

        this.shown = false;
        EventHandlerUtil.trigger(this.element, 'kt.feedback.hidden');
        return this;
    };

    public isShown = () => {
        return this.isShown;
    };

    public getElement = () => {
        return this.element;
    };

    // Event API
    public on = (name: string, handler: EventListener) => {
        return EventHandlerUtil.on(this.element, name, handler);
    };

    public one = (name: string, handler: EventListener) => {
        return EventHandlerUtil.one(this.element, name, handler);
    };

    public off = (name: string) => {
        return EventHandlerUtil.off(this.element, name);
    };

    public trigger = (name: string, event: Event) => {
        return EventHandlerUtil.trigger(this.element, name, event);
    };
}

export { FeedbackComponent, defaultFeedbackOptions };
