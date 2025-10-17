using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MyApp.Application.Interfaces;

namespace MyApp.Controllers.Api
{
    [ApiController]
    [Route("api/[controller]")]
    public class ExecutionsController : ControllerBase
    {
        private readonly IExecutionService executionService;

        public ExecutionsController(IExecutionService executionService)
        {
            this.executionService = executionService;
        }

        [HttpGet]
        public async Task<IActionResult> GetAllAsync(CancellationToken cancellationToken)
        {
            return Ok(await executionService.GetAllAsync(cancellationToken));
        }
    }
}
