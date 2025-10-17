using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;

namespace MyApp.Application.Services
{
    public class WorkspaceSummaryService : IWorkspaceSummaryService
    {
        private readonly IScenarioService scenarioService;
        private readonly ILocatorService locatorService;
        private readonly IProjectService projectService;
        private readonly IExecutionService executionService;
        private readonly IIntegrationService integrationService;
        private readonly IWorkspaceSettingService settingService;

        public WorkspaceSummaryService(
            IScenarioService scenarioService,
            ILocatorService locatorService,
            IProjectService projectService,
            IExecutionService executionService,
            IIntegrationService integrationService,
            IWorkspaceSettingService settingService)
        {
            this.scenarioService = scenarioService;
            this.locatorService = locatorService;
            this.projectService = projectService;
            this.executionService = executionService;
            this.integrationService = integrationService;
            this.settingService = settingService;
        }

        public async Task<WorkspaceSummaryDto> GetSummaryAsync(CancellationToken cancellationToken)
        {
            WorkspaceSummaryDto summary = new WorkspaceSummaryDto
            {
                Scenarios = await scenarioService.GetAllAsync(cancellationToken),
                Locators = await locatorService.GetAllAsync(cancellationToken),
                Projects = await projectService.GetAllAsync(cancellationToken),
                Executions = await executionService.GetAllAsync(cancellationToken),
                Integrations = await integrationService.GetAllAsync(cancellationToken),
                Settings = await settingService.GetAllAsync(cancellationToken)
            };

            return summary;
        }
    }
}
