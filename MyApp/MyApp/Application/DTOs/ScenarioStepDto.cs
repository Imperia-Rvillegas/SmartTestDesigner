using System;
using MyApp.Domain.Entities;

namespace MyApp.Application.DTOs
{
    public class ScenarioStepDto
    {
        public Guid Id { get; set; }

        public ScenarioStepType StepType { get; set; }

        public int Sequence { get; set; }

        public string Description { get; set; } = string.Empty;
    }
}
