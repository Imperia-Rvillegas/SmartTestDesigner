using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MyApp.Application.Interfaces;

namespace MyApp.Controllers.Api
{
    [ApiController]
    [Route("api/[controller]")]
    public class SettingsController : ControllerBase
    {
        private readonly IWorkspaceSettingService settingService;

        public SettingsController(IWorkspaceSettingService settingService)
        {
            this.settingService = settingService;
        }

        [HttpGet]
        public async Task<IActionResult> GetAllAsync(CancellationToken cancellationToken)
        {
            return Ok(await settingService.GetAllAsync(cancellationToken));
        }
    }
}
