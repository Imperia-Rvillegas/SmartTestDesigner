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
    public class ExecutionRepository : IExecutionRepository
    {
        private readonly ApplicationDbContext context;

        public ExecutionRepository(ApplicationDbContext context)
        {
            this.context = context;
        }

        public async Task<List<TestExecution>> GetAllAsync(CancellationToken cancellationToken)
        {
            return await context.Executions
                .AsNoTracking()
                .OrderByDescending(execution => execution.StartedAtUtc)
                .ToListAsync(cancellationToken);
        }
    }
}
