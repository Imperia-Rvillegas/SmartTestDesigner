using System;

namespace MyApp.Domain.Entities
{
    public class ExternalIntegration
    {
        public Guid Id { get; set; }

        public string Name { get; set; } = string.Empty;

        public string Type { get; set; } = string.Empty;

        public string Status { get; set; } = string.Empty;

        public DateTime ConnectedAtUtc { get; set; }
    }
}
