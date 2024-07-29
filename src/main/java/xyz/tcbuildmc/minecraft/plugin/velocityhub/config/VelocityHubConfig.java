package xyz.tcbuildmc.minecraft.plugin.velocityhub.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VelocityHubConfig {
    private final String version = "3";
    private boolean enableHub = true;
    private boolean enableHubQuery = true;
    private boolean enableHubStat = true;
    private boolean autoResetStat = false;
    private String hubServer = "lobby";
}
