# Contribution guidelines

The purpose of this section is to provide a introduction to the process of contributing in project. You must familiar with the standard development workflow.

## Prepare Your Commit

1. Find a JIRA issue that is currently unassigned that you want to work on at JIRA issue tracker, or create your own (you’ll need a JIRA account for this, see above)!
    1. This could be a JIRA representing a bug (possibly a bug that you encountered and reported, e.g. when trying to build) or a new feature.
    2. When identifying a JIRA issue to work on, it is recommended to work on items that are relevant to the next release. Selecting work items important for the next release increases the priority for reviewers during the contribution process. See the tracking ticket for the release to figure out the high priority projects or ask the release manager to guide you.
2. Assign the JIRA issue to yourself.
3. Formulate a plan for resolving the issue. Guidelines to consider when designing a solution can be found in the Code Reviewing document. It is important to discuss your proposed solution within the JIRA ticket early in the resolution process in order to get feedback from reviewers. Early discussions will help:
    1. ensure the solution will be scoped in a consumable fashion;
    2. eliminate duplicate work with other contributions; and
    3. alert anyone interested in following the activity and progress of the ticket.
4. Create the branch associate to JIRA issue.

### Use real name in git commits

Please configure git to use your real first and last name for any commits you
intend to submit as pull requests. For example, this is not acceptable:

    Author: Nickname <user@mail.com>

Rather, please include your first and last name, properly capitalized:

    Author: First Last <user@mail.com>

This helps ensure traceability and also goes a long way to
ensuring useful output from tools like `git shortlog` and others.

You can configure this via the account admin area in Bitbucket (useful for
fork-and-edit cases); _globally_ on your machine with

    git config --global user.name "First Last"
    git config --global user.email user@mail.com

or _locally_ for the `tpd` repository only by omitting the
'--global' flag:

    cd tpd
    git config user.name "First Last"
    git config user.email user@mail.com

### Submit JUnit unit, functional and integration test cases for all behavior changes

Search the codebase to find related tests and add additional `@Test` methods
as appropriate. It is also acceptable to submit test cases on a per JIRA issue
basis, for example:

```java
package org.springframework.beans.factory.support;


@Configuration
@ContextConfiguration(classes = { SubmissionServiceIT.class })
public class SubmissionServiceIT extends BaseStoreTestIT  {

    /**
     * Can not update attachment with same name but with different case.
     *
     * @throws IOException if write file failed
     * @throws CorrurencyAttachmentAccessException if trying update of an attachment that it is sending.
     * @see TPD-145
     */
    @Test
    public void shouldFilenameAttachmentCaseSensitive() throws IOException, CorrurencyAttachmentAccessException {
		...
    }
}
```

This is applicable on unit and integration test. For functional, you must create a new BDD scenario in the appropriate feature file and annotate it with `@issue` or `@issues` annotation like following example:

```Gherkin
@issue:TPD-145
Scenario: Upload attachement with same file name but with different case sensitive 
    Given the attachment file "attachment.pdf" exists 
    When upload file "case-sensitive/Attachment.pdf" 
    Then ensure attachment file "Attachment.pdf" is new 
```

This allows to link the scenario in Cucumber functional report to JIRA issue.

## Format commit messages

Please read and follow the [Commit Guidelines section of Pro Git][git-commit-guideline].

Most importantly, please format your commit messages in the following way:

    Short (50 chars or less) summary of changes

    More detailed explanatory text, if necessary. Wrap it to about 72
    characters or so. In some contexts, the first line is treated as the
    subject of an email and the rest of the text as the body. The blank
    line separating the summary from the body is critical (unless you omit
    the body entirely); tools like rebase can get confused if you run the
    two together.

    Further paragraphs come after blank lines.

     - Bullet points are okay, too

     - Typically a hyphen or asterisk is used for the bullet, preceded by a
       single space, with blank lines in between, but conventions vary here

    Issue: TPD-1234, TPD-1235

1. Use imperative statements in the subject line, e.g. "Fix broken Javadoc link".
1. Begin the subject line with a capitalized verb, e.g. "Add, Prune, Fix,
    Introduce, Avoid, etc."
1. Do not end the subject line with a period.
1. Restrict the subject line to 50 characters or less if possible.
1. Wrap lines in the body at 72 characters or less.
1. Mention associated JIRA issue(s) at the end of the commit comment, prefixed
    with "Issue: " as above.
1. In the body of the commit message, explain how things worked before this
    commit, what has changed, and how things work now.

[git-commit-guideline]: https://git-scm.com/book/en/v2/Distributed-Git-Contributing-to-a-Project#Commit-Guidelines

## Submit your pull request

1. You’re ready to submit your pull-request for review!
    1. Run all tests prior to submission. See the [Building from Source][build-from-source] section for instructions. Make sure that all tests pass prior to submitting your pull request.
    2. Log in on Bitbucket, go to the project, select Pull Requests section and click on Create pull request button.
    3. Add your shepherd in the “Reviewers” section. You should also include other members who have contributed to the discussion of your proposed change.
    4. Under “Description” in addition to details about your changes (follow the how submit your pull request document).
2. Wait for a code review from another developer via review page of pull request, address their feedback and upload updated patches until you receive a “Approval” from a committer.
    1. If you don’t receive any feedback, contact your shepherd to remind them. While the committers try their best to provide prompt feedback on proposed changes, they are busy and sometimes a patch gets overlooked.
    2. When addressing feedback, adjust your existing commit(s) instead of creating new commits (git rebase -i is your friend).
    3. Review page of pull request comments should be used for code-specific discussions, and JIRA comments for bigger-picture design discussions.
    4. Always respond to each RB comment that you address directly (i.e. each comment can be responded to directly) with either “Done.” or a comment explaining how you addressed it.
    5. If an issue has been raised in the review, please resolve the issue as “Fixed” or “Dropped”. If “Dropped” please add a comment explaining the reason. Also, if your fix warrants a comment (e.g., fixed differently than suggested by the reviewer) please add a comment.
3. After consensus is reached on your JIRA/patch, you’re review request will receive a “Approved!” from a committer, and then a committer will commit your patch to the git repository.
4. The last step is to ensure that the necessary documentation gets created or updated so the whole world knows about your new feature or bug fix.

Subject line:

Follow the same conventions for pull request subject lines as mentioned above
for commit message subject lines.

In the body:

1. Explain your use case. What led you to submit this change? Why were existing
    mechanisms in the framework insufficient? Make a case that this is a
    general-purpose problem and that yours is a general-purpose solution, etc.
1. Add any additional information and ask questions; start a conversation or
    continue one from JIRA.
1. Mention the JIRA issue ID.

Note that for pull requests containing a single commit, BitBucket will default the
subject line and body of the pull request to match the subject line and body of
the commit message. This is fine, but please also include the items above in the
body of the request.

[build-from-source]: dev-documentation.html#dev-build-from-source
