using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using FluentAssertions;
using Microsoft.EntityFrameworkCore;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;
using MyApp.Application.Services;
using MyApp.Domain.Entities;
using MyApp.Infrastructure.Data;
using MyApp.Infrastructure.Repositories;
using Xunit;

namespace MyApp.Tests
{
    public class ScenarioServiceTests
    {
        private readonly CancellationToken cancellationToken = CancellationToken.None;

        [Fact]
        public async Task CreateAsync_ShouldPersistScenarioWithStepsAndTags()
        {
            ApplicationDbContext context = CreateDbContext();
            IScenarioRepository repository = new ScenarioRepository(context);
            IScenarioService service = new ScenarioService(repository);

            CreateScenarioRequest request = new CreateScenarioRequest
            {
                Name = "@@smoke flujo login",
                Objective = "Validar acceso correcto",
                Status = "Listo",
                Owner = "qauser",
                Tags = new List<string> { "@@smoke", "@@login" },
                Steps = new List<CreateScenarioStepRequest>
                {
                    new CreateScenarioStepRequest
                    {
                        StepType = ScenarioStepType.Given,
                        Sequence = 1,
                        Description = "que el usuario está en la pantalla de acceso"
                    },
                    new CreateScenarioStepRequest
                    {
                        StepType = ScenarioStepType.When,
                        Sequence = 2,
                        Description = "ingresa credenciales válidas"
                    },
                    new CreateScenarioStepRequest
                    {
                        StepType = ScenarioStepType.Then,
                        Sequence = 3,
                        Description = "visualiza el dashboard"
                    }
                }
            };

            ScenarioDto result = await service.CreateAsync(request, cancellationToken);

            result.Id.Should().NotBeEmpty();
            result.Tags.Should().Contain(new[] { "@@smoke", "@@login" });
            result.Steps.Should().HaveCount(3);

            Scenario? stored = await context.Scenarios.Include(scenario => scenario.Steps).FirstOrDefaultAsync(scenario => scenario.Id == result.Id, cancellationToken);
            stored.Should().NotBeNull();
            stored!.Steps.Select(step => step.Sequence).Should().ContainInOrder(1, 2, 3);
        }

        [Fact]
        public async Task UpdateAsync_ShouldUpdateExistingScenario()
        {
            ApplicationDbContext context = CreateDbContext();
            IScenarioRepository repository = new ScenarioRepository(context);
            IScenarioService service = new ScenarioService(repository);

            Scenario existingScenario = new Scenario
            {
                Id = Guid.NewGuid(),
                Name = "Escenario base",
                Objective = "Objetivo inicial",
                Status = "Borrador",
                Owner = "qa",
                LastUpdatedUtc = DateTime.UtcNow,
                Tags = new List<string> { "@@draft" },
                Steps = new List<ScenarioStep>
                {
                    new ScenarioStep
                    {
                        Id = Guid.NewGuid(),
                        ScenarioId = Guid.Empty,
                        StepType = ScenarioStepType.Given,
                        Sequence = 1,
                        Description = "paso inicial"
                    }
                }
            };

            foreach (ScenarioStep step in existingScenario.Steps)
            {
                step.ScenarioId = existingScenario.Id;
            }

            await context.Scenarios.AddAsync(existingScenario, cancellationToken);
            await context.SaveChangesAsync(cancellationToken);

            UpdateScenarioRequest updateRequest = new UpdateScenarioRequest
            {
                Id = existingScenario.Id,
                Name = "Escenario actualizado",
                Objective = "Nuevo objetivo",
                Status = "Listo",
                Owner = "qa",
                Tags = new List<string> { "@@ready" },
                Steps = new List<UpdateScenarioStepRequest>
                {
                    new UpdateScenarioStepRequest
                    {
                        Id = existingScenario.Steps.First().Id,
                        StepType = ScenarioStepType.Given,
                        Sequence = 1,
                        Description = "paso actualizado"
                    },
                    new UpdateScenarioStepRequest
                    {
                        StepType = ScenarioStepType.Then,
                        Sequence = 2,
                        Description = "validación final"
                    }
                }
            };

            ScenarioDto? updated = await service.UpdateAsync(existingScenario.Id, updateRequest, cancellationToken);

            updated.Should().NotBeNull();
            updated!.Name.Should().Be("Escenario actualizado");
            updated.Steps.Should().HaveCount(2);
            updated.Tags.Should().ContainSingle(tag => tag == "@@ready");

            Scenario? stored = await context.Scenarios.Include(scenario => scenario.Steps).FirstOrDefaultAsync(scenario => scenario.Id == existingScenario.Id, cancellationToken);
            stored.Should().NotBeNull();
            stored!.Steps.Should().HaveCount(2);
        }

        [Fact]
        public async Task WorkspaceSummary_ShouldAggregateInformation()
        {
            ApplicationDbContext context = CreateDbContext();
            await DatabaseInitializer.InitialiseAsync(context, cancellationToken);

            IScenarioService scenarioService = new ScenarioService(new ScenarioRepository(context));
            ILocatorService locatorService = new LocatorService(new LocatorRepository(context));
            IProjectService projectService = new ProjectService(new ProjectRepository(context));
            IExecutionService executionService = new ExecutionService(new ExecutionRepository(context));
            IIntegrationService integrationService = new IntegrationService(new IntegrationRepository(context));
            IWorkspaceSettingService settingService = new WorkspaceSettingService(new WorkspaceSettingRepository(context));
            IWorkspaceSummaryService summaryService = new WorkspaceSummaryService(
                scenarioService,
                locatorService,
                projectService,
                executionService,
                integrationService,
                settingService);

            WorkspaceSummaryDto summary = await summaryService.GetSummaryAsync(cancellationToken);

            summary.Scenarios.Should().NotBeEmpty();
            summary.Locators.Should().NotBeEmpty();
            summary.Projects.Should().NotBeEmpty();
            summary.Executions.Should().NotBeEmpty();
            summary.Integrations.Should().NotBeEmpty();
            summary.Settings.Should().NotBeEmpty();
        }

        private ApplicationDbContext CreateDbContext()
        {
            DbContextOptionsBuilder<ApplicationDbContext> optionsBuilder = new DbContextOptionsBuilder<ApplicationDbContext>();
            optionsBuilder.UseInMemoryDatabase(Guid.NewGuid().ToString());
            return new ApplicationDbContext(optionsBuilder.Options);
        }
    }
}
