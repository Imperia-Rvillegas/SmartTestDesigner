using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using MyApp.Domain.Entities;

namespace MyApp.Infrastructure.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }

        public DbSet<Scenario> Scenarios => Set<Scenario>();

        public DbSet<ScenarioStep> ScenarioSteps => Set<ScenarioStep>();

        public DbSet<LocatorDefinition> Locators => Set<LocatorDefinition>();

        public DbSet<AutomationProject> Projects => Set<AutomationProject>();

        public DbSet<TestExecution> Executions => Set<TestExecution>();

        public DbSet<ExternalIntegration> Integrations => Set<ExternalIntegration>();

        public DbSet<WorkspaceSetting> Settings => Set<WorkspaceSetting>();

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            ConfigureScenario(modelBuilder.Entity<Scenario>());
            ConfigureScenarioStep(modelBuilder.Entity<ScenarioStep>());
            ConfigureLocator(modelBuilder.Entity<LocatorDefinition>());
            ConfigureProject(modelBuilder.Entity<AutomationProject>());
            ConfigureExecution(modelBuilder.Entity<TestExecution>());
            ConfigureIntegration(modelBuilder.Entity<ExternalIntegration>());
            ConfigureSetting(modelBuilder.Entity<WorkspaceSetting>());
        }

        private void ConfigureScenario(EntityTypeBuilder<Scenario> builder)
        {
            builder.HasKey(scenario => scenario.Id);
            builder.Property(scenario => scenario.Name).IsRequired().HasMaxLength(140);
            builder.Property(scenario => scenario.Objective).IsRequired().HasMaxLength(300);
            builder.Property(scenario => scenario.Status).IsRequired().HasMaxLength(40);
            builder.Property(scenario => scenario.Owner).IsRequired().HasMaxLength(40);
            builder.Property(scenario => scenario.LastUpdatedUtc).IsRequired();
            builder.Property(scenario => scenario.Tags)
                .HasConversion(
                    tags => string.Join(';', tags),
                    tags => string.IsNullOrWhiteSpace(tags)
                        ? new List<string>()
                        : new List<string>(tags.Split(';', StringSplitOptions.RemoveEmptyEntries)))
                .HasMaxLength(400);

            builder.HasMany(scenario => scenario.Steps)
                .WithOne(step => step.Scenario)
                .HasForeignKey(step => step.ScenarioId)
                .OnDelete(DeleteBehavior.Cascade);
        }

        private void ConfigureScenarioStep(EntityTypeBuilder<ScenarioStep> builder)
        {
            builder.HasKey(step => step.Id);
            builder.Property(step => step.Description).IsRequired().HasMaxLength(280);
            builder.Property(step => step.StepType).IsRequired();
            builder.Property(step => step.Sequence).IsRequired();
        }

        private void ConfigureLocator(EntityTypeBuilder<LocatorDefinition> builder)
        {
            builder.HasKey(locator => locator.Id);
            builder.Property(locator => locator.Name).IsRequired().HasMaxLength(120);
            builder.Property(locator => locator.Selector).IsRequired().HasMaxLength(200);
            builder.Property(locator => locator.ElementType).IsRequired().HasMaxLength(80);
            builder.Property(locator => locator.ConfidencePercentage).IsRequired();
            builder.Property(locator => locator.AlternativeSelectors)
                .HasConversion(
                    selectors => string.Join('|', selectors),
                    selectors => string.IsNullOrWhiteSpace(selectors)
                        ? new List<string>()
                        : new List<string>(selectors.Split('|', StringSplitOptions.RemoveEmptyEntries)))
                .HasMaxLength(600);
        }

        private void ConfigureProject(EntityTypeBuilder<AutomationProject> builder)
        {
            builder.HasKey(project => project.Id);
            builder.Property(project => project.Name).IsRequired().HasMaxLength(120);
            builder.Property(project => project.RepositoryUrl).IsRequired().HasMaxLength(220);
            builder.Property(project => project.Framework).IsRequired().HasMaxLength(80);
            builder.Property(project => project.LastSyncedUtc).IsRequired();
        }

        private void ConfigureExecution(EntityTypeBuilder<TestExecution> builder)
        {
            builder.HasKey(execution => execution.Id);
            builder.Property(execution => execution.ScenarioId).IsRequired();
            builder.Property(execution => execution.Environment).IsRequired().HasMaxLength(60);
            builder.Property(execution => execution.Status).IsRequired().HasMaxLength(40);
            builder.Property(execution => execution.StartedAtUtc).IsRequired();
        }

        private void ConfigureIntegration(EntityTypeBuilder<ExternalIntegration> builder)
        {
            builder.HasKey(integration => integration.Id);
            builder.Property(integration => integration.Name).IsRequired().HasMaxLength(140);
            builder.Property(integration => integration.Type).IsRequired().HasMaxLength(80);
            builder.Property(integration => integration.Status).IsRequired().HasMaxLength(40);
            builder.Property(integration => integration.ConnectedAtUtc).IsRequired();
        }

        private void ConfigureSetting(EntityTypeBuilder<WorkspaceSetting> builder)
        {
            builder.HasKey(setting => setting.Id);
            builder.Property(setting => setting.Key).IsRequired().HasMaxLength(120);
            builder.Property(setting => setting.Value).IsRequired().HasMaxLength(200);
        }
    }
}
