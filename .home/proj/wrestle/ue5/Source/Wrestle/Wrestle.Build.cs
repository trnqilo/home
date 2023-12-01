// Source/Wrestle/Wrestle.Build.cs
using UnrealBuildTool;

public class Wrestle : ModuleRules
{
    public Wrestle(ReadOnlyTargetRules Target) : base(Target)
    {
        PCHUsage = PCHUsageMode.UseExplicitOrSharedPCHs;

        PublicDependencyModuleNames.AddRange(new[]
        {
            "Core","CoreUObject","Engine","InputCore","EnhancedInput","NetCore"
        });

        PrivateDependencyModuleNames.AddRange(new[]
        {
            "Slate","SlateCore"
        });
    }
}
