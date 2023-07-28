### Hexlet tests and linter status:
[![Actions Status](https://github.com/Wo0ty/java-project-lvl4/workflows/hexlet-check/badge.svg)](https://github.com/Wo0ty/java-project-lvl4/actions)
[![Java CI](https://github.com/Wo0ty/java-project-lvl4/actions/workflows/build.yml/badge.svg)](https://github.com/Wo0ty/java-project-lvl4/actions/workflows/build.yml)
[![Maintainability](https://api.codeclimate.com/v1/badges/8c478f840717af74cb1e/maintainability)](https://codeclimate.com/github/Wo0ty/java-project-lvl4/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/8c478f840717af74cb1e/test_coverage)](https://codeclimate.com/github/Wo0ty/java-project-lvl4/test_coverage)

Web application based on the **Javalin** and **Ebean ORM** frameworks for analyzing web pages. The application checks the availability of the specified page. Information about the entered URLs and the results of the checks are saved in a database.
## System requirements
- JDK 20
- Gradle 8.2
- GNU Make

## Setup
```shell
make build
make start
# Open http://localhost:8090
```

## Demo
You can try Page Analyzer [on render](https://page-analyzer-avrw.onrender.com).