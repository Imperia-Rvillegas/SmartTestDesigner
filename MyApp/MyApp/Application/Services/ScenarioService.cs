using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;
using MyApp.Domain.Entities;

namespace MyApp.Application.Services
{
    public class ScenarioService : IScenarioService
    {
        private readonly IScenarioRepository scenarioRepository;

        public ScenarioService(IScenarioRepository scenarioRepository)
        {
            this.scenarioRepository = scenarioRepository;
        }

        public async Task<List<ScenarioDto>> GetAllAsync(CancellationToken cancellationToken)
        {
            List<Scenario> scenarios = await scenarioRepository.GetAllAsync(cancellationToken);
            return scenarios.Select(ConvertToDto).ToList();
        }

        public async Task<ScenarioDto?> GetByIdAsync(Guid id, CancellationToken cancellationToken)
        {
            Scenario? scenario = await scenarioRepository.GetByIdAsync(id, cancellationToken);
            if (scenario == null)
            {
                return null;
            }

            return ConvertToDto(scenario);
        }

        public async Task<ScenarioDto> CreateAsync(CreateScenarioRequest request, CancellationToken cancellationToken)
        {
            Scenario scenario = new Scenario
            {
                Id = Guid.NewGuid(),
                Name = request.Name.Trim(),
                Objective = request.Objective.Trim(),
                Status = request.Status.Trim(),
                Owner = request.Owner.Trim(),
                Tags = request.Tags.Select(tag => tag.Trim()).Where(tag => tag.Length > 0).Distinct(StringComparer.OrdinalIgnoreCase).ToList(),
                LastUpdatedUtc = DateTime.UtcNow
            };

            scenario.Steps = request.Steps
                .OrderBy(step => step.Sequence)
                .Select(step => new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = scenario.Id,
                    StepType = step.StepType,
                    Sequence = step.Sequence,
                    Description = step.Description.Trim()
                })
                .ToList();

            await scenarioRepository.AddAsync(scenario, cancellationToken);
            await scenarioRepository.SaveChangesAsync(cancellationToken);

            return ConvertToDto(scenario);
        }

        public async Task<ScenarioDto?> UpdateAsync(Guid id, UpdateScenarioRequest request, CancellationToken cancellationToken)
        {
            Scenario? existingScenario = await scenarioRepository.GetByIdAsync(id, cancellationToken);
            if (existingScenario == null)
            {
                return null;
            }

            Scenario updatedScenario = new Scenario
            {
                Id = existingScenario.Id,
                Name = request.Name.Trim(),
                Objective = request.Objective.Trim(),
                Status = request.Status.Trim(),
                Owner = request.Owner.Trim(),
                LastUpdatedUtc = DateTime.UtcNow,
                Tags = request.Tags.Select(tag => tag.Trim()).Where(tag => tag.Length > 0).Distinct(StringComparer.OrdinalIgnoreCase).ToList()
            };

            foreach (UpdateScenarioStepRequest stepRequest in request.Steps.OrderBy(step => step.Sequence))
            {
                ScenarioStep step = new ScenarioStep
                {
                    Id = Guid.NewGuid(),
                    ScenarioId = updatedScenario.Id,
                    StepType = stepRequest.StepType,
                    Sequence = stepRequest.Sequence,
                    Description = stepRequest.Description.Trim()
                };

                updatedScenario.Steps.Add(step);
            }

            await scenarioRepository.UpdateAsync(updatedScenario, cancellationToken);
            await scenarioRepository.SaveChangesAsync(cancellationToken);

            return ConvertToDto(updatedScenario);
        }

        public async Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken)
        {
            Scenario? scenario = await scenarioRepository.GetByIdAsync(id, cancellationToken);
            if (scenario == null)
            {
                return false;
            }

            await scenarioRepository.DeleteAsync(scenario, cancellationToken);
            await scenarioRepository.SaveChangesAsync(cancellationToken);

            return true;
        }

        private ScenarioDto ConvertToDto(Scenario scenario)
        {
            ScenarioDto dto = new ScenarioDto
            {
                Id = scenario.Id,
                Name = scenario.Name,
                Objective = scenario.Objective,
                Status = scenario.Status,
                Owner = scenario.Owner,
                LastUpdatedUtc = scenario.LastUpdatedUtc,
                Tags = scenario.Tags.ToList()
            };

            dto.Steps = scenario.Steps
                .OrderBy(step => step.Sequence)
                .Select(step => new ScenarioStepDto
                {
                    Id = step.Id,
                    StepType = step.StepType,
                    Sequence = step.Sequence,
                    Description = step.Description
                })
                .ToList();

            return dto;
        }
    }
}
