# EUCEG Schema Descriptor Release Notes

## Release Notes 1.1.0

### Global Product Changes

- SameCompositionProductsExist and OtherProductsExist in Product section use BooleanNullable (no impact).
- ProductIdentification size increase to 1000 characters (no impact).
- CasNumberExists in ingredient section is optional (no impact).
- ClpWhetherClassification in ingredient section is optional (no impact).
  
### Tobacco Product Changes

- Filter in Product section uses BooleanNullable (no impact).
- TobaccoIngredients section is optional (no impact).
- HasOtherMarketData in presentation section uses BooleanNullable (no impact).
- Add new PackageType 28 -> 'Plastic Container' (no impact).
- PartType, LeafType and LeafCureMethod in ingredient section are optional (no impact).
- QuantityFluctuate and PriorityAdditive in other ingredient section use BooleanNullable (no impact).
- Laboratory in tnco section size increase to 500 characters (no impact).
- FilterVentilation, FilterDropPressureClosed and FilterDropPressureOpen in cigarette specific section are optional (no impact).

### Ecigarette Product Changes

- ProductionConformity, QualitySafety in design section use BooleanNullable (no impact).
- ChildTamperProof and HighPurity in design section are required **(business impact)**.

### Submitter Changes

- Add optional SubmitterID in submitter details section (no impact).
- Add optional HasNaturalLegalRepresentative and NaturalLegalRepresentative company in submitter section (no impact).

## Release Notes 1.0.3

### Tobacco Product Changes

- Add tobacco product type `Other`.
- Review and update of submission type description.


## Release Notes 1.0.3

### Global Product Changes

- The list of Functions in ingredient is option.


## Release Notes 1.0.2

### Global Product Changes

- Ingredient\_Name increase size to 300 characters.
- Increase field `FL number` of Ingredient to 10 characters.
- Add to `SkinSensitisationCode` enumeration value '1A' and '1B'.
- Add to `ReproductiveToxCode` enumeration value '1A\_lact', '1B\_lact' and '2\_lact'.
- Correct emission names `Formaldehyde` and `Acetyl Propionyl`

### Tobacco Product Changes

- The list of Suppliers is optional.

### Ecigarette Product Changes
-  The priority of the following fields has been changed to optional in the electronic cigarettes data dictionary.
	- Emissions
	- Design\_VoltageWattageAdjustable
	- Design\_NicotineDoseUptakeFile





## Release Notes 1.0.1

- Modifications in the `products.xsd` to correct the list of values for some CLP fields: (values in bold have been added)
	- New values for the CLP\_Acute\_Tox\_Dermal : 1,2,3,**4**
	- New values for the CLP\_Acute\_Tox\_Inhalation: 1,2,3,**4**
	- New values for the CLP\_STOT: 1,2,**3**
- Modification in the `ecig_products.xsd` to allow the ecigarette products with product type 3 or 5 or 8 or 9 to be submitted without ingredients.

## Release Notes 1.0.0
the 16.06.2016

- Initial Implementation



