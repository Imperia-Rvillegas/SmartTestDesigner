using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;
using MyApp.Domain.Entities;

namespace MyApp.Application.Services
{
    public class ExecutionService : IExecutionService
    {
        private readonly IExecutionRepository executionRepository;

        public ExecutionService(IExecutionRepository executionRepository)
        {
            this.executionRepository = executionRepository;
        }

        public async Task<List<ExecutionDto>> GetAllAsync(CancellationToken cancellationToken)
        {
            List<TestExecution> executions = await executionRepository.GetAllAsync(cancellationToken);
            return executions.Select(execution => new ExecutionDto
            {
                Id = execution.Id,
                ScenarioId = execution.ScenarioId,
                Environment = execution.Environment,
                Status = execution.Status,
                StartedAtUtc = execution.StartedAtUtc,
                FinishedAtUtc = execution.FinishedAtUtc
            }).ToList();
        }
    }
}
