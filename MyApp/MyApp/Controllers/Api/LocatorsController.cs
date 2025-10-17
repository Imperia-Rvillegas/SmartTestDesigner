using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MyApp.Application.Interfaces;

namespace MyApp.Controllers.Api
{
    [ApiController]
    [Route("api/[controller]")]
    public class LocatorsController : ControllerBase
    {
        private readonly ILocatorService locatorService;

        public LocatorsController(ILocatorService locatorService)
        {
            this.locatorService = locatorService;
        }

        [HttpGet]
        public async Task<IActionResult> GetAllAsync(CancellationToken cancellationToken)
        {
            return Ok(await locatorService.GetAllAsync(cancellationToken));
        }
    }
}
