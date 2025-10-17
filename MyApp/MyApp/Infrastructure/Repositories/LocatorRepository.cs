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
    public class LocatorRepository : ILocatorRepository
    {
        private readonly ApplicationDbContext context;

        public LocatorRepository(ApplicationDbContext context)
        {
            this.context = context;
        }

        public async Task<List<LocatorDefinition>> GetAllAsync(CancellationToken cancellationToken)
        {
            return await context.Locators
                .AsNoTracking()
                .OrderBy(locator => locator.Name)
                .ToListAsync(cancellationToken);
        }
    }
}
