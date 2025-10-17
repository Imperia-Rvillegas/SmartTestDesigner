using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;

namespace MyApp.Application.Interfaces
{
    public interface IProjectService
    {
        Task<List<ProjectDto>> GetAllAsync(CancellationToken cancellationToken);
    }
}
