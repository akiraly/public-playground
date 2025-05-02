# Simple Gradle HTTP Build Cache Project Guidelines

## Project Overview

Simple Gradle Build Cache is a HTTP based Gradle Build Cache built with Kotlin and Spring Boot, using a functional interface-based coding style.

- the HTTP paths should have a `cache-id` and a `cache-key` segment
- `cache-key` is the Gradle Cache Key
- `cache-id` is used for segregating the Gradle Caches and will be specified by the Gradle builds as part of the url in the Gradle Build Cache configuration

The Gradle HTTP Build Cache contract is:

- When attempting to load an entry, a GET request is made to https://example.com:8123/cache/«cache-key». The response must have a 2xx status and the cache entry as the body, or a 404 Not Found status if the entry does not exist.
- When attempting to store an entry, a PUT request is made to https://example.com:8123/cache/«cache-key». Any 2xx response status is interpreted as success. A 413 Payload Too Large response may be returned to indicate that the payload is larger than the server will accept, which will not be treated as an error.
- Use of HTTP Expect-Continue can be enabled. This causes upload requests to happen in two parts: first a check whether a body would be accepted, then transmission of the body if the server indicates it will accept it.  
This is useful when uploading to cache servers that routinely redirect or reject upload requests, as it avoids uploading the cache entry just to have it rejected (e.g. the cache entry is larger than the cache will allow) or redirected. This additional check incurs extra latency when the server accepts the request, but reduces latency when the request is rejected or redirected.



### Tech Stack
- Kotlin 2.1.20+ with Java 21
- Spring Boot (Web)
- Storing caches on disk
- JUnit 5 and MockK for testing

## Project Structure

```
src/
├── main/
│   └── kotlin/
└── test/
    └── kotlin/
```

## Build and Run

- Build: `./gradlew build`
- Run application: `./gradlew run`
- Run tests: `./gradlew test`
- Run single test: `./gradlew test --tests "io.github.akiraly.sghbc.TestClassName.testMethodName"`
- Static analysis: `./gradlew detekt`

## Coding Conventions

### Functional Interface Style
- Define interfaces around actions (verbs): `fun interface ValidateUser : (User) -> Boolean`
- Single responsibility: Each interface does exactly one thing
- Explicit dependencies: Services declare their behavioral dependencies
- Composition: Build complex behavior by composing smaller components

### Naming Conventions
- Interfaces: Start with a verb (`Validate`, `Process`, `Transform`)
- Functional Interface Names should read like an action in a sentence, for example: `StoreInCache`, `RetrieveFromCache`
- Implementations: Interface name + "By"/"With" + strategy (`ValidateUserByEmail`)

### Kotlin Style
- Use functional features (extension functions, higher-order functions)
- Prefer immutability (`val` over `var`, immutable collections)
- Use null safety features (safe calls `?.`, Elvis operator `?:`)
- Follow Kotlin conventions (camelCase for properties/functions, PascalCase for classes)

### Other
- all code (including main and test) should be under the `io.github.akiraly.sghbc` package

## Testing Guidelines

### Test Structure
- Use Given-When-Then pattern
- Use descriptive test names with backticks: `` `should return 404 when user not found` ``

### Mocking with MockK
```kotlin
// Create a mock
private val validateUser: ValidateUser = mockk()

// Define behavior
every { validateUser.invoke(any()) } returns true

// Verify interactions
verify { validateUser.invoke(expectedUser) }
```

### Component Testing
- Controllers: Use MockMvc to test HTTP endpoints
- Services: Test each functional component in isolation

## Code Quality Requirements
- Maintain at least 80% code coverage
- Use Detekt for static code analysis
- Document public APIs and complex logic
- All warnings are treated as errors

## Contribution Workflow
- Follow a conventional commit format: `type(scope): message`
- All code must be reviewed before merging
- Ensure tests pass and coverage requirements are met
