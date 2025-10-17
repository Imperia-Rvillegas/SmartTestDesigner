using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Domain.Entities;

namespace MyApp.Application.Interfaces
{
    public interface IIntegrationRepository
    {
        Task<List<ExternalIntegration>> GetAllAsync(CancellationToken cancellationToken);
    }
}
