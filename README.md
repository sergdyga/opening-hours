# Opening Hours

### Description

Service accepts JSON-formatted opening hours as an input and returns the rendered human-readable format as output.

### Endpoints Overview

Swagger: http://localhost:8080/swagger-ui/

* __POST__ `v1/formatHours` - accepts map with separate key for each day of the week.
  `value` - stands for seconds from beginning *of the day*
    - Order of input hours does not affect result
    - Days of week names are case-insensitive
    - Missing or empty `[]` day of the week means - closed

Example:

```json
{
  "sunday": [],
  "monday": [
    {
      "type": "open",
      "value": 32400
    },
    {
      "type": "close",
      "value": 72000
    }
  ]
}
```

### Running Locally

* Use existing Docker image

**The fastest solution 🚀** _You need Docker_

```shell
docker-compose up
```

* Build and run image locally

_**Precondition**: You need Docker_

```shell
docker-compose up -f docker-compose-local.yaml
```

* Run using Maven

_**Precondition**: You need Java 11 and Maven installed_

```shell
mvn spring-boot:run -Dspring.profiles.active=default
```

* Compile and run JAR

```shell
mvn clean install
```

```shell
java -Dspring.profiles.active=local -jar opening-hours-ws/target/opening-hours-ws-0.0.1-SNAPSHOT.jar
```