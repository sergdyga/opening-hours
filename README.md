# Opening Hours

### Description

Service accepts JSON-formatted opening hours as an input and returns the rendered human-readable format as output.

### Endpoints Overview

Swagger: http://localhost:8080/swagger-ui/

There are 2 versions of OpeningHours Controller. Each version accepts data in different format.

* __POST__ `v1/formatHours` - with separate key for each day of week.
`value` - stands for seconds from beginning *of the day*
  - Order of input hours does not affect result
  - Days of week names are case-insensitive
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
* __POST__ `v2/formatHours` - is proposed new format. Here whole week 
  is represented as flat list of `from` - `to` pairs (e.g. open - close).
  _Motivation_: flat structures are easier to validate and use (for example as SQL input data).
    - Order of input hours does not affect result
    - Maximum value is _691200_ which is seconds from beginning *of the week* plus one day.
      Additional _MONDAY_ seconds is used for late _SUNDAY_ closing hours.
```json
[
  {
    "from": 32400,
    "to": 72000
  },
  {
    "from": 432000,
    "to": 518399
  },
  {
    "from": 518400,
    "to": 604799
  }
]
```
* __POST__ `v1/convertToWeeklyFormat` - accepts hours in _v1_ input format 
  and return them in _v2_ input format. This response can then be sent to `v2/formatHours`

### Running Locally
* With maven
```
mvn spring-boot:run -Dspring.profiles.active=default
```
* Compile and run jar
```
mvn clean install
```
```
java -Dspring.profiles.active=local -jar opening-hours-ws/target/opening-hours-ws-0.0.1-SNAPSHOT.jar
```

### Testing
* All tests:
  ```mvn test```
* Only unit tests:
  ```mvn test -P skipItTest```
* Only integration tests:
  ```mvn test -P onlyItTest```