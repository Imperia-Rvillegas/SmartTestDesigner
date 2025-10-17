using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using MyApp.Application.DTOs;
using MyApp.Application.Interfaces;
using MyApp.Domain.Entities;

namespace MyApp.Application.Services
{
    public class WorkspaceSettingService : IWorkspaceSettingService
    {
        private readonly IWorkspaceSettingRepository settingRepository;

        public WorkspaceSettingService(IWorkspaceSettingRepository settingRepository)
        {
            this.settingRepository = settingRepository;
        }

        public async Task<List<WorkspaceSettingDto>> GetAllAsync(CancellationToken cancellationToken)
        {
            List<WorkspaceSetting> settings = await settingRepository.GetAllAsync(cancellationToken);
            return settings.Select(setting => new WorkspaceSettingDto
            {
                Id = setting.Id,
                Key = setting.Key,
                Value = setting.Value
            }).ToList();
        }
    }
}
