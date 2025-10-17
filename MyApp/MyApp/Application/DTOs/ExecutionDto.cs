using System;

namespace MyApp.Application.DTOs
{
    public class ExecutionDto
    {
        public Guid Id { get; set; }

        public Guid ScenarioId { get; set; }

        public string Environment { get; set; } = string.Empty;

        public string Status { get; set; } = string.Empty;

        public DateTime StartedAtUtc { get; set; }

        public DateTime? FinishedAtUtc { get; set; }
    }
}
