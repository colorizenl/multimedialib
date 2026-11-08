# AGENTS.md

This file provides instructions to agents working on this repository.

## Project summary

This is a Java framework that is used to create multimedia applications that run on the desktop,
as mobile apps, and in the browser via [TeaVM](https://teavm.org). Although this is a Java
library, the codebase contains a small portion of JavaScript that acts as a wrapper when running
in the browser.

## Commands

- `gradle test` runs all unit tests.
- `gradle test --tests '*SomethingTest'` runs unit tests in a specific class.
- `gradle spotbugsMain` runs the SpotBugs quality checks.
- `gradle assemble` does a full build.

## Architecture

- All renderer implementations must live in their own sub-package within 
  `nl.colorize.multimedialib.renderer`. All code related to that renderer must be located
  within its package.

## Code style

- Use the UTF-8 character encoding, 4 space indent, Unit line breaks.
- In general, code style is not very strict and not enforced. Try to follow a reasonable style.
- Maximum line length is ideally 100 characters, but this can be ignored if necessary.
- Use Lombok annotations to reduce boilerplate code.
