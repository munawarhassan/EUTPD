# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [3.0.0][3.0.0]

### Added

- Add Domibus message logs and error logs associated to submission receipts.
- Add submission tracking file reports.
- Add Bulk create/send submission.
- Add negative filters feature.
- Add statistic on product,attachment and submission.

### Changed

- Update Administration Domibus configuration, include jms configuration, health check and additional AS4 properties.
- Implementation of jms domibus backend
- add Docker domibus test environment.
- Refactoring ws domibus backend
- Add self-signed certificate generator for test
- Bump ASPayload version to 1.2
- Submission stores the PIR status of sent product.

### Dependencies

- Upgraded Angular version to 15.2.1 (up from 12.2.0)
- Update backend.wsdl with version of Domibus 4.2.1
- Bump tomcat version to 9.0.68 from 9.0.46
- Upgrade java JDK version to 11.0.16.1_1 from 11.0.11_9
- Bump log4j version to 2.19.0 from 2.14.1
- Bump swagger version to 2.2.4 from 1.6.2
- Bump querydsl version to 5.5.5 from 4.4.0
- Bump commons-text version to 1.10.0 from 1.9

## [2.5.3][2.5.3]

### Changed

- Upgrade java JDK version to 11.0.16.1_1 from 11.0.11_9
- Bump tomcat version to 9.0.68 from 9.0.46
- Bump log4j version to 2.19.0 from 2.14.1


## [2.5.2][2.5.2]

### Changed

- Bump ASPayload version to 1.2 from 1.1.

### Fixed

- Fix NPE, This error occurs when any presentation is associated to one product.
- Fix IllegalArgumentException, this error occurs when last update exists and set DateTime with Optional value returned by function preference.getDate() rather than using the date.
- Fix old date strike in daterangepicker for audit view.
- Display message error on duplicate key during import

## [2.5.1][2.5.1]

### Changed

- Bump mvn wrapper version to 3.8.6 from 3.6.3

## [2.5.0][2.5.0]

### Added

- Add Product Information Reporting (PIR) status indicating the lifecycle state of product.
- Submissions not yet sent are automatically updated with updated product data.
- Generate zip package containing the submission and all associated attachments.
- Add ordering in Hateoas request.
- Add Tobbaco and Ecig products Excel file export feature. In global view, export Excel file is a bulk operation in a bulk wizard.
  - refactoring Excel mapping part.
  - include XPath in mapping description.
  - add excel generator class using product mapping description.
  
### Changed

- Add possibility to add and update sale data manually through front end.
- Allows to keep up to date sales data history for product import.
- Update the AS4Payload schema with the release 1.1.0
- Each property in filtering accepts one or more inclusive term values.
- Send Submitter during submission only after change. The validity of submitter is also check before send.
  - Add submitter status
  - Update submitter index to v2 (need reindex application)
- Package is available for Windows x64, Linux x64 (`glibc version 2.12 or higher`) and Macos x64. The distribution for Windows x86 has been removed.
- Replace EhCache implementation by JCache
- All Toxicological Attachments have to accept the multi file import.

### Fixed

- Fixes other emission UI section of tobacco product failed when CasNumber field is null.
- Fixes use preferred submission type during import (used to pre-select the submission type in product send view).
- Fixes indexing of submission status (Update submitter index to v1).
- Fixes sorting of columns in submission history.
- Fixes slug user in rename username feature.
- Fix build date is missing on home page, after update of git-commit-id-plugin plugin.
- Fix update certificate
  - Missing check of existing of certificate
  - Add `app.security.keystore.defaultLocation` property to split default keystore and storage location of persistent keystore.
- Fix the business error ERR-RULES-0008-0027: The value of the submitter ID of the product ID (5 first digits of the product ID) is not correct because it doesn't correspond to the value of the Submitter ID.
- Fix Arbitrary file upload leading to Stored Cross-Site Scripting.
- Fix all clear text password in the server response.
- Fix Withdrawal date conversion.
- Fix update Configuration data. `PropertySource` should be enumerable to retrieve all nested property.
- Fix update ldap configuration.
  
### Refactoring

- Add module tpd-testing-junit4 and tpd-testing-junit5 to migrate easier.
- Add module tpd-keystore and move all concerning it from tpd-core.
- Move domibus backend WS in tpd-backend-core module.
- Move euceg excel import/export in tpd-euceg-core module.
- Move cryptography in tpd-api module.
- Upgrade all tests with Junit 5.

### Docker

- Split frontend server and backend serve in distinct docker container.
- Add a server Using Express and http-proxy-middleware to proxify call to backend, compress http requests, CORS enabling,...
- Add DejaVue application for elasticsearch development.
- Add Domibus 4.2.2 test environment.

### Dependencies

- Downgrade Jdk version to 11.0.11 to use AdoptOpenJdk **LTS** version (for compatibility of Docker Tomcat container and LTS support).
- Bump Tomcat version to 9.0.46 to fix NPE.
- Add jxpath 1.3 dependency
- Bump poi version to 5.0.0
- Bump jackson version to 2.11.4
- Bump swagger version to 1.6.2 (for compatibility xml-api dependency)
- Bump nakeyaml version to 1.26
- Bump Liquibase version to 4.3.5 from 3.8.9
- Bump git-commit-id-plugin version to 4.0.4
- Bump atlassian-cache version to 5.4.2
- Bump Hibernate version to 5.4.31.Final
- Bump SpringFramework version to 5.3.6 from 5.2.2.RELEASE
- Bump Spring Data version to 2.5.0 from 2.2.4.RELEASE
- Bump Spring Data ElasticSearch to 4.2.0 from 3.2.3.RELEASE
- Bump ElasticSearch to 7.12.1 from 6.8.4
- Remove usertype-core library (use JPA converter instead)
- Bump EhCache version to 3.4.0

## [2.4.4][2.4.4]

**8 June 2021**
This release is a hot fix.

### Fixed

- Fix error when submission type missing for product.
- sets submission type for update.
- removes IllegalArgumentException exception on convert integer to SubmissionTypeEnum.
- sets submission type to 1 if missing in send product view.

## [2.4.3][2.4.3]

**17 March 2021**
This release is a hot fix.

### Fixed

- E-cigarette ingredient section failed when AdditionalCasNumbers field is null.
- E-cigarette product design section failed when VoltageWattageAdjustable field is null.

## [2.4.2][2.4.2]

**26 February 2021**
This release is a hot fix.

### Fixed

- Add missing 'hibernate_sequence' sequence for Envers and fix compatibility with PostgreSQL.

## [2.4.1][2.4.1]

**23 February 2021**
This release is a hot fix.

### Fixed

- Submitter ID of Manufacturer and Supplier is not managed as expected during import.

## [2.4.0][2.4.0]

**13 Junary 2021**
###  Highlight

- Report of product revisions
- Track all high priority admin actions
- Track user authentication access
- Report of submitter revisions

### Fixed

- All Ingredient Suppliers are linked to all ingredients and no specific declared ingredient
- Administration settings are empty when application run in production

### Dependencies

- Update to jdk 13
- Bump pageobjects version to 3.1.0
- Bump Angular version to 9

### Added

- Add Field to associate the excel file used during import to product
- Add audit fields of submission in submission view

### Changed

- Compare imported products with existing product during import
- Update Tomcat bootstrap java version check to jre 13
- update user documentation concerning the imported product.

### Removed

- Remove old angular 1 frontend

## [2.3.2][2.3.2]

### Fixed

- Spring profile is not use when sets int Windows Service script.

## [2.3.1][2.3.1]

### Fixed

- Administration settings are empty in production.
- All ingredient suppliers are linked to all ingredients and no specific declared ingredient.

## [2.3.0][2.3.0]

### Fixed

- Migration to PostgresSql failed
- Ldap configuration failed on store
- OutOfMemoryError during indexing

## [2.2.0][2.2.0]

### Fixed

- TPD Pagination Issue

### Added

- Notify when certificate will expire
- Display a sorry page when use internet explorer
- Add filtering on list view

### Dependencies

- Bump spring-data-elasticsearch version to 3.1.0

### Changed

- Add possibility to use external elasticsearch

## [2.1.1][2.1.1]

### Fixed

- a error message is not returned when a jax-rs resource return 404 error.
- Unexpected error when try upload wrong file

### Changed

- Use retrieveMessage call instead use downloadMessage
- Replace ViewLastLog by jax-rs method
- Add authentication on web service backend of Domibus
- Limit access to update of attachment  to Admin role
- The user error message is not explicit when try to convert a empty string to number

### Added

- Add Bug Report Analytic Tools

[3.0.0]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v3.0.0..v2.5.0
[2.5.3]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.5.3..v2.5.0
[2.5.2]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.5.2..v2.5.0
[2.5.1]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.5.1..v2.5.0
[2.5.0]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.5.0..v2.4.0
[2.4.4]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.4.3..v2.4.4
[2.4.3]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.4.2..v2.4.3
[2.4.2]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.4.1..v2.4.2
[2.4.1]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.4.1..v2.4.0
[2.4.0]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.4.0..v2.3.0
[2.3.2]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.3.2..v2.3.1
[2.3.1]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.3.1..v2.3.0
[2.3.0]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.3.0..v2.2.0
[2.2.0]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.2.0..v2.1.0
[2.1.1]:  https://rd-bitbucket.app.pmi/projects/TPD/compare/v2.1.1..v2.1.0
