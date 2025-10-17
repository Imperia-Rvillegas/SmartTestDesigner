using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;

namespace MyApp.Application.Interfaces
{
    public interface IIntegrationService
    {
        Task<List<IntegrationDto>> GetAllAsync(CancellationToken cancellationToken);
    }
}
