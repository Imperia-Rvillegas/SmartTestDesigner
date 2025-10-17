using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using MyApp.Application.Interfaces;
using MyApp.Domain.Entities;
using MyApp.Infrastructure.Data;

namespace MyApp.Infrastructure.Repositories
{
    public class ScenarioRepository : IScenarioRepository
    {
        private readonly ApplicationDbContext context;

        public ScenarioRepository(ApplicationDbContext context)
        {
            this.context = context;
        }

        public async Task<List<Scenario>> GetAllAsync(CancellationToken cancellationToken)
        {
            return await context.Scenarios
                .Include(scenario => scenario.Steps)
                .AsNoTracking()
                .OrderByDescending(scenario => scenario.LastUpdatedUtc)
                .ToListAsync(cancellationToken);
        }

        public async Task<Scenario?> GetByIdAsync(Guid id, CancellationToken cancellationToken)
        {
            return await context.Scenarios
                .Include(scenario => scenario.Steps)
                .FirstOrDefaultAsync(scenario => scenario.Id == id, cancellationToken);
        }

        public async Task AddAsync(Scenario scenario, CancellationToken cancellationToken)
        {
            await context.Scenarios.AddAsync(scenario, cancellationToken);
        }

        public async Task UpdateAsync(Scenario scenario, CancellationToken cancellationToken)
        {
            Scenario? trackedScenario = await context.Scenarios
                .Include(entity => entity.Steps)
                .FirstOrDefaultAsync(entity => entity.Id == scenario.Id, cancellationToken);

            if (trackedScenario == null)
            {
                return;
            }

            context.ScenarioSteps.RemoveRange(trackedScenario.Steps);
            context.Scenarios.Remove(trackedScenario);
            await context.Scenarios.AddAsync(scenario, cancellationToken);
        }

        public Task DeleteAsync(Scenario scenario, CancellationToken cancellationToken)
        {
            context.Scenarios.Remove(scenario);
            return Task.CompletedTask;
        }

        public async Task SaveChangesAsync(CancellationToken cancellationToken)
        {
            await context.SaveChangesAsync(cancellationToken);
        }
    }
}
