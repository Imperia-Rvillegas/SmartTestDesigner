using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;
using MyApp.Domain.Entities;

namespace MyApp.Application.Services
{
    public class ProjectService : IProjectService
    {
        private readonly IProjectRepository projectRepository;

        public ProjectService(IProjectRepository projectRepository)
        {
            this.projectRepository = projectRepository;
        }

        public async Task<List<ProjectDto>> GetAllAsync(CancellationToken cancellationToken)
        {
            List<AutomationProject> projects = await projectRepository.GetAllAsync(cancellationToken);
            return projects.Select(project => new ProjectDto
            {
                Id = project.Id,
                Name = project.Name,
                RepositoryUrl = project.RepositoryUrl,
                Framework = project.Framework,
                LastSyncedUtc = project.LastSyncedUtc
            }).ToList();
        }
    }
}
