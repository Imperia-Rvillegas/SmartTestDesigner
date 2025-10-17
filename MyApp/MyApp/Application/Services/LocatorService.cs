using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;
using MyApp.Domain.Entities;

namespace MyApp.Application.Services
{
    public class LocatorService : ILocatorService
    {
        private readonly ILocatorRepository locatorRepository;

        public LocatorService(ILocatorRepository locatorRepository)
        {
            this.locatorRepository = locatorRepository;
        }

        public async Task<List<LocatorDto>> GetAllAsync(CancellationToken cancellationToken)
        {
            List<LocatorDefinition> locators = await locatorRepository.GetAllAsync(cancellationToken);
            return locators.Select(locator => new LocatorDto
            {
                Id = locator.Id,
                Name = locator.Name,
                Selector = locator.Selector,
                ConfidencePercentage = locator.ConfidencePercentage,
                ElementType = locator.ElementType,
                AlternativeSelectors = locator.AlternativeSelectors.ToList()
            }).ToList();
        }
    }
}
