# TPD Submission Tool 2.0 Release Notes

IS Team is proud to present TPD Submission Tool 2.0, which facilitate the submission to E.U..
If you are upgrading from an earlier version of TPD Submission Tool an new installation, check [TPD Submission Tool Installation & Upgrade Guide](../installation-upgrade-note.html).
For a complete installation documentation, see [Installation Guide Documentation][installation].

[installation]: ../doc/installation-guide.html

## Highlights of this release

This release covers all features targeted for 2.0. Some of the highlights include:

* offers a full integration with LADP authentication and authorization
* supports large attachment improving the memory usage and fix certain leak memory.
* Now error on upload attachment is displayed.
* All front end activity are indicating with a beautiful spinner.
* Security improvement and bug fix.

## Team

A successful project requires many people to play many roles. Some members write code or documentation, while others are valuable as testers, submitting patches and suggestions.

The team is comprised of Members and Contributors. Members have direct access to the source of a project and actively evolve the code-base. Contributors improve the project through submission of patches and suggestions to the Members.

| login   | Name                  | Email                           | Roles       |
|---------|-----------------------|---------------------------------|-------------|
| devacfr | Christophe Friederich | <christophe.friederich@pmi.com> | Developer   |
| ssiret  | Sébastien Siret       | <sebastien.siret@pmi.com>       | Contributor |

---

## 2.0.2 Changelog

(*30 August 2018*)

### Bug

* [TPD-203] - JpaAttachmentRepository.getByFilename: com.querydsl.core.NonUniqueResultException: Only one result is allowed for fetchOne calls

### New Feature

* [TPD-205] - Allow to remove unused Attachment

### Improvement

* [TPD-204] - Allow to update the file name of attachment

---

## 2.0.1 Changelog

(*26 July 2018*)

### Bug

* [TPD-196] - Exception java.net.SocketTimeoutException: Read timeout; remaining name 'DC=PMINTL,DC=NET'

---

## 2.0.0 Changelog

(*18 June 2018*)

### Bug

* [TPD-162] - Submission of new product cancelled can not resend as new product.
* [TPD-177] - Application doesn't support huge file

### Story

* [TPD-148] - Replace group/role by granted permission
* [TPD-149] - Associate external group to global permission
* [TPD-151] - Modify properties of attachment

### Task

* [TPD-130] - URL Site should refer to the latest released version
* [TPD-155] - Remove proxy used in maven and documentation
* [TPD-160] - Update Getting Started development page
* [TPD-161] - Add unit test for SubmissionService class
* [TPD-164] - Use devacfr.githib.io reflow instead
* [TPD-165] - Add 404 page

### Improvement

* [TPD-114] - Missing feedback when it is not possible to upload an attachment
* [TPD-118] - Add spinning during waiting process
* [TPD-120] - Delegating authentication to an LDAP directory
* [TPD-153] - not allow editing the password as it is stored in a read-only user directory.
* [TPD-179] - Upload distribution package in SDLC distribution repository

### Sub-task

* [TPD-98] - Add Admin part behavior testing
