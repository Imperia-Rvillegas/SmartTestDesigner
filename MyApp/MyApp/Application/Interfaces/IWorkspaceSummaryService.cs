using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;

namespace MyApp.Application.Interfaces
{
    public interface IWorkspaceSummaryService
    {
        Task<WorkspaceSummaryDto> GetSummaryAsync(CancellationToken cancellationToken);
    }
}
