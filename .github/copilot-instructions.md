# Project-Level Copilot Instructions

## Build Tool

- This project uses **Maven** as the build tool.
- **Always use `mvn` (system-installed Maven) instead of `mvnw` or `mvnw.cmd` (Maven Wrapper).**
- Example commands:
  - Build: `mvn clean package -DskipTests`
  - Full build with tests: `mvn clean install`
  - Run tests: `mvn test`
  - Run application: `mvn spring-boot:run`

## Project Overview

- This is a **Spring Boot** multi-module Java project named **Mortise**.
- The project uses **Java 21+**, **MyBatis-Flex**, **PostgreSQL**, and **Spring Security OAuth2**.
- Frontend is in `frontend/` directory, using **pnpm** as package manager.

## Terminal Environment

- Default terminal is **PowerShell 7 (pwsh)**.
- Use PowerShell syntax and cmdlets when running terminal commands.

## Language

- Code comments, commit messages, and documentation should be in **Chinese (简体中文)** unless specified otherwise.
