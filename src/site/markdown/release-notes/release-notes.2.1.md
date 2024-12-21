# TPD Submission Tool 2.1 Release Notes

IS Team is proud to present TPD Submission Tool 2.1, which facilitate the submission to E.U..
If you are upgrading from an earlier version of TPD Submission Tool an new installation, check [TPD Submission Tool Installation & Upgrade Guide][upgrade].
For a complete installation documentation, see [Installation Guide Documentation][installation].

[upgrade]: ../installation-upgrade-note.html
[installation]: ../doc/installation-guide.html

## Highlights of this release

This maintenance release includes:

* enforcing of security on Domibus backend service.
* new functionality to facilitate the user feedback and bug reporting using JIRA Issue Collector.
* a refactoring of frontend and upgrade of Angularjs version to 1.7.2
* fix minor bugs

### You’ll feel more connected

Now you can help us to improve TPD Submission Tool. This feature use JIRA Issue Collector to notify automatically TPD Submission Team.

**To access user feedback**

1. Click on <var class="fa fa-commenting-o fa-2x" aria-hidden="true"></var> button on top right in header.
2. Fill the form and click on **Submit** button.

![User Feedback Form](../images/user-feedback-form.png)

### Bug Reporting

To reduce the resolution time when an error occurs, you can send the report now directly for checking and possible resolution of the problem. Automatically, the description is prefilled with the exception and java stackstrace.

![Bug Report Form](../images/bug-report-form.png)

### Domibus More secure

Now, The Domimus backend  service **must** be configure with a specific credential.

{{< callout color="info" title="Note" >}}

The Domibus credential must be created manually with password encrypted with sha256, `ADMIN` role and stored in table `TB_AUTHENTICATION_ENTRY`. See with your administrator for more information.

{{< /callout >}}

## Team

A successful project requires many people to play many roles. Some members write code or documentation, while others are valuable as testers, submitting patches and suggestions.

The team is comprised of Members and Contributors. Members have direct access to the source of a project and actively evolve the code-base. Contributors improve the project through submission of patches and suggestions to the Members.

| login   | Name                  | Email                           | Roles       |
|---------|-----------------------|---------------------------------|-------------|
| devacfr | Christophe Friederich | <christophe.friederich@pmi.com> | Developer   |
| ssiret  | Sébastien Siret       | <sebastien.siret@pmi.com>       | Contributor |

## 2.1.0 Changelog

(*13 September 2018*)

### Bug

* [TPD-183] - a error message is not returned when a jax-rs resource return 404 error.
* [TPD-195] - Unexpected error when try upload wrong file

### New Feature

* [TPD-182] - Add Jira Feedback

### Task

    * [TPD-202] - Use retrieveMessage call instead use downloadMessage

### Improvement

* [TPD-186] - Replace ViewLastLog by jax-rs method
* [TPD-188] - Add Bug Report Analytic Tools
* [TPD-200] - Add authentication on web service backend of Domibus
* [TPD-206] - Limit access to update of attachment  to Admin role
* [TPD-209] - The user error message is not explicit when try to convert a empty string to number
