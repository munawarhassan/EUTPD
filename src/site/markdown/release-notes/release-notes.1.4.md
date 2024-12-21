# TPD Submission Tool 1.4 Release Notes

IS Team is proud to present TPD Submission Tool 1.4, which facilitate the submission to E.U..
If you are upgrading from an earlier version of TPD Submission Tool an new installation, check [TPD Submission Tool Installation & Upgrade Guide](../installation-upgrade-note.html).
For a complete installation documentation, see [Installation Guide Documentation][installation].

You **HAVE** to read this [Updgrade Note 1.4](../installation-upgrade-note.html#upgrade-note_1_4) before install this version. 

[installation]: ../doc/installation-guide.html

### Highlights of this release

This release focus on memory improvement.

* Add entity indexing to reduce the TTFB on pageable requests.
* The JRE 1.8 is the minimum required.
* Fix problem of Spring inference on destroy method.
* Optimize javascript code.
* Fix memory leak.
	

### TPD Submission Team

A successful project requires many people to play many roles. Some members write code or documentation, while others are valuable as testers, submitting patches and suggestions.

The team is comprised of Members and Contributors. Members have direct access to the source of a project and actively evolve the code-base. Contributors improve the project through submission of patches and suggestions to the Members.

| login | Name | Email | Roles |
|-------|------|-------|-------|
| devacfr | Christophe Friederich | <christophe.friederich@pmi.com> | Developer |
| ssiret | Sébastien Siret | <sebastien.siret@pmi.com> | Contributor |

### 1.4.0 Changelog
(*7 December 2016*)

#### Bug

* [TPD-122] - Search field in Attachment section doesn't work anymore

#### Task

* [TPD-117] - Improve memory usage
* [TPD-119] - Ordering column in list details with displayed values.




