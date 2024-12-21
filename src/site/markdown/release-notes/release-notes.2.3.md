# TPD Submission Tool 2.3 Release Notes

IS Team is proud to present TPD Submission Tool 2.3, which facilitate the submission to E.U..
If you are upgrading from an earlier version of TPD Submission Tool an new installation, check [TPD Submission Tool Installation & Upgrade Guide][upgrade].
For a complete installation documentation, see [Installation Guide Documentation][installation].

[upgrade]: ../installation-upgrade-note.html
[installation]: ../doc/installation-guide.html

## Highlights of this release

This maintenance release includes:

* TPD Submission Tool supports Domibus 4 backend web service.
* Introduce a persistent pre-filter search for products list.
* Bump spring-data-elasticsearch version to 3.1.0 and add possibility to use an external Elasticsearch (by configuration file only).
* Notify when certificate will expire.
* Discard the possibility to use Internet Explorer.
* Fix indexing problem of version 2.2.

{{< callout color="danger" title="Required Action" >}}

You must reindexing the search index after first start. You must have `sysadmin` role to perform this task. Go to administration part, click on `Search indexes` link and click on button `Re-Index`. Wait a while until complete and acknowledge.

{{< /callout >}}

## TPD Submission Team

A successful project requires many people to play many roles. Some members write code or documentation, while others are valuable as testers, submitting patches and suggestions.

The team is comprised of Members and Contributors. Members have direct access to the source of a project and actively evolve the code-base. Contributors improve the project through submission of patches and suggestions to the Members.

| login   | Name                  | Email                           | Roles       |
|---------|-----------------------|---------------------------------|-------------|
| devacfr | Christophe Friederich | <christophe.friederich@pmi.com> | Developer   |
| ssiret  | Sébastien Siret       | <sebastien.siret@pmi.com>       | Contributor |

### 2.3.2 Changelog
(*14 September 2020*)

#### Bug

* [TPD-263] - Spring profile is not use when sets in Windows Service script.

### 2.3.1 Changelog
(*8 September 2020*)

#### Bug

* [TPD-227] - Administration settings are empty in production.
* [TPD-225] - All ingredient suppliers are linked to all ingredients and no specific declared ingredient.

## 2.3.0 Changelog

(*03 October 2019*)

### Bug

* [TPD-222] - Migration to PostgresSql failed
* [TPD-223] - Ldap configuration failed on store
* [TPD-224] - OutOfMemoryError during indexing
