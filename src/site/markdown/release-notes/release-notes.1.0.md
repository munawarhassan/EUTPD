# TPD Submission Tool 1.0 Release Notes

IS Team is proud to present TPD Submission Tool 1.0, which facilitate the submission to E.U..

If you are upgrading from an earlier version of TPD Submission Tool an new installation, check [TPD Submission Tool Installation & Upgrade Guide](../installation-upgrade-note.html).

You **HAVE** to read this [Updgrade Note 1.0](../installation-upgrade-note.html#upgrade-note_1_0) before install this version. 

For a complete installation documentation, see [Installation Guide Documentation][installation].

## Highlights of this release

This release covers all features targeted for 1.0. Some of the highlights include:

* Import submitter, products data from Excel<i class="fa fa-trademark" aria-hidden="true"></i> sheet.
* Integration with [eDelivery][edelivery] services using [Domibus Access Point][domibus].
* Drag and drop multi-attachment files.
* User have access to all pending submission status in real time.
* User can use Windows&copy; authentification to access application.

[directive]: http://ec.europa.eu/health/tobacco/docs/dir_201440_en.pdf
[domibus]: https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus
[edelivery]: https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eDelivery
[installation]: ../doc/installation-guide.html

### TPD Submission Team

A successful project requires many people to play many roles. Some members write code or documentation, while others are valuable as testers, submitting patches and suggestions.

The team is comprised of Members and Contributors. Members have direct access to the source of a project and actively evolve the code-base. Contributors improve the project through submission of patches and suggestions to the Members.

| login   | Name                  | Email                           | Roles       |
|---------|-----------------------|---------------------------------|-------------|
| devacfr | Christophe Friederich | <christophe.friederich@pmi.com> | Developer   |
| ssiret  | Sébastien Siret       | <sebastien.siret@pmi.com>       | Contributor |

### 1.0.1 Changelog
(*29 September 2016*)

#### Bug 
* Fix initialization of PropertySet Manager.


### 1.0.0 Changelog
**(20 September 2016)**

#### Bug 

* [TPD-8] - The javascript paging of list detail doesn't work.
* [TPD-14] - Setup page doesn't work offline.
* [TPD-16] - E-cig Product Weight e-liquid missing value.
* [TPD-17] - E-Cig missing value for field Product Volume e-liquid.
* [TPD-18] - E-cig missing value in Launch Date.
* [TPD-20] - E-cig the field Mode of Sales is supposed to be an attachment.
* [TPD-21] - E-cig: the Ingredient list shows 10 Ingredient instead of 16.
* [TPD-22] - E-cig: The Ingredient Name is missing in the detailed view.
* [TPD-24] - E-cig: field Ingredient_Non_Vaporised_Status not exist in the TPD interface.
* [TPD-25] - E-cig: field Reach Registration is missing for the detailed ingredient view.
* [TPD-27] - E-cig: Emission: only 10 emissions in the UI instead of 13 in the excel.
* [TPD-28] - E-cig: Emission Other Name and CAS Number not mapped correctly.
* [TPD-29] - E-CIG: IUPAC and Quantity and Unit column wrongly mapped.
* [TPD-30] - E-cig: The field Production File from the Design section is empty.
* [TPD-31] - E-cig: the field Opening Refill File doesn't contain any attachment.
* [TPD-33] - E-cig: Ingredient the section CLP : several values not imported from excel.
* [TPD-34] - First time "Emission other name" is displayed, value disappear.
* [TPD-38] - Impossible to load a typ 8 e-Cig excel file.
* [TPD-39] - E-Cig: Product type 8 doesn't appear in the UI.
* [TPD-40] - E-cig: For 00007-16-00033, the submitter information are missiong in the UI.
* [TPD-41] - E-cig: Product Picure file attachement doesn't display in the UI.
* [TPD-42] - System to system: status of submission in pending doesn't allow to test locally the XML.
* [TPD-44] - E-cig: missing field E-Cigarette_Liquid_Volume/Capacity in the UI.
* [TPD-45] - E-Cig: The field E-Cigarette_Nicotine_Concentration is not appearing within the UI.
* [TPD-46] - E-cig: the filed: E-Cigarette_Nicotine_Dose/Uptake_File should contain an attachment.
* [TPD-47] - E-cig: Product Design section, attachement field Production File doesn't contain the right value.
* [TPD-48] - E-cig: Product Design section attachement field Opening Refill File doesn't contain the right value.
* [TPD-49] - E-cig: Product Design section: the field E-Cigarette_Coil_Resistance is not existing within the UI.
* [TPD-50] - Confidential attributes: conditional rule.
* [TPD-53] - Error rendering in Ecigarette Cpl ingredient form.
* [TPD-55] - Remove carriage return in address of company in submitter details.
* [TPD-56] - Submitter can not be set on new product.
* [TPD-57] - E-cig: the boolean BrandSubtypeNameExist boolean is not imported.
* [TPD-69] - The field "Ingredient_CAS_Additional " from excel file is not according to the xsd validation.
* [TPD-70] - Update user failed.
* [TPD-71] - Submission ordering.
* [TPD-76] - missing values with User Interface.
* [TPD-77] - XSD Validation: not possible to see the errors.

#### New Feature

* [TPD-15] - Add Validation of Submission.
* [TPD-35] - Hidden UI Form until complete download of data.
* [TPD-43] - Force confidential field for e-cig.
* [TPD-52] - Default confidential in UI for e-cig.

#### Story

* [TPD-60] - Split the Submission and product management.

#### Task

* [TPD-3] - Configuration automatic deployment.

#### Improvement

* [TPD-1] - Refactoring AttachmentResource Class.
* [TPD-4] - Refactoring Import Excel files.
* [TPD-5] - Create specific backend module.
* [TPD-7] - Reduce the download size of list request in frontend.
* [TPD-13] - Fail deadly on encryption error.
* [TPD-19] - E-cig Boolean for Withdrawal, can be calculated.
* [TPD-23] - E-Cig: The field Identification of Refill Container / Cartridge should be add as a column in the Ingredient general view.
* [TPD-26] - UI: Some drop down list can not display the full entered value due to a limited sized of the displayed valued.
* [TPD-36] - Reduce the number of Threads.
* [TPD-37] - Exclude angular templates from explorer cache.
* [TPD-51] - Addition of features: sorting and filtering.
* [TPD-62] - Add system-to-system settings.
* [TPD-72] - Unsynchronize the product and associated submission in REST call.



