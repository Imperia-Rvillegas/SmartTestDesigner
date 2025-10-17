using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MyApp.Application.Interfaces;

namespace MyApp.Controllers.Api
{
    [ApiController]
    [Route("api/workspace/summary")]
    public class WorkspaceSummaryController : ControllerBase
    {
        private readonly IWorkspaceSummaryService summaryService;

        public WorkspaceSummaryController(IWorkspaceSummaryService summaryService)
        {
            this.summaryService = summaryService;
        }

        [HttpGet]
        public async Task<IActionResult> GetAsync(CancellationToken cancellationToken)
        {
            return Ok(await summaryService.GetSummaryAsync(cancellationToken));
        }
    }
}
