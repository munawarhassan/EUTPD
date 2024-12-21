# TPD Submission Tool 2.2 Release Notes

IS Team is proud to present TPD Submission Tool 2.2, which facilitate the submission to E.U..
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

## 2.2.0 Changelog

(*09 September 2019*)

### Bug

* [TPD-218] - TPD Pagination Issue

### Improvement

* [TPD-152] - Notify when certificate will expire
* [TPD-178] - Bump spring-data-elasticsearch version to 3.1.0
* [TPD-181] - Display a sorry page when use internet explorer
* [TPD-216] - Add possibility to use external elasticsearch
* [TPD-217] - Add filtering on list view

### Task

* [TPD-211] - Update JIRA URL and issue collector ids
* [TPD-212] - Restore help link
* [TPD-213] - Update serenity report with new JIRA url
