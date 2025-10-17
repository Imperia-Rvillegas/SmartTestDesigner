using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Serilog;
using Serilog.Events;
using MyApp.Application.Interfaces;
using MyApp.Application.Services;
using MyApp.Infrastructure.Data;
using MyApp.Infrastructure.Repositories;

namespace MyApp
{
    public class Program
    {
        public static async Task Main(string[] args)
        {
            WebApplicationBuilder builder = WebApplication.CreateBuilder(args);

            ConfigureLogging(builder);

            builder.Services.AddControllersWithViews();
            builder.Services.AddRouting(options => options.LowercaseUrls = true);
            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            string? connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
            builder.Services.AddDbContext<ApplicationDbContext>(options =>
            {
                if (!string.IsNullOrWhiteSpace(connectionString) && !connectionString.Contains("localdb", StringComparison.OrdinalIgnoreCase))
                {
                    options.UseSqlServer(connectionString);
                }
                else
                {
                    options.UseInMemoryDatabase("SmartTestDesigner");
                }
            });

            builder.Services.AddScoped<IScenarioRepository, ScenarioRepository>();
            builder.Services.AddScoped<ILocatorRepository, LocatorRepository>();
            builder.Services.AddScoped<IProjectRepository, ProjectRepository>();
            builder.Services.AddScoped<IExecutionRepository, ExecutionRepository>();
            builder.Services.AddScoped<IIntegrationRepository, IntegrationRepository>();
            builder.Services.AddScoped<IWorkspaceSettingRepository, WorkspaceSettingRepository>();

            builder.Services.AddScoped<IScenarioService, ScenarioService>();
            builder.Services.AddScoped<ILocatorService, LocatorService>();
            builder.Services.AddScoped<IProjectService, ProjectService>();
            builder.Services.AddScoped<IExecutionService, ExecutionService>();
            builder.Services.AddScoped<IIntegrationService, IntegrationService>();
            builder.Services.AddScoped<IWorkspaceSettingService, WorkspaceSettingService>();
            builder.Services.AddScoped<IWorkspaceSummaryService, WorkspaceSummaryService>();

            WebApplication app = builder.Build();

            using (IServiceScope scope = app.Services.CreateScope())
            {
                ApplicationDbContext context = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
                await DatabaseInitializer.InitialiseAsync(context, CancellationToken.None);
            }

            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }
            else
            {
                app.UseExceptionHandler("/Home/Error");
                app.UseHsts();
            }

            app.UseHttpsRedirection();
            app.UseRouting();
            app.UseAuthorization();

            app.MapControllers();

            app.MapStaticAssets();
            app.MapControllerRoute(
                name: "default",
                pattern: "{controller=Home}/{action=Index}/{id?}")
                .WithStaticAssets();

            await app.RunAsync();
        }

        private static void ConfigureLogging(WebApplicationBuilder builder)
        {
            LoggerConfiguration loggerConfiguration = new LoggerConfiguration()
                .MinimumLevel.Information()
                .MinimumLevel.Override("Microsoft", LogEventLevel.Warning)
                .MinimumLevel.Override("Microsoft.Hosting.Lifetime", LogEventLevel.Information)
                .Enrich.FromLogContext()
                .WriteTo.Console();

            Log.Logger = loggerConfiguration.CreateLogger();
            builder.Host.UseSerilog();
        }
    }
}
