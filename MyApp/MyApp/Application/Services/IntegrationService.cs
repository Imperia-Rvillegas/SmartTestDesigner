using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;
using MyApp.Domain.Entities;

namespace MyApp.Application.Services
{
    public class IntegrationService : IIntegrationService
    {
        private readonly IIntegrationRepository integrationRepository;

        public IntegrationService(IIntegrationRepository integrationRepository)
        {
            this.integrationRepository = integrationRepository;
        }

        public async Task<List<IntegrationDto>> GetAllAsync(CancellationToken cancellationToken)
        {
            List<ExternalIntegration> integrations = await integrationRepository.GetAllAsync(cancellationToken);
            return integrations.Select(integration => new IntegrationDto
            {
                Id = integration.Id,
                Name = integration.Name,
                Type = integration.Type,
                Status = integration.Status,
                ConnectedAtUtc = integration.ConnectedAtUtc
            }).ToList();
        }
    }
}
