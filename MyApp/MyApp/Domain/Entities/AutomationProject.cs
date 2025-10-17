using System;

namespace MyApp.Domain.Entities
{
    public class AutomationProject
    {
        public Guid Id { get; set; }

        public string Name { get; set; } = string.Empty;

        public string RepositoryUrl { get; set; } = string.Empty;

        public string Framework { get; set; } = string.Empty;

        public DateTime LastSyncedUtc { get; set; }
    }
}
