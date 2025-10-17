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
    public class ProjectRepository : IProjectRepository
    {
        private readonly ApplicationDbContext context;

        public ProjectRepository(ApplicationDbContext context)
        {
            this.context = context;
        }

        public async Task<List<AutomationProject>> GetAllAsync(CancellationToken cancellationToken)
        {
            return await context.Projects
                .AsNoTracking()
                .OrderBy(project => project.Name)
                .ToListAsync(cancellationToken);
        }
    }
}
