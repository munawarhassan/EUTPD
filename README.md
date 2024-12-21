# TPD Submission

EU Common Entry Gate for reporting of information on tobacco products and notification of information on electronic cigarettes and refill containers.

an IT tool designed to ensure uniform application of the reporting and notification obligations, harmonise the submission of data, facilitate comparison and reduce administrative burden for regulators and industry stakeholders alike.



## Build

This project use [maven](https://maven.apache.org) to build distribution package, war file, site documentation and documents.


### build with maven wrapper

The [Maven Wrapper](https://github.com/takari/maven-wrapper) is an easy way to ensure a  Maven build has everything necessary to run.

```bash
$ ./mvnw clean package -PskipTests,npm-default  -Djavadoc.skip=true
```

to update execute following command:

```bash
$ mvn wrapper:wrapper
```

## Create Distribution

To create distribution packages, you can execute following command:

```bash
$ mvn clean package -P full
```

This command uses `full` profile to include distribution modules.

>Before create first time the distribution, you should install third party dependencies to executing following command:
>
>```bash
>$ ./script/install-thirdparty.sh
>```
>

