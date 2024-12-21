import { Euceg } from '@devacfr/euceg';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export type MarketSymbolValue = {
    countryName: string;
    iso: string;
};

export function fromNationalMarkets(
    presentations: { nationalMarketName: string; nationalMarket: string }[]
): (obs: Observable<unknown>) => Observable<MarketSymbolValue[]> {
    return (obs: Observable<unknown>) =>
        obs.pipe(
            map(() => {
                const result: MarketSymbolValue[] = [];
                const map = new Map();
                if (presentations) {
                    for (const item of presentations) {
                        if (!map.has(item.nationalMarket)) {
                            map.set(item.nationalMarket, true); // set any value to Map
                            let stateName = item.nationalMarketName;
                            const c = Euceg.NationalMarkets.find((v) => v.name === item.nationalMarket)?.value;
                            if (c) stateName = c;
                            result.push({
                                countryName: stateName,
                                iso: item.nationalMarket,
                            } as MarketSymbolValue);
                        }
                    }
                }
                return result;
            })
        );
}

export function fromNationalMarket(
    countryCode: string | undefined
): (obs: Observable<unknown>) => Observable<MarketSymbolValue[]> {
    return (obs: Observable<unknown>) =>
        obs.pipe(
            map(() => {
                const country = Euceg.Countries.find((c) => c.name === countryCode);
                if (country) {
                    return [
                        {
                            countryName: country.value,
                            iso: country.name,
                        } as MarketSymbolValue,
                    ];
                } else {
                    return [];
                }
            })
        );
}
