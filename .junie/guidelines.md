# Kotlin Style Guide for Ktor-Dagger Project

This style guide outlines the coding conventions for our Kotlin-based Ktor project with Dagger dependency injection.

## Idiomatic Kotlin Usage

### General Principles
- Prefer Kotlin's built-in features over Java-style code
- Use expression-oriented programming where appropriate
- Leverage extension functions to enhance existing classes
- Use nullable types and safe calls (`?.`) instead of null checks
- Utilize `when` expressions instead of complex if-else chains

### Code Organization
- Group code by feature rather than by layer
- Keep files focused on a single responsibility
- Use extension functions to organize related functionality

## Architectural Patterns

### Ktor Specific
- Use the Ktor DSL for defining routes and application features
- Organize routes by feature or resource type
- Use type-safe routing with `@Resource` annotations
- Keep route handlers concise, delegating business logic to service classes

### Dependency Injection with Dagger
- Use constructor injection as the primary DI method
- Mark classes that should be singletons with `@Singleton`
- Define clear component interfaces with meaningful method names
- Avoid unnecessary scopes; prefer unscoped or singleton

### Service Layer
- Create service classes for business logic
- Services should be injectable and have a single responsibility
- Use interfaces for services when multiple implementations might exist

## Common Pitfalls to Avoid

### Kotlin Specific
- Avoid unnecessary use of `!!` operator
- Don't overuse extension functions for unrelated functionality
- Be cautious with higher-order functions that might impact readability
- Avoid excessive nesting of lambdas

### Ktor Specific
- Don't put business logic directly in route handlers
- Avoid blocking operations in the main application thread
- Don't forget to handle exceptions in routes

### Dagger Specific
- Avoid circular dependencies
- Don't create unnecessary modules
- Be careful with scope annotations

## Stylistic Preferences

### Functional vs Object-Oriented
- Prefer a balanced approach, using functional programming for data transformations
- Use object-oriented design for stateful components and services
- Leverage Kotlin's functional features (map, filter, etc.) for collections

### Mutability
- Prefer immutable data (`val` over `var`)
- Use data classes for DTOs and domain models
- Consider using immutable collections when appropriate

### Naming Conventions
- Follow Kotlin's official naming conventions
- Classes: PascalCase
- Functions and properties: camelCase
- Constants: SCREAMING_SNAKE_CASE
- Use descriptive names that convey purpose

### Code Formatting
- Use 4 spaces for indentation
- Keep line length under 120 characters
- Use trailing commas in multi-line declarations
- Follow the official Kotlin coding conventions

## Testing
- Write unit tests for all business logic
- Use Ktor's test host for integration testing
- Mock dependencies in unit tests
- Test both success and failure scenarios