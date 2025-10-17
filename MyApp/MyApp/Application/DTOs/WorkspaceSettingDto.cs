using System;

namespace MyApp.Application.DTOs
{
    public class WorkspaceSettingDto
    {
        public Guid Id { get; set; }

        public string Key { get; set; } = string.Empty;

        public string Value { get; set; } = string.Empty;
    }
}
