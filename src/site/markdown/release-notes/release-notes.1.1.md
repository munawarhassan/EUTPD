# TPD Submission Tool 1.1 Release Notes

IS Team is proud to present TPD Submission Tool 1.1, which facilitate the submission to E.U..

If you are upgrading from an earlier version of TPD Submission Tool an new installation, check [TPD Submission Tool Installation & Upgrade Guide](../installation-upgrade-note.html).

For a complete installation documentation, see [Installation Guide Documentation][installation].

[installation]: ../doc/installation-guide.html

### Highlights of this release

This release covers all features targeted for 1.1. Some of the highlights include:

* Bump **EUCEG** Schema to version 1.0.2.
    - Ingredient\_Name increase size to 300 characters.
    - Increase field `FL_number` of Ingredient to 10 characters.
    - Add to `SkinSensitisationCode` enumeration value '1A' and '1B'.
    - Add to `ReproductiveToxCode` enumeration value '1A\_lact', '1B\_lact' and '2\_lact'.
    - Correct emission names `Formaldehyde` and `Acetyl Propionyl`
* Allow multiple attachments for the fields Product\_Technical\_File and Novel\_Study
* Imported products will be only updated whether they are changed compared to previous import.
* Now you can see the acknowledge receipt detail of the submission.
	
	![submission view feature](submission-view-feature.png)
	


### TPD Submission Team

A successful project requires many people to play many roles. Some members write code or documentation, while others are valuable as testers, submitting patches and suggestions.

The team is comprised of Members and Contributors. Members have direct access to the source of a project and actively evolve the code-base. Contributors improve the project through submission of patches and suggestions to the Members.

| login | Name | Email | Roles |
|-------|------|-------|-------|
| devacfr | Christophe Friederich | <christophe.friederich@pmi.com> | Developer |
| ssiret | Sébastien Siret | <sebastien.siret@pmi.com> | Contributor |

### 1.1.2 Changelog
(*19 October 2016*)

#### Bug

* [TPD-100] - The product validation action failed when TNCO Lab info exists.
* [TPD-101] - Update only Product that changed in import excel file.
* [TPD-102] - The field Product_Length must be confidential by default.

### 1.1.1 Changelog
(*13 October 2016*)

#### Bug

* [TPD-99] -Tobacco Product: Emission data are not imported from the excel and not created in the xml.

### 1.1.0 Changelog
(*12 October 2016*)

#### New Feature

* [TPD-74] -  Add submission receipt view.
* [TPD-88] -  Allow multiple attachment for the attributes Product\_Technical_File and Novel\_Study.

#### Task

* [TPD-86] - Add liquibase test context in qa environment.
* [TPD-90] - Bump EUCEG Schema to version 1.0.2.

#### Improvement

* [TPD-61] - The display of filename of attachment is too short in upload dialog.
* [TPD-72] - Create simple HATEOAS.
* [TPD-81] - Add BBD functional test.
* [TPD-85] - Update only changed products in import excel file.
* [TPD-95] - Replace the flag sent on attachment as a status.



