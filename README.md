# Ktor DI Overview

This project compares different Dependency Injection frameworks by building the same example project for each framework:

- [Ktor DI EAP](https://blog.jetbrains.com/kotlin/2024/03/ktor-2024-roadmap-di-update/)
- [Koin](https://github.com/InsertKoinIO/koin)
- [kotlin-inject](https://github.com/evant/kotlin-inject)
- [Dagger 2](https://github.com/google/dagger)
- [KodeIn](https://github.com/kosi-libs/Kodein)

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Domain

The domain of the project is fairly simple, and all modules use the same structuring:

```console
com.example/
├── config/
│   └── [DI Configuration Files]
├── posts/
│   ├── Post.kt
│   ├── Repository.kt
│   └── Routes.kt
├── users/
│   ├── User.kt
│   ├── Repository.kt
│   └── Routes.kt
├── comments/
│   ├── Comment.kt
│   ├── Repository.kt
│   └── Routes.kt
└── Application.kt
```

If you inspect and compare all the files,
you will see that only the `config` module where the `DI` code resides differentiates.
The actual Ktor- and business-related code remains unchanged between all frameworks.

## Features

Here's a list of features included in this project:

| Name                                           | Description                       |
|------------------------------------------------|-----------------------------------|
| [Routing](https://start.ktor.io/p/routing)     | Provides a structured routing DSL |
| [Resources](https://start.ktor.io/p/resources) | Provides type-safe routing        |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
