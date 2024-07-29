package xyz.tcbuildmc.minecraft.plugin.velocityhub.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import xyz.tcbuildmc.common.config.v0.api.SimpleConfigApi;
import xyz.tcbuildmc.common.config.v0.api.parser.DefaultParsers;
import xyz.tcbuildmc.common.util.simpletools.i18n.Translations;
import xyz.tcbuildmc.minecraft.plugin.velocityhub.VelocityHub;
import xyz.tcbuildmc.minecraft.plugin.velocityhub.config.VelocityHubConfig;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CommandHub {
    public static Map<String, Integer> hubStat = new LinkedHashMap<>();
    public static final File exportFile = VelocityHub.getInstance().getDataDir().resolve("hubStat").resolve("export.toml").toFile();

    public static BrigadierCommand registerHub() {
        return new BrigadierCommand(BrigadierCommand.literalArgumentBuilder("hub")
                .requires(source -> source.hasPermission("velocityhub.command.hub"))
                .executes(context -> {
                    VelocityHubConfig config = VelocityHub.getInstance().getConfig();

                    if (config.isEnableHub()) {
                        CommandSource source = context.getSource();

                        if (source instanceof Player player) {
                            if (!hubStat.containsKey(player.getUsername())) {
                                hubStat.put(player.getUsername(), 1);
                            } else {
                                hubStat.merge(player.getUsername(),
                                        hubStat.get(player.getUsername()) + 1,
                                        (a, b) -> b);
                            }

                            ProxyServer server = VelocityHub.getInstance().getServer();
                            Optional<RegisteredServer> optionalLobby = server.getServer(config.getHubServer());

                            if (optionalLobby.isPresent()) {
                                RegisteredServer lobby = optionalLobby.get();
                                player.createConnectionRequest(lobby).fireAndForget();

                                source.sendMessage(Component.text(Translations.getTranslations("command.hub.message.transferred")));
                            } else {
                                source.sendMessage(Component.text(Translations.getTranslations("command.hub.message.no_hub")));
                            }
                        } else {
                            source.sendMessage(Component.text(Translations.getTranslations("command.hub.message.console")));
                        }
                    }

                    return 0;
                })
                .build());
    }

    public static BrigadierCommand registerLobby() {
        return new BrigadierCommand(BrigadierCommand.literalArgumentBuilder("lobby").redirect(registerHub().getNode()));
    }

    public static BrigadierCommand registerHubQuery() {
        return new BrigadierCommand(BrigadierCommand.literalArgumentBuilder("hubQuery")
                .requires(source -> source.hasPermission("velocityhub.command.hubQuery"))
                .executes(context -> {
                    VelocityHubConfig config = VelocityHub.getInstance().getConfig();

                    if (config.isEnableHubQuery()) {
                        CommandSource source = context.getSource();

                        if (source instanceof Player player) {
                            source.sendMessage(Component.text(
                                    Translations.getTranslations("command.hubQuery.message.time")
                                            .formatted(player.getUsername(), hubStat.getOrDefault(player.getUsername(), 0))));
                        }
                    }

                    return 0;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            ProxyServer server = VelocityHub.getInstance().getServer();
                            for (Player player : server.getAllPlayers()) {
                                builder.suggest(player.getUsername());
                            }

                            return builder.buildFuture();
                        })
                        .requires(source -> source.hasPermission("velocityhub.command.hubQuery.other"))
                        .executes(context -> {
                            VelocityHubConfig config = VelocityHub.getInstance().getConfig();

                            if (config.isEnableHubQuery()) {
                                CommandSource source = context.getSource();
                                String player = context.getArgument("player", String.class);

                                source.sendMessage(Component.text(
                                        Translations.getTranslations("command.hubQuery.message.time")
                                                .formatted(player, hubStat.getOrDefault(player, 0))));
                            }

                            return 0;
                        }))
                .build());
    }

    public static BrigadierCommand registerHubStat() {
        return new BrigadierCommand(BrigadierCommand.literalArgumentBuilder("hubStat")
                .then(BrigadierCommand.literalArgumentBuilder("import")
                        .requires(source -> source.hasPermission("velocityhub.command.hubStat.import"))
                        .executes(context -> {
                            VelocityHubConfig config = VelocityHub.getInstance().getConfig();

                            if (config.isEnableHubStat()) {
                                CommandSource source = context.getSource();
                                importStat();

                                source.sendMessage(Component.text(Translations.getTranslations("command.hubStat.import.message")));
                            }

                            return 0;
                        }))
                .then(BrigadierCommand.literalArgumentBuilder("export")
                        .requires(source -> source.hasPermission("velocityhub.command.hubStat.export"))
                        .executes(context -> {
                            VelocityHubConfig config = VelocityHub.getInstance().getConfig();

                            if (config.isEnableHubStat()) {
                                CommandSource source = context.getSource();
                                exportStat();

                                source.sendMessage(Component.text(Translations.getTranslations("command.hubStat.export.message")));
                            }

                            return 0;
                        }))
                .then(BrigadierCommand.literalArgumentBuilder("reset")
                        .requires(source -> source.hasPermission("velocityhub.command.hubStat.reset"))
                        .executes(context -> {
                            VelocityHubConfig config = VelocityHub.getInstance().getConfig();

                            if (config.isEnableHubStat()) {
                                resetStat();

                                context.getSource().sendMessage(Component.text(Translations.getTranslations("command.hubStat.reset.message")));
                            }

                            return 0;
                        }))
                .build());
    }

    public static void importStat() {
        VelocityHubConfig config = VelocityHub.getInstance().getConfig();
        if (config.isEnableHubStat()) {
            if (!exportFile.exists()) {
                if (!exportFile.getParentFile().exists()) {
                    exportFile.getParentFile().mkdirs();
                }

                try {
                    exportFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create new export file.", e);
                }
            }


            hubStat = SimpleConfigApi.getInstance().read(LinkedHashMap.class,
                    exportFile,
                    DefaultParsers.toml4j(false));
        }
    }

    public static void exportStat() {
        VelocityHubConfig config = VelocityHub.getInstance().getConfig();
        if (config.isEnableHubStat()) {
            SimpleConfigApi.getInstance().write(Map.class,
                    hubStat,
                    exportFile,
                    DefaultParsers.toml4j(false));
        }
    }

    public static void resetStat() {
        hubStat = new LinkedHashMap<>();
        exportStat();
        importStat();
    }
}
