

# start selenium env
./mvnw docker:build docker:start -Pfunctional-test,docker -pl tpd-test/func-test

# stop selenium env
./mvnw docker:stop -Pfunctional-test,docker -pl tpd-test/func-test

# functional test wiht reporting
# ./mvnw clean install -PskipTests -D=maven.javadoc.skip
# ./mvnw clean verify -Pfunctional-test,docker,reporting -pl tpd-test/pageobjects,tpd-test/func-test -Dserenity.take.screenshots=FOR_EACH_ACTION -Dbrowser=chrome