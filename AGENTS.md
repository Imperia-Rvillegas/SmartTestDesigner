## Architectural Requirements
- Avoid `out` parameters and optional parameters.

## Frontend Requirements
- Ensure every screen includes the following scripts:
  ```html
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://cdn.jsdelivr.net/npm/%40tailwindplus/elements@1" type="module"></script>
  ```

## Architectural Requirements
- Use Clean Architecture principles.
- Organize code into the following layers: Domain, Application, Infrastructure, and API.
- Employ Entity Framework Core for data access.
- Store configuration settings in `appsettings.json`.
- Implement RESTful controllers with validation and DTOs.
- Configure dependency injection within `Program.cs`.
- Use structured logging with Serilog.
- Write unit tests with xUnit and FluentAssertions.
- Provide Swagger/OpenAPI documentation.

## Code Style
- Declare variables with explicit types (no usage of `var`).
- Choose clear, descriptive names for variables.
- Use camelCase for local variables, PascalCase for methods and class-level variables, and UPPER_SNAKE_CASE for properties tagged as `Ta`.
- API endpoints must not return `Ta` elements.
- LINQ and Entity Framework transformations are allowed with reasonable simplicity.
- Use 4 spaces for indentation (no tabs).
- Use CRLF for new lines.
- Place braces (`{}`) on new lines.
- Insert a blank line between methods.
- Use blank lines inside methods judiciously—keep related code together.
- Include a space before and after any operator (except for the postfix `++`).
- Apply the `++` operator only in postfix form.
- Prefer `double` over `float`.
- Prefer `List<T>` over arrays.
- Avoid `out` parameters and optional parameters.
