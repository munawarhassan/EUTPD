// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace Euceg {
    export interface NamedValue<T = unknown> {
        name: string;
        value: T;
    }
    export type NamedValues<T = unknown> = NamedValue<T>[];

    export const dateFormat = 'YYYY-MM-DD';

    // WARNING : Key values are index values, not XmlValues
    export const TobaccoProductTypes = {
        '1': 'Cigarette',
        '2': 'Cigar',
        '3': 'Cigarillo',
        '4': 'Roll your own tobacco',
        '5': 'Pipe Tobacco',
        '6': 'Waterpipe tobacco',
        '7': 'Oral tobacco',
        '8': 'Nasal tobacco',
        '9': 'Chewing tobacco',
        '10': 'Herbal product for smoking',
        '11': 'Novel tobacco product',
        '12': 'Other tobacco product',
    };
    export const EcigProductTypes = {
        '1': 'Electronic cigarette – Disposable',
        // eslint-disable-next-line max-len
        '2': 'Electronic cigarette – Rechargeable, placed on the market with one type of e-liquid (fixed combination). Any rechargeable which can also be used as a refillable should be reported under the refillable category',
        // eslint-disable-next-line max-len
        '3': 'Electronic cigarette – Rechargeable, device only.  Any rechargeable which can also be used as a refillable should be reported under the refillable category ',
        '4': 'Electronic cigarette – Refillable, placed on the market with one type of e-liquid (fixed combination).',
        '5': 'Electronic cigarette – Refillable, device only',
        // eslint-disable-next-line max-len
        '6': 'Kit – Pack containing more than one different e-cigarette device and/or more than one different refill container/cartridge',
        '7': 'Refill container/cartridge containing e-liquid',
        '8': 'Individual part of electronic cigarette capable of containing e-liquid',
        '9': 'Other',
    };
    export const SubmissionTypes = {
        '1': 'New product',
        '2': 'Modification of information on a previously reported product leading to a new TP-ID number',
        '3': 'Update – addition of information',
        '4': 'Update – withdrawal of information',
        '5': 'Update – other information',
        '6':
            'Update - to be submitted in regular intervals/annually ' +
            'such as sales data or actual quantities of ingredients',
        '7': 'Correction',
    };
    export const ProductNumberTypes = {
        SUBMITTER: 'Submitter identifier number',
        UPC: 'Universal Product Code',
        EAN: 'European Article Number',
        GTIN: 'Global Trade Identification Number',
        SKU: 'Stock Keeping Unit',
    };
    export const PackageTypes = {
        '1': 'Flip top box, square corner',
        '2': 'Flip top box, bevel corner/octagonal',
        '3': 'Flip top box, rounded corner',
        '4': 'Shoulder hinged box',
        '5': 'Soft pack',
        '6': 'Pouch with flap',
        '7': 'Bucket (Cylindrical or cuboid)',
        '8': 'Cuboid can',
        '9': 'Block/Foil pack',
        '10': 'Cylinder card/can',
        '11': 'Standing pouch',
        '12': 'Folding box',
        '13': 'Carton box',
        '14': 'Hinged box',
        '15': 'Hinged tin',
        '16': 'Flip top pack',
        '17': 'Single tube tin',
        '18': 'Bundle',
        '19': 'Multi cigar tube',
        '20': 'Cylinder tin',
        '21': 'Round tin',
        '22': 'Standing pouch roll-fold',
        '23': 'Slide lid box',
        '24': 'Flow wrap',
        '25': 'Folding pouch',
        '26': 'Shell/Hull & Slide box',
        '27': 'Multi-pack display',
        '28': 'Plastic Container',
    };

    export const SubmitterTypes = {
        MANUFACTURER: 'Manufacturer',
        IMPORTER: 'Importer',
    };
    export const TobaccoLeafCureMethods = {
        '1': 'Air',
        '2': 'Fire',
        '3': 'Steam',
        '4': 'Sun',
        '5': 'Flue',
        '6': 'Other',
    };
    export const TobaccoPartTypes = {
        '1': 'Tobacco leaf',
        '2': 'Manufactured – Cut stems',
        '3': 'Manufactured - Reconstituted tobacco',
        '4': 'Manufactured - Expanded tobacco',
        '5': 'Other/Unspecified',
    };
    export const TobaccoLeafTypes = {
        '1': 'Virginia',
        '2': 'Burley',
        '3': 'Oriental',
        '4': 'Maryland',
        '5': 'Kentucky',
        '6': 'Dark',
        '7': 'Other',
        '8': 'Unspecified',
    };
    export const IngredientCategories = {
        '1': 'Tobacco (burnt)',
        '2': 'Tobacco (unburnt)',
        '3': 'Paper (burnt)',
        '4': 'Side seam adhesive (burnt )',
        '5': 'Inks used on cigarette paper (burnt)',
        '6': 'Filtration material (unburnt)',
        '7': 'Filter overwrap (unburnt)',
        '8': 'Filter adhesive (unburnt)',
        '9': 'Tipping paper and tipping paper inks (unburnt)',
        '10': 'Adhesive (unburnt)',
        '11': 'Adhesive (burnt)',
        '12': 'Tips (unburnt)',
        '13': 'Pouch material (unburnt)',
        '14': 'Paper (unburnt)',
        '15': 'Other (unburnt)',
    };
    export const IngredientFunctions = {
        '1': 'Addictive Enhancer',
        '2': 'Adhesive',
        '3': 'Binder',
        '4': 'Carrier',
        '5': 'Colour',
        '6': 'Combustion Modifier',
        '7': 'Casing',
        '8': 'Fibre',
        '9': 'Filler',
        '10': 'Filter Component',
        '11': 'Filtration Material',
        '12': 'Flavour and/or Taste Enhancer',
        '13': 'Humectant',
        '14': 'pH Modifier',
        '15': 'Plasticiser',
        '16': 'Preservative',
        '17': 'Solvent - Processing Aid',
        '18': 'Reduced Ignition Propensity Agent',
        '19': 'Sizing Agent',
        '20': 'Smoke Enhancer',
        '21': 'Smoke Colour Modifier',
        '22': 'Smoke Odour Modifier',
        '23': 'Wrapper',
        '24': 'Water-Wetting Agents',
        '25': 'Viscosity Modifier',
        '26': 'Other',
    };
    export const ToxicityStatus = {
        '0': 'No available information on the ingredient’s toxicit',
        '1': 'Not toxic and without CMR properties.',
        '2': 'Identified as toxic and or with CMR properties.',
    };

    export const ReachRegistration = {
        NOT_YET_REGISTERED: 'Not yet registered',
        EXEMPTED: 'Exempted',
    };
    export const ToxicologicalDataAvailables = {
        '1': 'No toxicological data available',
        '2': 'Toxicological data is available but not new',
        '3': 'New toxicological data has been obtained since the last reporting period',
    };
    export const EmissionNames = {
        '1': 'Nicotine',
        '2': 'Ethylene glycol',
        '3': 'Diethylene glycol',
        '4': 'Formaldehyde',
        '5': 'Acetaldehyde',
        '6': 'Acrolein',
        '7': 'Crotonaldehyde',
        '8': 'SNA: NNN',
        '9': 'TSNA: NNK',
        '10': 'Cadmium',
        '11': 'Chromium',
        '12': 'Copper',
        '13': 'Lead',
        '14': 'Nickel',
        '15': 'Arsenic',
        '16': 'Toluene',
        '17': 'Benzene',
        '18': '1,3-Butadiene',
        '19': 'Isoprene',
        '20': 'Diacetyl',
        '21': 'Acetyl Propionyl',
        '22': 'Other',
    };
    export const VoltageWattageAdjustables = {
        '1': 'Yes, voltage and wattage adjustable',
        '2': 'Yes, only voltage adjustable',
        '3': 'Yes, only wattage adjustable',
        '4': 'No, un-adjustable',
    };

    export const NationalMarkets: NamedValues<string> = [
        {
            name: 'AT',
            value: 'Austria',
        },
        {
            name: 'BE',
            value: 'Belgium',
        },
        {
            name: 'BG',
            value: 'Bulgaria',
        },
        {
            name: 'HR',
            value: 'Croatia',
        },
        {
            name: 'CY',
            value: 'Cyprus',
        },
        {
            name: 'CZ',
            value: 'Czech Republic',
        },
        {
            name: 'DK',
            value: 'Denmark',
        },
        {
            name: 'EE',
            value: 'Estonia',
        },
        {
            name: 'FI',
            value: 'Finland',
        },
        {
            name: 'FR',
            value: 'France',
        },
        {
            name: 'DE',
            value: 'Germany',
        },
        {
            name: 'GR',
            value: 'Greece',
        },
        {
            name: 'HU',
            value: 'Hungary',
        },
        {
            name: 'IS',
            value: 'Iceland',
        },
        {
            name: 'IE',
            value: 'Ireland',
        },
        {
            name: 'IT',
            value: 'Italy',
        },
        {
            name: 'LV',
            value: 'Latvia',
        },
        {
            name: 'LI',
            value: 'Liechtenstein',
        },
        {
            name: 'LT',
            value: 'Lithuania',
        },
        {
            name: 'LU',
            value: 'Luxembourg',
        },
        {
            name: 'MT',
            value: 'Malta',
        },
        {
            name: 'NL',
            value: 'Netherlands',
        },
        {
            name: 'NO',
            value: 'Norway',
        },
        {
            name: 'PL',
            value: 'Poland',
        },
        {
            name: 'PT',
            value: 'Portugal',
        },
        {
            name: 'RO',
            value: 'Romania',
        },
        {
            name: 'SK',
            value: 'Slovakia',
        },
        {
            name: 'SI',
            value: 'Slovenia',
        },
        {
            name: 'ES',
            value: 'Spain',
        },
        {
            name: 'SE',
            value: 'Sweden',
        },
        {
            name: 'GB',
            value: 'United Kingdom',
        },
    ];

    export const Countries: NamedValues<string> = [
        {
            name: 'AF',
            value: 'Afghanistan',
        },
        {
            name: 'AL',
            value: 'Albania',
        },
        {
            name: 'DZ',
            value: 'Algeria',
        },
        {
            name: 'AS',
            value: 'American Samoa',
        },
        {
            name: 'AD',
            value: 'Andorra',
        },
        {
            name: 'AO',
            value: 'Angola',
        },
        {
            name: 'AI',
            value: 'Anguilla',
        },
        {
            name: 'AQ',
            value: 'Antarctica',
        },
        {
            name: 'AG',
            value: 'Antigua and Barbuda',
        },
        {
            name: 'AR',
            value: 'Argentina',
        },
        {
            name: 'AM',
            value: 'Armenia',
        },
        {
            name: 'AW',
            value: 'Aruba',
        },
        {
            name: 'AU',
            value: 'Australia',
        },
        {
            name: 'AT',
            value: 'Austria',
        },
        {
            name: 'AZ',
            value: 'Azerbaijan',
        },
        {
            name: 'BS',
            value: 'Bahamas',
        },
        {
            name: 'BH',
            value: 'Bahrain',
        },
        {
            name: 'BD',
            value: 'Bangladesh',
        },
        {
            name: 'BB',
            value: 'Barbados',
        },
        {
            name: 'BY',
            value: 'Belarus',
        },
        {
            name: 'BE',
            value: 'Belgium',
        },
        {
            name: 'BZ',
            value: 'Belize',
        },
        {
            name: 'BJ',
            value: 'Benin',
        },
        {
            name: 'BM',
            value: 'Bermuda',
        },
        {
            name: 'BT',
            value: 'Bhutan',
        },
        {
            name: 'BO',
            value: 'Bolivia',
        },
        {
            name: 'BQ',
            value: 'Bonaire, Sint Eustatius and Saba',
        },
        {
            name: 'BA',
            value: 'Bosnia and Herzegovina',
        },
        {
            name: 'BW',
            value: 'Botswana',
        },
        {
            name: 'BV',
            value: 'Bouvet Island',
        },
        {
            name: 'BR',
            value: 'Brazil',
        },
        {
            name: 'IO',
            value: 'British Indian Ocean Territory',
        },
        {
            name: 'VG',
            value: 'British Virgin Islands',
        },
        {
            name: 'BN',
            value: 'Brunei',
        },
        {
            name: 'BG',
            value: 'Bulgaria',
        },
        {
            name: 'BF',
            value: 'Burkina Faso',
        },
        {
            name: 'BI',
            value: 'Burundi',
        },
        {
            name: 'KH',
            value: 'Cambodia',
        },
        {
            name: 'CM',
            value: 'Cameroon',
        },
        {
            name: 'CA',
            value: 'Canada',
        },
        {
            name: 'CV',
            value: 'Cape Verde',
        },
        {
            name: 'KY',
            value: 'Cayman Islands',
        },
        {
            name: 'CF',
            value: 'Central African Republic',
        },
        {
            name: 'TD',
            value: 'Chad',
        },
        {
            name: 'CL',
            value: 'Chile',
        },
        {
            name: 'CN',
            value: 'China',
        },
        {
            name: 'CX',
            value: 'Christmas Island',
        },
        {
            name: 'CC',
            value: 'Cocos Islands',
        },
        {
            name: 'CO',
            value: 'Colombia',
        },
        {
            name: 'KM',
            value: 'Comoros',
        },
        {
            name: 'CG',
            value: 'Congo',
        },
        {
            name: 'CK',
            value: 'Cook Islands',
        },
        {
            name: 'CR',
            value: 'Costa Rica',
        },
        {
            name: 'HR',
            value: 'Croatia',
        },
        {
            name: 'CU',
            value: 'Cuba',
        },
        {
            name: 'CW',
            value: 'Curaçao',
        },
        {
            name: 'CY',
            value: 'Cyprus',
        },
        {
            name: 'CZ',
            value: 'Czech Republic',
        },
        {
            name: 'CI',
            value: "Côte d'Ivoire",
        },
        {
            name: 'DK',
            value: 'Denmark',
        },
        {
            name: 'DJ',
            value: 'Djibouti',
        },
        {
            name: 'DM',
            value: 'Dominica',
        },
        {
            name: 'DO',
            value: 'Dominican Republic',
        },
        {
            name: 'EC',
            value: 'Ecuador',
        },
        {
            name: 'EG',
            value: 'Egypt',
        },
        {
            name: 'SV',
            value: 'El Salvador',
        },
        {
            name: 'GQ',
            value: 'Equatorial Guinea',
        },
        {
            name: 'ER',
            value: 'Eritrea',
        },
        {
            name: 'EE',
            value: 'Estonia',
        },
        {
            name: 'ET',
            value: 'Ethiopia',
        },
        {
            name: 'FK',
            value: 'Falkland Islands',
        },
        {
            name: 'FO',
            value: 'Faroe Islands',
        },
        {
            name: 'FJ',
            value: 'Fiji',
        },
        {
            name: 'FI',
            value: 'Finland',
        },
        {
            name: 'FR',
            value: 'France',
        },
        {
            name: 'GF',
            value: 'French Guiana',
        },
        {
            name: 'PF',
            value: 'French Polynesia',
        },
        {
            name: 'TF',
            value: 'French Southern Territories',
        },
        {
            name: 'GA',
            value: 'Gabon',
        },
        {
            name: 'GM',
            value: 'Gambia',
        },
        {
            name: 'GE',
            value: 'Georgia',
        },
        {
            name: 'DE',
            value: 'Germany',
        },
        {
            name: 'GH',
            value: 'Ghana',
        },
        {
            name: 'GI',
            value: 'Gibraltar',
        },
        {
            name: 'GR',
            value: 'Greece',
        },
        {
            name: 'GL',
            value: 'Greenland',
        },
        {
            name: 'GD',
            value: 'Grenada',
        },
        {
            name: 'GP',
            value: 'Guadeloupe',
        },
        {
            name: 'GU',
            value: 'Guam',
        },
        {
            name: 'GT',
            value: 'Guatemala',
        },
        {
            name: 'GG',
            value: 'Guernsey',
        },
        {
            name: 'GN',
            value: 'Guinea',
        },
        {
            name: 'GW',
            value: 'Guinea-Bissau',
        },
        {
            name: 'GY',
            value: 'Guyana',
        },
        {
            name: 'HT',
            value: 'Haiti',
        },
        {
            name: 'HM',
            value: 'Heard Island And McDonald Islands',
        },
        {
            name: 'HN',
            value: 'Honduras',
        },
        {
            name: 'HK',
            value: 'Hong Kong',
        },
        {
            name: 'HU',
            value: 'Hungary',
        },
        {
            name: 'IS',
            value: 'Iceland',
        },
        {
            name: 'IN',
            value: 'India',
        },
        {
            name: 'ID',
            value: 'Indonesia',
        },
        {
            name: 'IR',
            value: 'Iran',
        },
        {
            name: 'IQ',
            value: 'Iraq',
        },
        {
            name: 'IE',
            value: 'Ireland',
        },
        {
            name: 'IM',
            value: 'Isle Of Man',
        },
        {
            name: 'IL',
            value: 'Israel',
        },
        {
            name: 'IT',
            value: 'Italy',
        },
        {
            name: 'JM',
            value: 'Jamaica',
        },
        {
            name: 'JP',
            value: 'Japan',
        },
        {
            name: 'JE',
            value: 'Jersey',
        },
        {
            name: 'JO',
            value: 'Jordan',
        },
        {
            name: 'KZ',
            value: 'Kazakhstan',
        },
        {
            name: 'KE',
            value: 'Kenya',
        },
        {
            name: 'KI',
            value: 'Kiribati',
        },
        {
            name: 'KW',
            value: 'Kuwait',
        },
        {
            name: 'KG',
            value: 'Kyrgyzstan',
        },
        {
            name: 'LA',
            value: 'Laos',
        },
        {
            name: 'LV',
            value: 'Latvia',
        },
        {
            name: 'LB',
            value: 'Lebanon',
        },
        {
            name: 'LS',
            value: 'Lesotho',
        },
        {
            name: 'LR',
            value: 'Liberia',
        },
        {
            name: 'LY',
            value: 'Libya',
        },
        {
            name: 'LI',
            value: 'Liechtenstein',
        },
        {
            name: 'LT',
            value: 'Lithuania',
        },
        {
            name: 'LU',
            value: 'Luxembourg',
        },
        {
            name: 'MO',
            value: 'Macao',
        },
        {
            name: 'MK',
            value: 'Macedonia',
        },
        {
            name: 'MG',
            value: 'Madagascar',
        },
        {
            name: 'MW',
            value: 'Malawi',
        },
        {
            name: 'MY',
            value: 'Malaysia',
        },
        {
            name: 'MV',
            value: 'Maldives',
        },
        {
            name: 'ML',
            value: 'Mali',
        },
        {
            name: 'MT',
            value: 'Malta',
        },
        {
            name: 'MH',
            value: 'Marshall Islands',
        },
        {
            name: 'MQ',
            value: 'Martinique',
        },
        {
            name: 'MR',
            value: 'Mauritania',
        },
        {
            name: 'MU',
            value: 'Mauritius',
        },
        {
            name: 'YT',
            value: 'Mayotte',
        },
        {
            name: 'MX',
            value: 'Mexico',
        },
        {
            name: 'FM',
            value: 'Micronesia',
        },
        {
            name: 'MD',
            value: 'Moldova',
        },
        {
            name: 'MC',
            value: 'Monaco',
        },
        {
            name: 'MN',
            value: 'Mongolia',
        },
        {
            name: 'ME',
            value: 'Montenegro',
        },
        {
            name: 'MS',
            value: 'Montserrat',
        },
        {
            name: 'MA',
            value: 'Morocco',
        },
        {
            name: 'MZ',
            value: 'Mozambique',
        },
        {
            name: 'MM',
            value: 'Myanmar',
        },
        {
            name: 'NA',
            value: 'Namibia',
        },
        {
            name: 'NR',
            value: 'Nauru',
        },
        {
            name: 'NP',
            value: 'Nepal',
        },
        {
            name: 'NL',
            value: 'Netherlands',
        },
        {
            name: 'NC',
            value: 'New Caledonia',
        },
        {
            name: 'NZ',
            value: 'New Zealand',
        },
        {
            name: 'NI',
            value: 'Nicaragua',
        },
        {
            name: 'NE',
            value: 'Niger',
        },
        {
            name: 'NG',
            value: 'Nigeria',
        },
        {
            name: 'NU',
            value: 'Niue',
        },
        {
            name: 'NF',
            value: 'Norfolk Island',
        },
        {
            name: 'KP',
            value: 'North Korea',
        },
        {
            name: 'MP',
            value: 'Northern Mariana Islands',
        },
        {
            name: 'NO',
            value: 'Norway',
        },
        {
            name: 'OM',
            value: 'Oman',
        },
        {
            name: 'PK',
            value: 'Pakistan',
        },
        {
            name: 'PW',
            value: 'Palau',
        },
        {
            name: 'PS',
            value: 'Palestine',
        },
        {
            name: 'PA',
            value: 'Panama',
        },
        {
            name: 'PG',
            value: 'Papua New Guinea',
        },
        {
            name: 'PY',
            value: 'Paraguay',
        },
        {
            name: 'PE',
            value: 'Peru',
        },
        {
            name: 'PH',
            value: 'Philippines',
        },
        {
            name: 'PN',
            value: 'Pitcairn',
        },
        {
            name: 'PL',
            value: 'Poland',
        },
        {
            name: 'PT',
            value: 'Portugal',
        },
        {
            name: 'PR',
            value: 'Puerto Rico',
        },
        {
            name: 'QA',
            value: 'Qatar',
        },
        {
            name: 'RE',
            value: 'Reunion',
        },
        {
            name: 'RO',
            value: 'Romania',
        },
        {
            name: 'RU',
            value: 'Russia',
        },
        {
            name: 'RW',
            value: 'Rwanda',
        },
        {
            name: 'BL',
            value: 'Saint Barthélemy',
        },
        {
            name: 'SH',
            value: 'Saint Helena',
        },
        {
            name: 'KN',
            value: 'Saint Kitts And Nevis',
        },
        {
            name: 'LC',
            value: 'Saint Lucia',
        },
        {
            name: 'MF',
            value: 'Saint Martin',
        },
        {
            name: 'PM',
            value: 'Saint Pierre And Miquelon',
        },
        {
            name: 'VC',
            value: 'Saint Vincent And The Grenadines',
        },
        {
            name: 'WS',
            value: 'Samoa',
        },
        {
            name: 'SM',
            value: 'San Marino',
        },
        {
            name: 'ST',
            value: 'Sao Tome And Principe',
        },
        {
            name: 'SA',
            value: 'Saudi Arabia',
        },
        {
            name: 'SN',
            value: 'Senegal',
        },
        {
            name: 'RS',
            value: 'Serbia',
        },
        {
            name: 'SC',
            value: 'Seychelles',
        },
        {
            name: 'SL',
            value: 'Sierra Leone',
        },
        {
            name: 'SG',
            value: 'Singapore',
        },
        {
            name: 'SX',
            value: 'Sint Maarten (Dutch part)',
        },
        {
            name: 'SK',
            value: 'Slovakia',
        },
        {
            name: 'SI',
            value: 'Slovenia',
        },
        {
            name: 'SB',
            value: 'Solomon Islands',
        },
        {
            name: 'SO',
            value: 'Somalia',
        },
        {
            name: 'ZA',
            value: 'South Africa',
        },
        {
            name: 'GS',
            value: 'South Georgia And The South Sandwich Islands',
        },
        {
            name: 'KR',
            value: 'South Korea',
        },
        {
            name: 'SS',
            value: 'South Sudan',
        },
        {
            name: 'ES',
            value: 'Spain',
        },
        {
            name: 'LK',
            value: 'Sri Lanka',
        },
        {
            name: 'SD',
            value: 'Sudan',
        },
        {
            name: 'SR',
            value: 'Suriname',
        },
        {
            name: 'SJ',
            value: 'Svalbard And Jan Mayen',
        },
        {
            name: 'SZ',
            value: 'Swaziland',
        },
        {
            name: 'SE',
            value: 'Sweden',
        },
        {
            name: 'CH',
            value: 'Switzerland',
        },
        {
            name: 'SY',
            value: 'Syria',
        },
        {
            name: 'TW',
            value: 'Taiwan',
        },
        {
            name: 'TJ',
            value: 'Tajikistan',
        },
        {
            name: 'TZ',
            value: 'Tanzania',
        },
        {
            name: 'TH',
            value: 'Thailand',
        },
        {
            name: 'CD',
            value: 'The Democratic Republic Of Congo',
        },
        {
            name: 'TL',
            value: 'Timor-Leste',
        },
        {
            name: 'TG',
            value: 'Togo',
        },
        {
            name: 'TK',
            value: 'Tokelau',
        },
        {
            name: 'TO',
            value: 'Tonga',
        },
        {
            name: 'TT',
            value: 'Trinidad and Tobago',
        },
        {
            name: 'TN',
            value: 'Tunisia',
        },
        {
            name: 'TR',
            value: 'Turkey',
        },
        {
            name: 'TM',
            value: 'Turkmenistan',
        },
        {
            name: 'TC',
            value: 'Turks And Caicos Islands',
        },
        {
            name: 'TV',
            value: 'Tuvalu',
        },
        {
            name: 'VI',
            value: 'U.S. Virgin Islands',
        },
        {
            name: 'UG',
            value: 'Uganda',
        },
        {
            name: 'UA',
            value: 'Ukraine',
        },
        {
            name: 'AE',
            value: 'United Arab Emirates',
        },
        {
            name: 'GB',
            value: 'United Kingdom',
        },
        {
            name: 'US',
            value: 'United States',
        },
        {
            name: 'UM',
            value: 'United States Minor Outlying Islands',
        },
        {
            name: 'UY',
            value: 'Uruguay',
        },
        {
            name: 'UZ',
            value: 'Uzbekistan',
        },
        {
            name: 'VU',
            value: 'Vanuatu',
        },
        {
            name: 'VA',
            value: 'Vatican',
        },
        {
            name: 'VE',
            value: 'Venezuela',
        },
        {
            name: 'VN',
            value: 'Vietnam',
        },
        {
            name: 'WF',
            value: 'Wallis And Futuna',
        },
        {
            name: 'EH',
            value: 'Western Sahara',
        },
        {
            name: 'YE',
            value: 'Yemen',
        },
        {
            name: 'ZM',
            value: 'Zambia',
        },
        {
            name: 'ZW',
            value: 'Zimbabwe',
        },
        {
            name: 'AX',
            value: 'Åland Islands',
        },
    ];

    export function truncateWithEllipses(str: string, max: number): string {
        return str.substr(0, max - 1) + (str.length > max ? '\u2026' : '');
    }
}
