using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Domain.Entities;

namespace MyApp.Application.Interfaces
{
    public interface IScenarioRepository
    {
        Task<List<Scenario>> GetAllAsync(CancellationToken cancellationToken);

        Task<Scenario?> GetByIdAsync(Guid id, CancellationToken cancellationToken);

        Task AddAsync(Scenario scenario, CancellationToken cancellationToken);

        Task UpdateAsync(Scenario scenario, CancellationToken cancellationToken);

        Task DeleteAsync(Scenario scenario, CancellationToken cancellationToken);

        Task SaveChangesAsync(CancellationToken cancellationToken);
    }
}
