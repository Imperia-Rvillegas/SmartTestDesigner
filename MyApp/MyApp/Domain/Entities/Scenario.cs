using System;
using System.Collections.Generic;

namespace MyApp.Domain.Entities
{
    public class Scenario
    {
        public Guid Id { get; set; }

        public string Name { get; set; } = string.Empty;

        public string Objective { get; set; } = string.Empty;

        public string Status { get; set; } = string.Empty;

        public string Owner { get; set; } = string.Empty;

        public DateTime LastUpdatedUtc { get; set; }

        public List<string> Tags { get; set; } = new List<string>();

        public List<ScenarioStep> Steps { get; set; } = new List<ScenarioStep>();
    }
}
