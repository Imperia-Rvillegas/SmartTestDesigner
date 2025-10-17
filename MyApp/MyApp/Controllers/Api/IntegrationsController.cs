using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MyApp.Application.Interfaces;

namespace MyApp.Controllers.Api
{
    [ApiController]
    [Route("api/[controller]")]
    public class IntegrationsController : ControllerBase
    {
        private readonly IIntegrationService integrationService;

        public IntegrationsController(IIntegrationService integrationService)
        {
            this.integrationService = integrationService;
        }

        [HttpGet]
        public async Task<IActionResult> GetAllAsync(CancellationToken cancellationToken)
        {
            return Ok(await integrationService.GetAllAsync(cancellationToken));
        }
    }
}
