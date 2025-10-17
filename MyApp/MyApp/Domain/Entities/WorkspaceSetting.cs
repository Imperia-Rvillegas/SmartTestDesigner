using System;

namespace MyApp.Domain.Entities
{
    public class WorkspaceSetting
    {
        public Guid Id { get; set; }

        public string Key { get; set; } = string.Empty;

        public string Value { get; set; } = string.Empty;
    }
}
