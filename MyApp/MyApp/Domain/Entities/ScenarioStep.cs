using System;

namespace MyApp.Domain.Entities
{
    public class ScenarioStep
    {
        public Guid Id { get; set; }

        public Guid ScenarioId { get; set; }

        public ScenarioStepType StepType { get; set; }

        public int Sequence { get; set; }

        public string Description { get; set; } = string.Empty;

        public Scenario Scenario { get; set; } = null!;
    }
}
