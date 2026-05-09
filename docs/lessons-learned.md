# Lessons Learned

## What is JaCoCo?

JaCoCo (Java Code Coverage) is a free code coverage library for Java that measures how much of your source code is executed by your tests.

### Key Features

- **Coverage metrics**: Tracks line coverage, branch coverage, instruction coverage, complexity, and method coverage
- **Integration**: Works seamlessly with Maven, Gradle, and popular CI/CD tools
- **Reports**: Generates HTML, XML, and CSV reports showing which parts of your code are tested
- **Byte code instrumentation**: Analyzes code coverage at the JVM byte code level without modifying source files

### In This Project

JaCoCo is already configured and running. The project has:
- `target/jacoco.exec` - The raw execution data from test runs
- `target/site/jacoco/` - HTML coverage reports you can open in a browser
- Coverage reports for all packages (auth, users, shared, config, health)

You can view the coverage report by opening `target/site/jacoco/index.html` in a browser to see exactly which lines of code are covered by tests and which aren't.

### Common Usage

```xml
<!-- In pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
</plugin>
```

JaCoCo helps identify untested code and maintain high test coverage in Java projects.
