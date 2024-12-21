# TPD Submission Tool 2.4 Release Notes

{{< layout class="row" >}}

{{< column class="align-self-center" >}}

<h2 class="text-black-50 no-anchor">13 November 2020</h2>

IS Team is proud to present TPD Submission Tool 2.4, which facilitate the submission to E.U..
If you are upgrading from an earlier version of TPD Submission Tool an new installation, check [TPD Submission Tool Installation & Upgrade Guide][upgrade].
For a complete installation documentation, see [Installation Guide Documentation][installation].

<h2 class="text-black-50 no-anchor">Highlights of this release</h2>

This maintenance release includes:

* New User Interface
* Products, Attachments and Submitters Revisions
* Administration Audit Log
* Access Audit Log
* Easy Installation on Windows

{{< /column >}}

{{< column >}}

![Release Notes](../images/release.svg)

{{< /column >}}

{{< /layout >}}

## New User Interface

This release introduce a new visual layout while keeping existing functionality.

![Welcome](../images/release-notes/2.4/image2020-11-11_12-27-36.png)

## Products, Attachments and Submitters Revisions

TPD Submission Tool now keep track of changes performed on products, submitters and attachments records.

### Product Revision

TPD Submission Tool allows to view and compare the different revisions of a product.

![Revision](../images/release-notes/2.4/image2020-10-13_15-51-48.png)

### Administration Audit Log

TPD Submission Tool keep track on settings changes. It is now possible to view log of changes perfomed on th application settings.

TPD Submission Tool also logs all account actions.

![Admin Log](../images/release-notes/2.4/image2020-10-13_16-20-53.png)

### Access Audit Log

TPD Submission Tool tracks all authentification actions.

![Access Log](../images/release-notes/2.4/image2020-10-13_16-31-38.png)


[upgrade]: ../installation-upgrade-note.html
[installation]: ../doc/installation-guide.html

### Easy Installation On Windows

Java JDK is now included in TPD Submission Tool Windows x64 distribution.

## TPD Submission Team

A successful project requires many people to play many roles. Some members write code or documentation, while others are valuable as testers, submitting patches and suggestions.

The team is comprised of Members and Contributors. Members have direct access to the source of a project and actively evolve the code-base. Contributors improve the project through submission of patches and suggestions to the Members.

| login      | Name                  | Email                               | Roles       |
|------------|-----------------------|-------------------------------------|-------------|
| devacfr    | Christophe Friederich | <christophe.friederich@pmi.com>     | Developer   |
| jjuillerat | Joel Juillerat        | <joel.juillerat@contracted.pmi.com> | Developer   |
| ssiret     | Sébastien Siret       | <sebastien.siret@pmi.com>           | Contributor |

## 2.4.3 Changelog

* [TPD-276] - E-cigarette ingredient section failed when AdditionalCasNumbers field is empty.
* [TPD-277] - E-cigarette product design section failed when VoltageWattageAdjustable field is empty.

## 2.4.2 Changelog

* [TPD-274] - Missing hibernate_sequence sequence declaration in Liquibase for Envers.

## 2.4.1 Changelog

* [TPD-273] - Submitter ID of Manufacturer and Supplier is not managed as expected during import.
  
## 2.4.0 Changelog

###  Improvement

* [TPD-226] - Update to jdk 9 or more
* [TPD-229] - Bump pageobjects version to 3.1.0
* [TPD-230] - Bump Angular version to 9
* [TPD-242] - Associate the excel file used during import to product
* [TPD-243] - Add audit fields of submission in submission view
* [TPD-244] - Compare imported products with existing product during import

###  Task

* [TPD-245] - update user documentation concerning the imported product.
* [TPD-257] - Remove old angular 1 frontend
* [TPD-260] - Implements Bulk send submission
* [TPD-268] - Verify application upgrade from 2.3 to 2.4
* [TPD-269] - Update Tomcat bootstrap java version check to jre 13

###  Bug

* [TPD-225] - All Ingredient Suppliers are linked to all ingredients and no specific declared ingredient
* [TPD-227] - Administration settings are empty in production
* [TPD-249] - Confidential flag doesn't display effective value in attachment list
* [TPD-253] - Internal Error when type wrong password in import keypair certificate
* [TPD-258] - Validation KO
* [TPD-259] - Sort icons not correct
* [TPD-261] - Admin log - add global permission to user KO
* [TPD-267] - Do not see old setting value in admin log when update value.
* [TPD-270] - app-confidential-date component doesn't work
* [TPD-271] - incorrect values in app-confidential-select
* [TPD-272] - try fix CI error: java.lang.NoSuchMethodError: 'void org.codehaus.janino.ClassBodyEvaluator.setImplementedInterfaces(java.lang.Class[])'

###  Sub-task

* [TPD-231] - create specific checkbox element see ElementUtils.makeCheckboxClikable
* [TPD-232] - update setup part
* [TPD-233] - add resetpassword page
* [TPD-234] - replace directive submissionStatus with submission status component containing progress bar (see SubmissionsComponent)
* [TPD-235] - add global config readonly flag to set product part as readonly
* [TPD-236] - set css table in product detail-> (add selected item, td vertical-align:middle,...)
* [TPD-237] - verify profile when user is readonly
* [TPD-238] - verify all fields for ecig and tobacco products
* [TPD-239] -  split product module to module -> partial, ecig, tobacco
* [TPD-240] - fix global progress bar of attachment dropzone
* [TPD-241] - add current version and current profile to home page and footer
* [TPD-251] - Add create external group
* [TPD-252] - Verify update ldap settings

###  Story

* [TPD-248] - Report of product revisions
* [TPD-250] - Track all high priority admin actions
* [TPD-254] - Track user authentication access
* [TPD-256] - Report of submitter revisions
