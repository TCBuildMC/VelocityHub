package xyz.tcbuildmc.minecraft.plugin.velocityhub.command;

import com.velocitypowered.api.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import xyz.tcbuildmc.common.util.simpletools.base.ObjectUtils;
import xyz.tcbuildmc.common.util.simpletools.i18n.Translations;
import xyz.tcbuildmc.minecraft.plugin.velocityhub.VelocityHub;

public class VelocityHubCommand {
    public static BrigadierCommand register() {
        return new BrigadierCommand(BrigadierCommand.literalArgumentBuilder("velocityhub")
                .then(BrigadierCommand.literalArgumentBuilder("version")
                        .executes(context -> {
                            context.getSource().sendMessage(Component.text(Translations.getTranslations("command.version")
                                    .formatted(ObjectUtils.requiresNonNullOrElse(
                                            VelocityHubCommand.class.getPackage().getImplementationVersion(),
                                            "0.0.0+unknown.0"))));

                            return 0;
                        }))
                .then(BrigadierCommand.literalArgumentBuilder("help")
                        .requires(source -> source.hasPermission("velocityhub.command.help"))
                        .executes(context -> {context.getSource().sendMessage(Component.text(Translations.getTranslations("command.help")));

                            return 0;
                        }))
                .then(BrigadierCommand.literalArgumentBuilder("reload")
                        .requires(source -> source.hasPermission("velocityhub.command.reload"))
                        .executes(context -> {context.getSource().sendMessage(Component.text(Translations.getTranslations("command.reload")));

                            VelocityHub.getInstance().reload();
                            return 0;
                        }))
                .build());
    }
}
