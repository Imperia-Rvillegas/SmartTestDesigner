using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;

namespace MyApp.Controllers.Api
{
    [ApiController]
    [Route("api/[controller]")]
    public class ScenariosController : ControllerBase
    {
        private readonly IScenarioService scenarioService;

        public ScenariosController(IScenarioService scenarioService)
        {
            this.scenarioService = scenarioService;
        }

        [HttpGet]
        public async Task<IActionResult> GetAllAsync(CancellationToken cancellationToken)
        {
            return Ok(await scenarioService.GetAllAsync(cancellationToken));
        }

        [HttpGet("{id:guid}")]
        public async Task<IActionResult> GetByIdAsync(Guid id, CancellationToken cancellationToken)
        {
            ScenarioDto? scenario = await scenarioService.GetByIdAsync(id, cancellationToken);
            if (scenario == null)
            {
                return NotFound();
            }

            return Ok(scenario);
        }

        [HttpPost]
        public async Task<IActionResult> CreateAsync([FromBody] CreateScenarioRequest request, CancellationToken cancellationToken)
        {
            if (!ModelState.IsValid)
            {
                return ValidationProblem(ModelState);
            }

            ScenarioDto scenario = await scenarioService.CreateAsync(request, cancellationToken);
            return CreatedAtAction(nameof(GetByIdAsync), new { id = scenario.Id }, scenario);
        }

        [HttpPut("{id:guid}")]
        public async Task<IActionResult> UpdateAsync(Guid id, [FromBody] UpdateScenarioRequest request, CancellationToken cancellationToken)
        {
            if (!ModelState.IsValid)
            {
                return ValidationProblem(ModelState);
            }

            ScenarioDto? scenario = await scenarioService.UpdateAsync(id, request, cancellationToken);
            if (scenario == null)
            {
                return NotFound();
            }

            return Ok(scenario);
        }

        [HttpDelete("{id:guid}")]
        public async Task<IActionResult> DeleteAsync(Guid id, CancellationToken cancellationToken)
        {
            bool deleted = await scenarioService.DeleteAsync(id, cancellationToken);
            if (!deleted)
            {
                return NotFound();
            }

            return NoContent();
        }
    }
}
