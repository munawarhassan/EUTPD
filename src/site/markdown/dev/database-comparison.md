# How create a comparison database diff file

TPD Submission Tool use a Liquibase extension lets you use your Hibernate configuration as a comparison database for diff, diffChangeLog and generateChangeLog.

the normal workflow using this extension is:

1. Edit your Hibernate mapped classes as needed (add and remove classes and attributes)
2. The application has need to be start before to create a last database version.
3. Run the following `maven` command:

```bash
$ mvn liquibase:diff -pl tpd-core -Dapp.home=C:/development/tpd-home
```

4. Check that the modified `tpt-core/target/changelog.xml` does what you expect, edit it if it does not
5. Run the following `maven`  command:

```bash
$ mvn liquibase:update -pl tpd-core -Dapp.home=C:/development/tpd-home
```

6. Repeat previous actions until complete generation

If you specify an existing changeLogFile, each run of diffChangeLog will append to the file.
