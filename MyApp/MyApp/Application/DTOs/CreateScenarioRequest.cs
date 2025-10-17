using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using MyApp.Domain.Entities;

namespace MyApp.Application.DTOs
{
    public class CreateScenarioRequest
    {
        [Required]
        [StringLength(140)]
        public string Name { get; set; } = string.Empty;

        [Required]
        [StringLength(300)]
        public string Objective { get; set; } = string.Empty;

        [Required]
        [StringLength(40)]
        public string Status { get; set; } = string.Empty;

        [Required]
        [StringLength(40)]
        public string Owner { get; set; } = string.Empty;

        public List<string> Tags { get; set; } = new List<string>();

        public List<CreateScenarioStepRequest> Steps { get; set; } = new List<CreateScenarioStepRequest>();
    }

    public class CreateScenarioStepRequest
    {
        [Required]
        public ScenarioStepType StepType { get; set; }

        [Range(1, int.MaxValue)]
        public int Sequence { get; set; }

        [Required]
        [StringLength(280)]
        public string Description { get; set; } = string.Empty;
    }
}
