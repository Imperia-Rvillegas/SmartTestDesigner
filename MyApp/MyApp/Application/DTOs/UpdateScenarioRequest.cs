using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using MyApp.Domain.Entities;

namespace MyApp.Application.DTOs
{
    public class UpdateScenarioRequest
    {
        [Required]
        public Guid Id { get; set; }

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

        public List<UpdateScenarioStepRequest> Steps { get; set; } = new List<UpdateScenarioStepRequest>();
    }

    public class UpdateScenarioStepRequest
    {
        public Guid? Id { get; set; }

        [Required]
        public ScenarioStepType StepType { get; set; }

        [Range(1, int.MaxValue)]
        public int Sequence { get; set; }

        [Required]
        [StringLength(280)]
        public string Description { get; set; } = string.Empty;
    }
}
