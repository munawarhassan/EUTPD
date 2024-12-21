import { Inject, Injectable, InjectionToken } from '@angular/core';
import { objectPath } from '@devacfr/util';
import { BehaviorSubject, Observable } from 'rxjs';
import { LayoutConfig } from '../layout-config';
import { DefaultLayoutConfig } from '../layout-config-default';

export const LAYOUT_CONFIG = new InjectionToken<LayoutConfig>('LAYOUT_CONFIG');
export const LAYOUT_CONFIG_STORAGE_KEY = new InjectionToken<LayoutConfig>('LAYOUT_CONFIG_STORAGE_KEY');

function getEmptyHTMLAttributes(): Record<string, Record<string, string | boolean>> {
    return {
        asideMenu: {},
        headerMobile: {},
        headerMenu: {},
        headerContainer: {},
        pageTitle: {},
    };
}

function getEmptyCssClasses(): {
    [key: string]: string[];
} {
    return {
        header: [],
        headerContainer: [],
        headerMobile: [],
        headerMenu: [],
        aside: [],
        asideMenu: [],
        asideToggle: [],
        toolbar: [],
        toolbarContainer: [],
        content: [],
        contentContainer: [],
        footerContainer: [],
        sidebar: [],
        pageTitle: [],
    };
}

@Injectable({
    providedIn: 'root',
})
export class LayoutService {
    // scope list of css classes
    private classes: {
        [key: string]: string[];
    } = getEmptyCssClasses();

    // scope list of html attributes
    private attrs: Record<string, Record<string, string | boolean>> = getEmptyHTMLAttributes();

    private name: string;
    private _configUpdated$: BehaviorSubject<LayoutConfig>;

    /**
     * Servcie constructor
     */
    public constructor(@Inject(LAYOUT_CONFIG) config?: LayoutConfig, @Inject(LAYOUT_CONFIG_STORAGE_KEY) name?: string) {
        this.name = name || 'layoutConfig';
        // register on config changed event and set default config
        const conf = this.loadConfigs(config);
        this._configUpdated$ = new BehaviorSubject(conf);
    }

    public configUpdated(): Observable<LayoutConfig> {
        return this._configUpdated$;
    }

    /**
     * Get layout config from local storage
     */
    public get config(): LayoutConfig {
        return this._configUpdated$.value;
    }

    public set config(value: LayoutConfig) {
        this.setConfig(value, true);
    }

    /**
     * Set existing config with a new value
     *
     * @param value
     * @param save
     */
    public setConfig(value: LayoutConfig | Partial<LayoutConfig>, save?: boolean): void {
        const config = this._configUpdated$.value;
        if (!config) {
            return;
        }
        const updatedConfig = { ...config, ...value };

        this.classes = getEmptyCssClasses();
        this.attrs = Object.assign({}, getEmptyHTMLAttributes());

        // fire off an event that all subscribers will listen
        this._configUpdated$.next(updatedConfig);
        if (save) {
            this.saveConfig(updatedConfig);
        }
    }

    /**
     * Save layout config to the local storage
     *
     * @param config
     */
    public saveConfig(config: LayoutConfig): LayoutConfig {
        if (config) {
            localStorage.setItem(this.name, JSON.stringify(config));
        }
        return config;
    }

    /**
     * Remove saved layout config and revert back to default
     */
    public removeConfig(): void {
        localStorage.removeItem(this.name);
    }

    public get(path: string): unknown {
        // if path is specified, get the value within object
        return objectPath.get(this.config, path);
    }

    public has(path: string): unknown {
        // if path is specified, get the value within object
        return objectPath.has(this.config, path);
    }

    /**
     * Get layout config from local storage
     */
    public getSavedConfig(): LayoutConfig | null {
        const config = localStorage.getItem(this.name);
        if (config) {
            try {
                return JSON.parse(config);
            } catch (e) {
                // noop
            }
        }
        return null;
    }

    /**
     * Initialize layout config
     *
     * @param config
     */
    private loadConfigs(config?: LayoutConfig): LayoutConfig {
        if (config) {
            this.saveConfig(config);
        }
        let conf = this.getSavedConfig();
        if (!conf) {
            // save default value
            conf = this.saveConfig(DefaultLayoutConfig);
        }
        return conf;
    }

    /**
     * Reload current layout config to the state of latest saved config
     */
    public reloadConfigs(): LayoutConfig {
        let conf = this.getSavedConfig();
        if (!conf) {
            conf = this.loadConfigs();
        }
        this.setConfig(conf, true);
        return conf;
    }

    public getHTMLAttributes(path: string): {
        [attrName: string]: string | boolean;
    } {
        const attributesObj = this.attrs[path];
        if (!attributesObj) {
            return {};
        }
        return attributesObj;
    }

    public setHTMLAttribute(path: string, attrKey: string, attrValue: string | boolean) {
        const attributesObj = this.attrs[path];
        if (!attributesObj) {
            this.attrs[path] = {};
        }
        this.attrs[path][attrKey] = attrValue;
    }

    public getCSSClasses(path: string): string[] {
        const cssClasses = this.classes[path];
        if (!cssClasses) {
            return [];
        }

        return cssClasses;
    }

    public getStringCSSClasses(path: string) {
        return this.getCSSClasses(path).join(' ');
    }

    public setCSSClass(path: string, classesInStr: string) {
        const cssClasses = this.classes[path];
        if (!cssClasses) {
            this.classes[path] = [];
        }
        classesInStr.split(' ').forEach((cssClass: string) => this.classes[path].push(cssClass));
    }
}
