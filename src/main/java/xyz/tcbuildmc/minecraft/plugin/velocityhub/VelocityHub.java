package xyz.tcbuildmc.minecraft.plugin.velocityhub;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import xyz.tcbuildmc.common.config.api.ConfigApi;
import xyz.tcbuildmc.common.config.api.parser.DefaultParsers;
import xyz.tcbuildmc.common.i18n.Translations;
import xyz.tcbuildmc.minecraft.plugin.velocityhub.command.CommandHub;
import xyz.tcbuildmc.minecraft.plugin.velocityhub.command.VelocityHubCommand;
import xyz.tcbuildmc.minecraft.plugin.velocityhub.config.VelocityHubConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

@Plugin(
        id = "velocityhub",
        name = "VelocityHub",
        version = BuildConstants.VERSION
)
public class VelocityHub {
    @Getter
    private static VelocityHub instance;

    @Inject
    @Getter
    private Logger logger;

    @Inject
    @Getter
    private ProxyServer server;

    @Inject
    @DataDirectory
    @Getter
    private Path dataDir;

    @Getter
    private VelocityHubConfig config;
    @Getter
    private File configFile;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        this.configFile = this.dataDir.resolve("config.toml").toFile();

        if (!this.configFile.exists()) {
            this.config = new VelocityHubConfig();

            if (!this.configFile.getParentFile().exists()) {
                this.configFile.getParentFile().mkdirs();
            }

            ConfigApi.getInstance().write(VelocityHubConfig.class,
                    this.config,
                    this.configFile,
                    DefaultParsers.toml4j(false));
        } else {
            this.config = ConfigApi.getInstance().read(VelocityHubConfig.class,
                    this.configFile,
                    DefaultParsers.toml4j(false));
        }

        instance = this;
        Map<String, String> translations = Translations.getTranslationsFromClasspath("lang", Translations.getLocalLanguage(), "json", DefaultParsers.gson());
        Translations.setTranslations(translations);

        if (config.isAutoResetStat()) {
            CommandHub.resetStat();
        } else {
            CommandHub.importStat();
        }
        this.server.getCommandManager().register(VelocityHubCommand.register());
        this.server.getCommandManager().register(CommandHub.registerHub());
        this.server.getCommandManager().register(CommandHub.registerLobby());
        this.server.getCommandManager().register(CommandHub.registerHubQuery());
        this.server.getCommandManager().register(CommandHub.registerHubStat());
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent e) {
        this.reload();
    }

    public void reload() {
        ConfigApi.getInstance().write(VelocityHubConfig.class,
                this.config,
                this.configFile,
                DefaultParsers.toml4j(false));

        this.config = ConfigApi.getInstance().read(VelocityHubConfig.class,
                this.configFile,
                DefaultParsers.toml4j(false));
        Map<String, String> translations = Translations.getTranslationsFromClasspath("lang", Translations.getLocalLanguage(), "json", DefaultParsers.gson());
        Translations.setTranslations(translations);

        CommandHub.exportStat();
        CommandHub.importStat();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        ConfigApi.getInstance().write(VelocityHubConfig.class,
                this.config,
                this.configFile,
                DefaultParsers.toml4j(false));

        CommandHub.exportStat();
    }
}
