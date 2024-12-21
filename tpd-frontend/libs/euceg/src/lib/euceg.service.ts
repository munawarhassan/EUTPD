import { formatDate } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { I18nService } from '@devacfr/layout';
import { Observable, of } from 'rxjs';
import { ProductType } from '.';
import { BACKEND_SERVER_API_URL_TOKEN } from '../shared';
import { Euceg } from './euceg';
import { ConfidentialValue } from './product.type';

function getByName<T>(ar: Euceg.NamedValues<T>, name: string): Euceg.NamedValue<T> | undefined {
    let i = 0;
    const len = ar.length;
    for (; i < len; i++) {
        if (ar[i].name === name) {
            return ar[i];
        }
    }
    return undefined;
}

@Injectable({ providedIn: 'root' })
export class EucegService {
    constructor(
        @Inject(BACKEND_SERVER_API_URL_TOKEN) private BACKEND_SERVER_API_URL: string,
        private _http: HttpClient,
        private _i18n: I18nService
    ) {}

    /**
     *
     */
    public get Countries(): Euceg.NamedValues {
        return Euceg.Countries;
    }

    /**
     *
     */
    public get SubmitterTypes(): Euceg.NamedValues {
        const ar = Object.keys(Euceg.SubmitterTypes).map((key) => {
            return { value: key, name: Euceg.SubmitterTypes[key] };
        });
        return ar;
    }

    /**
     *
     * @param submitterType
     * @returns
     */
    public getSubmitterType(submitterType): string {
        const key = submitterType && submitterType.value ? submitterType.value : submitterType;
        if (key) {
            return Euceg.SubmitterTypes[key];
        }
        return '';
    }

    /**
     *
     */
    public get SubmissionTypes(): Euceg.NamedValues<string> {
        const ar = Object.keys(Euceg.SubmissionTypes).map((key) => {
            return { value: key, name: Euceg.SubmissionTypes[key] };
        });
        return ar;
    }

    /**
     *
     * @param submissionType
     * @returns
     */
    public getSubmissionType(
        submissionType: number | string | ConfidentialValue<string> | undefined,
        elipsis = 50
    ): string {
        if (submissionType) {
            let key;
            if (typeof submissionType === 'number' || typeof submissionType === 'string') {
                key = submissionType;
            } else {
                key = submissionType.value;
            }
            if (key) {
                if (elipsis === -1) return Euceg.SubmissionTypes[key];
                else return Euceg.truncateWithEllipses(Euceg.SubmissionTypes[key], elipsis);
            }
        }
        return '';
    }

    public get TobaccoPartTypes(): Observable<Euceg.NamedValues> {
        const ar = Object.keys(Euceg.TobaccoPartTypes).map((key) => {
            return { value: key, name: Euceg.TobaccoPartTypes[key] };
        });
        return of(ar);
    }

    public get IngredientCategories(): Observable<Euceg.NamedValues> {
        const ar = Object.keys(Euceg.IngredientCategories).map((key) => {
            return { value: key, name: Euceg.IngredientCategories[key] };
        });
        return of(ar);
    }

    public get IngredientFunctions(): Observable<Euceg.NamedValues> {
        const ar = Object.keys(Euceg.IngredientFunctions).map((key) => {
            return { value: key, name: Euceg.IngredientFunctions[key] };
        });
        return of(ar);
    }

    public get TobaccoLeafTypes(): Observable<Euceg.NamedValues> {
        return of(
            Object.keys(Euceg.TobaccoLeafTypes).map((key) => {
                return { value: key, name: Euceg.TobaccoLeafTypes[key] };
            })
        );
    }

    public get TobaccoLeafCureMethods(): Observable<Euceg.NamedValues> {
        return of(
            Object.keys(Euceg.TobaccoLeafCureMethods).map((key) => {
                return { value: key, name: Euceg.TobaccoLeafCureMethods[key] };
            })
        );
    }

    public get ToxicologicalDataAvailables(): Observable<Euceg.NamedValues> {
        return of(
            Object.keys(Euceg.ToxicologicalDataAvailables).map((key) => {
                return { value: key, name: Euceg.ToxicologicalDataAvailables[key] };
            })
        );
    }

    public get ToxicityStatus(): Observable<Euceg.NamedValues> {
        return of(
            Object.keys(Euceg.ToxicityStatus).map((key) => {
                return { value: key, name: Euceg.ToxicityStatus[key] };
            })
        );
    }

    public get ReachRegistration(): Observable<Euceg.NamedValues> {
        return of(
            Object.keys(Euceg.ReachRegistration).map((key) => {
                return { value: key, name: Euceg.ReachRegistration[key] };
            })
        );
    }

    public get PackageTypes(): Observable<Euceg.NamedValues> {
        return of(
            Object.keys(Euceg.PackageTypes).map((key) => {
                return { value: key, name: Euceg.PackageTypes[key] };
            })
        );
    }

    public get ProductNumberTypes(): Observable<Euceg.NamedValues> {
        return of(
            Object.keys(Euceg.ProductNumberTypes).map((key) => {
                return { value: key, name: Euceg.ProductNumberTypes[key] };
            })
        );
    }

    public get VoltageWattageAdjustables(): Observable<Euceg.NamedValues<string>> {
        return of(
            Object.keys(Euceg.VoltageWattageAdjustables).map((key) => {
                return { value: key, name: Euceg.VoltageWattageAdjustables[key] };
            })
        );
    }

    public get EmissionNames(): Observable<Euceg.NamedValues<string>> {
        return of(
            Object.keys(Euceg.EmissionNames).map((key) => {
                return { value: key, name: Euceg.EmissionNames[key] };
            })
        );
    }

    public get NationalMarkets(): Observable<Euceg.NamedValues<string>> {
        return of(
            Euceg.NationalMarkets.map((market) => {
                return { name: market.value, value: market.name };
            })
        );
    }

    public getCountries(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/countries');
    }

    public getNationalMarkets(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/nationalMarkets');
    }

    public getTobaccoProductTypes(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/tobaccoProductTypes');
    }

    public getEcigProductTypes(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/ecigProductTypes');
    }

    public getPackageTypes(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/packageTypes');
    }

    public getProductNumberTypes(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/productNumberTypes');
    }

    public getTobaccoLeafCureMethods(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/tobaccoLeafCureMethods');
    }

    public getLeafTypes(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/tobaccoLeafTypes');
    }

    public getTobaccoPartTypes(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/tobaccoPartTypes');
    }

    public getToxicologicalDataAvailables(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/toxicologicalDataAvailables');
    }

    public getIngredientCategories(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/ingredientCategories');
    }

    public getIngredientFunctions(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/ingredientFunctions');
    }

    public getEmissionNames(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/emissionNames');
    }

    public getVoltageWattageAdjustables(): Observable<unknown> {
        return this._http.get(this.BACKEND_SERVER_API_URL + 'refs/voltageWattageAdjustables');
    }

    /**
     *
     */
    public get defaultDateFormat(): string {
        return Euceg.dateFormat;
    }

    /**
     *
     * @param date
     * @returns
     */
    public dateToString(date: Date) {
        if (date) return formatDate(date, Euceg.dateFormat, this._i18n.currentLang);
        return null;
    }

    /**
     *
     * @param name
     * @returns
     */
    public getCountry(name: string): string | undefined {
        const val = getByName(Euceg.Countries, name);
        if (val) return val.value;
        return undefined;
    }

    /**
     *
     * @param partType
     * @returns
     */
    public getTobaccoPartType(partType: ConfidentialValue<string>): string {
        if (partType && partType.value) {
            return Euceg.TobaccoPartTypes[partType.value];
        }
        return '';
    }

    /**
     *
     * @param typeCode
     * @returns
     */
    public getProductNumberType(typeCode: string | undefined): string {
        const val = typeCode ? Euceg.ProductNumberTypes[typeCode] : undefined;
        return val ?? '';
    }

    /**
     *
     * @param leafType
     * @returns
     */
    public getTobaccoLeafType(leafType: ConfidentialValue<string>): string {
        if (leafType && leafType.value) {
            return Euceg.TobaccoLeafTypes[leafType.value];
        }
        return '';
    }

    /**
     *
     * @param leafCureMethod
     * @returns
     */
    public getTobaccoLeafCureMethod(leafCureMethod: ConfidentialValue<string>): string {
        if (leafCureMethod && leafCureMethod.value) {
            return Euceg.TobaccoLeafCureMethods[leafCureMethod.value];
        }
        return '';
    }

    /**
     *
     * @param category
     * @returns
     */
    public getIngredientCategorie(category: ConfidentialValue<string>): string | undefined {
        if (category && category.value) return Euceg.IngredientCategories[category.value];
        return undefined;
    }

    /**
     *
     * @param packageType
     * @returns
     */
    public getPackageType(packageType): string {
        if (packageType && packageType.value) {
            return Euceg.truncateWithEllipses(Euceg.PackageTypes[packageType.value], 15);
        }
        return '';
    }

    /**
     *
     * @param functions
     * @returns
     */
    public getIngredientFunction(functions) {
        const ar: string[] = [];
        if (functions) {
            functions.forEach(function (f) {
                if (typeof f.value === 'string') {
                    ar.push(Euceg.IngredientFunctions[f.value]);
                }
            });
        }
        return Euceg.truncateWithEllipses(ar.join(', '), 20);
    }

    /**
     *
     * @param nationalMarket
     * @returns
     */
    public getNationalMarket(nationalMarket: string | { value: string }): string | undefined {
        let country;
        if (nationalMarket) {
            let key;
            if (typeof nationalMarket === 'string') key = nationalMarket;
            else key = nationalMarket.value;
            if (key) country = getByName(Euceg.NationalMarkets, key);
        }
        if (country) return country.value;
        return undefined;
    }

    /**
     * Gets the name of product type in a specific generic product type.
     * @param type the value of product type in enumeration.
     * @param productType the generic product type.
     */
    public getProductType(type?: number | string | { value: string }, productType?: ProductType): string | undefined {
        let key;
        if (!type || !productType) {
            return undefined;
        }
        if (typeof type === 'number' || typeof type === 'string') key = type;
        else key = type.value;
        if (key) {
            if (productType === 'TOBACCO') {
                return Euceg.TobaccoProductTypes[key];
            } else if (productType === 'ECIGARETTE') {
                return Euceg.EcigProductTypes[key];
            }
        }
        return undefined;
    }

    /**
     * Gets the list of NamedValue containing oll product type for a generic product type
     * @param type the generic product type.
     * @returns
     */
    public getProductTypes(type?: ProductType): Euceg.NamedValues<string> {
        if (type) {
            let types;
            if (type === 'TOBACCO') {
                types = Euceg.TobaccoProductTypes;
            } else if (type === 'ECIGARETTE') {
                types = Euceg.EcigProductTypes;
            }
            return Object.keys(types).map((key) => {
                return { name: types[key], value: key };
            });
        }
        return [];
    }

    /**
     *
     * @param name
     * @returns
     */
    public getEmissionName(name) {
        if (name.value) name = name.value;
        if (name) {
            return Euceg.EmissionNames[name];
        }
    }
}
