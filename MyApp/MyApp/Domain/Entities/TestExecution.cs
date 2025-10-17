using System;

namespace MyApp.Domain.Entities
{
    public class TestExecution
    {
        public Guid Id { get; set; }

        public Guid ScenarioId { get; set; }

        public string Environment { get; set; } = string.Empty;

        public string Status { get; set; } = string.Empty;

        public DateTime StartedAtUtc { get; set; }

        public DateTime? FinishedAtUtc { get; set; }
    }
}
