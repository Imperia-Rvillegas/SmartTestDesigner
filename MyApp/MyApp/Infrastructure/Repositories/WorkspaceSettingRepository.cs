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
    public class WorkspaceSettingRepository : IWorkspaceSettingRepository
    {
        private readonly ApplicationDbContext context;

        public WorkspaceSettingRepository(ApplicationDbContext context)
        {
            this.context = context;
        }

        public async Task<List<WorkspaceSetting>> GetAllAsync(CancellationToken cancellationToken)
        {
            return await context.Settings
                .AsNoTracking()
                .OrderBy(setting => setting.Key)
                .ToListAsync(cancellationToken);
        }
    }
}
