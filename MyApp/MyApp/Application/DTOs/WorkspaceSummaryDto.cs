using System.Collections.Generic;

namespace MyApp.Application.DTOs
{
    public class WorkspaceSummaryDto
    {
        public List<ScenarioDto> Scenarios { get; set; } = new List<ScenarioDto>();

        public List<LocatorDto> Locators { get; set; } = new List<LocatorDto>();

        public List<ProjectDto> Projects { get; set; } = new List<ProjectDto>();

        public List<ExecutionDto> Executions { get; set; } = new List<ExecutionDto>();

        public List<IntegrationDto> Integrations { get; set; } = new List<IntegrationDto>();

        public List<WorkspaceSettingDto> Settings { get; set; } = new List<WorkspaceSettingDto>();
    }
}
