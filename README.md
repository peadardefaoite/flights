# Flight Interconnections RESTful Microservice using Spring Boot

This repo contains a test microservice for finding interconnections between two given airports and a time-frame with a maximum of 1 stop. 
In case of a journey with a stop, the second leg must depart two hours after the arrival of the first leg. 
This microservice consumes two Ryanair APIs to calculate this data, but can be extended to process other airlines.

## Installation / Requirements
* Java 8 / JDK 1.8 - and appropriate environment variables set such as `$JAVA_HOME`.
* Maven 3 - `mvn` binary path will need to be set on your `$PATH` environment variable.

All other dependencies will be downloaded during build, such as Spring Boot, Mockito, etc.

## Running
To compile and run tests, execute this command in your shell/command prompt
 
```
mvn clean verify
```

This will produce `target/flights-0.0.1-SNAPSHOT.jar`. To run the Spring Boot application, execute

```
java -jar target/flights-0.0.1-SNAPSHOT.jar
```

This will start the Spring Boot application on `http://localhost:8080` by default.
I recommend using [Postman](https://www.getpostman.com/) to send requests to the application. 

## Endpoints

### `{host}/api/v1/interconnections`

Request parameters

| Name | Type | Example |
|:----- | :---: | :---:|
| `departure` | IATA Code | DUB |
| `arrival` | IATA Code | SXF |
| `departureDateTime` | Date-Time in ISO format | 2019-12-03T08:00 |
| `arrivalDateTime` | Date-Time in ISO format | 2019-12-04T10:00 |  

Full example URL: `http://localhost:8080/api/v1/interconnections?departure=DUB&arrival=SXF&departureDateTime=2019-12-03T08:00&arrivalDateTime=2019-12-04T10:00`

#### Parameter restrictions
* All are required, they cannot be empty or not supplied.
* `departure` and `arrival` codes cannot be the same. They must also be 3-letter codes.
* `departureDateTime` cannot occur before the current day.

## Configuration
There are a few keys that can be configured in `application.properties` or by setting them up in your Spring environment. 

* `ryanairApiClientBaseUrl`: default is `https://services-api.ryanair.com`
* `routeOperator`: default is `RYANAIR`

## Assumptions
* The departure and arrival times occur on the same day in their respective local times. 
There is no flight that arrives on the day before or the day after it departs.

## Known Bugs
SpotBugs is enabled on this project. Execute 
```
mvn clean verify
``` 
and provided all tests pass, then execute 
```
mvn spotbugs:gui
``` 
to bring up a GUI for inspecting what bugs exist in the code. 
Currently there are 3 static analysis bugs:
* Two regarding String localisation with the `.toUpperCase()` method in the `FlightV1Controller`. 
    As this is done on the IATA codes (which are Latin characters), this is ignored for now.
* One regarding an anonymous class involving `ParameterizedTypeReference` of a `List<Route>` when using `RestTemplate` 
    when calling the Routes API. As this is only used once in the code, I decided to keep as an anonymous class.

## Testing
The repo contains a number of test suites for the application, at the controller, service, client levels.
Network interactions are mocked out in the tests contained in the `service` folder. 
They will respond instead with the test JSON constructed from real data of sample API requests.

The test data is a subset of that which comes from the real APIs (listed below), reduced to make it easier to calculate expected results, along with the irrelevant fields removed.
The tests focus on connections from DUB to SXF (Dublin and Berlin Schoenefeld airports) during January/February 2030 (data is actually from Jan/Feb 2020).

* Routes API: `https://services-api.ryanair.com/locate/3/routes/`
* Schedules API: `https://services-api.ryanair.com/timtbl/3/schedules/{departure}/{arrival}/years/{year}/months/{month}`

#### Routes API data
There are 11 routes contained in the `routes-subset.json` file in `src/test/resources/`.

| Departing airports | Arriving airports       | Notes                             |
|-------------------:|-------------------------|-----------------------------------|
| DUB                | ACE, AMS, BCN, LGW, SXF | AMS and LGW do not connect to SXF |
| ACE, BCN, DUB, EVN | SXF                     | EVN does not connect from DUB     |
| SXF                | DUB                     | Wrong direction                   |
| MLA                | TUN                     | `operator` is `AIR_MALTA`         |
| VNO                | CAG                     | `connectingAirport` is not `null` |

Only 3 journeys (made up of 5 individual routes) are potentially valid:
* `DUB -> SXF`
* `DUB -> ACE -> SXF`
* `DUB -> BCN -> SXF`

The other routes are invalid due to no connection, wrong direction, incorrect `operator`, or `connectingAirport` is not `null`.

#### Schedules API data
As there is no need to get the schedules for the invalid routes, we will make at most 5 calls to the Schedules for each year/month in the specified time-range.
As we have two months to query, this will be at most 10 calls for Schedules.
However, this may be less as if the first leg has no suitable times, there is no need to query for Schedules for the second leg.

The test data is contained in the JSON files prefixed with `schedule-` in the `src/test/resources/` folder.
There are 9 files in total, 2 months each for 5 routes, but with one month missing (simulated `404` response from Schedules API).

#### Test Suites
My method of testing this API is to do full service tests, that is, we send a crafted request to our microservice instance, and put expectations on what the response should be.
This method can cut down on the amount of extraneous unit testing that is done at the class level, and allows us to change the internal workings of the API without having to change a
bunch of unit tests at the same time, so long as the API response remains the same expected value.
 
Various Spring Beans will get mocked in different test suites, meaning we can have them return whatever we choose to.
We can also verify when these mock beans are called with specific parameters and how many times.
This is done using the Mockito framework.

##### FlightV1ControllerBasicServiceTest
This test suite is just testing Spring's handling on a request on a non-existent API. 
We verify that the status code returned is `404`

##### FlightV1ControllerParamValidationServiceTests
This test suite is at the controller level and tests that the parameters submitted in the request are valid, otherwise return a `400 Bad Request` response. 
In the case of valid parameters, we verify that the `flightService.getAvailableFlights` method is called.
In all other cases, we verify that this is *never* called.

| Test Cases             | Expected Response |
|------------------------|:-----------------:|
| No Parameters          | 400               |
| Empty Parameters       | 400               |
| Invalid DateTimes      | 400               |
| Departure before today | 400               |
| Non-IATA airport codes | 400               |
| Same airport codes     | 400               |
| Valid parameters       | 200               |

##### FlightV1ControllerFullServiceTests
This test suite tests both the `FlightService` and `RyanairApiClient` levels of code.
The `RyanairApiClient` class is the code used to communicate with the Ryanair APIs, handle the responses, and return a usable object rather than the raw JSON.
Rather than communicate directly with the Ryanair APIs, the HTTP requests from `RestTemplate` are mocked out and return test data as mentioned above.

| Test Cases              |   Expected Response   |
|-------------------------|:---------------------:|
| Routes returns empty    |    200 - No flights   |
| Routes API down         |          502          |
| Routes Rate-Limits      |  500 (no retry logic) |
| Schedules API down      |          502          |
| Schedules returns empty |    200 - No flights   |
| Routes & Schedules up   | 200 - List of flights |

The happy path case was only tested in one scenario from the given test data.
Given the wide range of tests that could be taken involving different schedules, I decided to intentionally limit the scope to a single case that would test the majority of code paths. 

#### Manual testing
Manual sanity tests were performed with the live APIs.