using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Domain.Entities;

namespace MyApp.Infrastructure.Data
{
    public static class DatabaseInitializer
    {
        public static async Task InitialiseAsync(ApplicationDbContext context, CancellationToken cancellationToken)
        {
            await context.Database.EnsureCreatedAsync(cancellationToken);

            if (!context.Scenarios.Any())
            {
                await SeedScenariosAsync(context, cancellationToken);
            }

            if (!context.Locators.Any())
            {
                await SeedLocatorsAsync(context, cancellationToken);
            }

            if (!context.Projects.Any())
            {
                await SeedProjectsAsync(context, cancellationToken);
            }

            if (!context.Executions.Any())
            {
                await SeedExecutionsAsync(context, cancellationToken);
            }

            if (!context.Integrations.Any())
            {
                await SeedIntegrationsAsync(context, cancellationToken);
            }

            if (!context.Settings.Any())
            {
                await SeedSettingsAsync(context, cancellationToken);
            }
        }

        private static async Task SeedScenariosAsync(ApplicationDbContext context, CancellationToken cancellationToken)
        {
            Scenario loginScenario = new Scenario
            {
                Id = Guid.NewGuid(),
                Name = "@@smoke Validar login Imperia",
                Objective = "Garantizar acceso exitoso al dashboard",
                Status = "Listo",
                Owner = "rvillegas",
                LastUpdatedUtc = DateTime.UtcNow.AddHours(-2),
                Tags = new List<string> { "@@smoke", "@@qa" }
            };

            loginScenario.Steps = new List<ScenarioStep>
            {
                new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = loginScenario.Id,
                    StepType = ScenarioStepType.Given,
                    Sequence = 1,
                    Description = "que el usuario abre la pantalla de acceso"
                },
                new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = loginScenario.Id,
                    StepType = ScenarioStepType.When,
                    Sequence = 2,
                    Description = "ingresa credenciales correctas"
                },
                new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = loginScenario.Id,
                    StepType = ScenarioStepType.Then,
                    Sequence = 3,
                    Description = "visualiza el dashboard principal"
                }
            };

            Scenario rejectionScenario = new Scenario
            {
                Id = Guid.NewGuid(),
                Name = "@@regression Gestión pedidos rechazo",
                Objective = "Validar flujo de rechazo de pedidos",
                Status = "En revisión",
                Owner = "lmartinez",
                LastUpdatedUtc = DateTime.UtcNow.AddDays(-1),
                Tags = new List<string> { "@@regression", "@@stage" }
            };

            rejectionScenario.Steps = new List<ScenarioStep>
            {
                new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = rejectionScenario.Id,
                    StepType = ScenarioStepType.Given,
                    Sequence = 1,
                    Description = "que existe un pedido pendiente de aprobación"
                },
                new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = rejectionScenario.Id,
                    StepType = ScenarioStepType.When,
                    Sequence = 2,
                    Description = "el analista rechaza el pedido"
                },
                new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = rejectionScenario.Id,
                    StepType = ScenarioStepType.Then,
                    Sequence = 3,
                    Description = "se genera notificación a logística"
                }
            };

            await context.Scenarios.AddRangeAsync(new[] { loginScenario, rejectionScenario }, cancellationToken);
            await context.SaveChangesAsync(cancellationToken);
        }

        private static async Task SeedLocatorsAsync(ApplicationDbContext context, CancellationToken cancellationToken)
        {
            List<LocatorDefinition> locators = new List<LocatorDefinition>
            {
                new LocatorDefinition
                {
                    Id = Guid.NewGuid(),
                    Name = "btnLogin",
                    Selector = "[data-testid='login-btn']",
                    ConfidencePercentage = 0.95,
                    ElementType = "Button",
                    AlternativeSelectors = new List<string> { "css:.btn-primary", "xpath://button[@id='login']" }
                },
                new LocatorDefinition
                {
                    Id = Guid.NewGuid(),
                    Name = "inputEmail",
                    Selector = "input[name='email']",
                    ConfidencePercentage = 0.92,
                    ElementType = "Input",
                    AlternativeSelectors = new List<string> { "css:#email", "xpath://input[@type='email']" }
                }
            };

            await context.Locators.AddRangeAsync(locators, cancellationToken);
            await context.SaveChangesAsync(cancellationToken);
        }

        private static async Task SeedProjectsAsync(ApplicationDbContext context, CancellationToken cancellationToken)
        {
            List<AutomationProject> projects = new List<AutomationProject>
            {
                new AutomationProject
                {
                    Id = Guid.NewGuid(),
                    Name = "Imperia Runner",
                    RepositoryUrl = "https://github.com/imperia/runner",
                    Framework = "Playwright",
                    LastSyncedUtc = DateTime.UtcNow.AddHours(-3)
                },
                new AutomationProject
                {
                    Id = Guid.NewGuid(),
                    Name = "Pedidos API",
                    RepositoryUrl = "https://github.com/imperia/pedidos-api",
                    Framework = "RestAssured",
                    LastSyncedUtc = DateTime.UtcNow.AddDays(-2)
                }
            };

            await context.Projects.AddRangeAsync(projects, cancellationToken);
            await context.SaveChangesAsync(cancellationToken);
        }

        private static async Task SeedExecutionsAsync(ApplicationDbContext context, CancellationToken cancellationToken)
        {
            List<TestExecution> executions = new List<TestExecution>
            {
                new TestExecution
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = context.Scenarios.First().Id,
                    Environment = "QA",
                    Status = "Éxito",
                    StartedAtUtc = DateTime.UtcNow.AddHours(-5),
                    FinishedAtUtc = DateTime.UtcNow.AddHours(-4.5)
                },
                new TestExecution
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = context.Scenarios.Skip(1).First().Id,
                    Environment = "Stage",
                    Status = "En progreso",
                    StartedAtUtc = DateTime.UtcNow.AddHours(-1)
                }
            };

            await context.Executions.AddRangeAsync(executions, cancellationToken);
            await context.SaveChangesAsync(cancellationToken);
        }

        private static async Task SeedIntegrationsAsync(ApplicationDbContext context, CancellationToken cancellationToken)
        {
            List<ExternalIntegration> integrations = new List<ExternalIntegration>
            {
                new ExternalIntegration
                {
                    Id = Guid.NewGuid(),
                    Name = "Jira Cloud",
                    Type = "Issue Tracker",
                    Status = "Conectado",
                    ConnectedAtUtc = DateTime.UtcNow.AddDays(-7)
                },
                new ExternalIntegration
                {
                    Id = Guid.NewGuid(),
                    Name = "Slack QA",
                    Type = "Notificaciones",
                    Status = "Conectado",
                    ConnectedAtUtc = DateTime.UtcNow.AddDays(-3)
                }
            };

            await context.Integrations.AddRangeAsync(integrations, cancellationToken);
            await context.SaveChangesAsync(cancellationToken);
        }

        private static async Task SeedSettingsAsync(ApplicationDbContext context, CancellationToken cancellationToken)
        {
            List<WorkspaceSetting> settings = new List<WorkspaceSetting>
            {
                new WorkspaceSetting
                {
                    Id = Guid.NewGuid(),
                    Key = "runner.defaultBrowser",
                    Value = "Chrome"
                },
                new WorkspaceSetting
                {
                    Id = Guid.NewGuid(),
                    Key = "runner.baseUrl",
                    Value = "https://qa.imperia.com"
                },
                new WorkspaceSetting
                {
                    Id = Guid.NewGuid(),
                    Key = "integrations.slack.channel",
                    Value = "#qa-alertas"
                }
            };

            await context.Settings.AddRangeAsync(settings, cancellationToken);
            await context.SaveChangesAsync(cancellationToken);
        }
    }
}
