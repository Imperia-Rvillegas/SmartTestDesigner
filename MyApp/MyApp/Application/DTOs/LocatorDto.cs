using System;
using System.Collections.Generic;

namespace MyApp.Application.DTOs
{
    public class LocatorDto
    {
        public Guid Id { get; set; }

        public string Name { get; set; } = string.Empty;

        public string Selector { get; set; } = string.Empty;

        public double ConfidencePercentage { get; set; }

        public string ElementType { get; set; } = string.Empty;

        public List<string> AlternativeSelectors { get; set; } = new List<string>();
    }
}
