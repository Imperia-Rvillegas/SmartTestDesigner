using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;

namespace MyApp.Application.Interfaces
{
    public interface IScenarioService
    {
        Task<List<ScenarioDto>> GetAllAsync(CancellationToken cancellationToken);

        Task<ScenarioDto?> GetByIdAsync(Guid id, CancellationToken cancellationToken);

        Task<ScenarioDto> CreateAsync(CreateScenarioRequest request, CancellationToken cancellationToken);

        Task<ScenarioDto?> UpdateAsync(Guid id, UpdateScenarioRequest request, CancellationToken cancellationToken);

        Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken);
    }
}
