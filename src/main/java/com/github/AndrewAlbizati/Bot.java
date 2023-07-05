package com.github.AndrewAlbizati;

import com.github.AndrewAlbizati.commands.ClaimCommand;
import com.github.AndrewAlbizati.commands.JoinCommand;
import com.github.AndrewAlbizati.commands.PingCommand;
import com.github.AndrewAlbizati.commands.admin.ActivateUser;
import com.github.AndrewAlbizati.commands.admin.DeactivateUser;
import com.github.AndrewAlbizati.events.button.check.CheckButtonClicked;
import com.github.AndrewAlbizati.events.button.check.PingButtonClicked;
import com.github.AndrewAlbizati.events.button.claim.CompleteButtonClicked;
import com.github.AndrewAlbizati.events.button.claim.UnclaimButtonClicked;
import com.github.AndrewAlbizati.events.button.ping.CloseAndCheckButtonClicked;
import com.github.AndrewAlbizati.events.button.ping.CloseAndPingButtonClicked;
import com.github.AndrewAlbizati.events.button.ping.CloseAndResolveButtonClicked;
import com.github.AndrewAlbizati.events.button.ping.ResolveButtonClicked;
import com.github.AndrewAlbizati.events.modal.PingModalSubmit;
import com.github.AndrewAlbizati.events.modal.ResolveModalSubmit;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.awt.*;
import java.sql.Connection;
import java.util.Collections;

public class Bot {
    private final String token;
    private final Color primaryEmbedColor;
    private final Color successEmbedColor;
    private final Color dangerEmbedColor;
    private final long claimChannelId;
    private final long checkChannelId;

    private DiscordApi api;
    private final Connection connection;

    public Bot(String token, Color primaryEmbedColor, Color successEmbedColor, Color dangerEmbedColor, long claimChannelId, long checkChannelId, Connection conn) {
        this.token = token;

        this.primaryEmbedColor = primaryEmbedColor;
        this.successEmbedColor = successEmbedColor;
        this.dangerEmbedColor = dangerEmbedColor;

        this.claimChannelId = claimChannelId;
        this.checkChannelId = checkChannelId;
        this.connection = conn;
    }

    public DiscordApi getApi() {
        return api;
    }

    public Connection getConnection() {
        return connection;
    }

    public Color getPrimaryEmbedColor() {
        return primaryEmbedColor;
    }

    public Color getSuccessEmbedColor() {
        return successEmbedColor;
    }

    public Color getDangerEmbedColor() {
        return dangerEmbedColor;
    }
    public long getClaimChannelId() {
        return claimChannelId;
    }

    public long getCheckChannelId() {
        return checkChannelId;
    }

    /**
     * Starts the Discord bot and initializes commands
     */
    public void run() {
        // Start Discord bot
        api = new DiscordApiBuilder().setToken(token).login().join();
        System.out.println("Logged in as " + api.getYourself().getDiscriminatedName());

        // Create slash command (may take a few minutes to update on Discord)
        SlashCommand.with("ping", "Pings the bot.").createGlobal(api).join();
        SlashCommand.with("claim", "Claim a case.",
                Collections.singletonList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "case_num", "Case #", true)
                )).createGlobal(api).join();

        SlashCommand.with("join", "Creates a user.")
                .setDefaultEnabledForPermissions(PermissionType.DEAFEN_MEMBERS)
                .createGlobal(api)
                .join();

        SlashCommand.with("activateuser", "Activates a user.")
                .setDefaultEnabledForPermissions(PermissionType.BAN_MEMBERS)
                .createGlobal(api)
                .join();
        SlashCommand.with("deactivateuser", "Deactivates a user.").createGlobal(api).join();

        // Create slash command listeners
        api.addSlashCommandCreateListener(new PingCommand(this));
        api.addSlashCommandCreateListener(new ClaimCommand(this));
        api.addSlashCommandCreateListener(new JoinCommand(this));

        api.addSlashCommandCreateListener(new ActivateUser(this));
        api.addSlashCommandCreateListener(new DeactivateUser(this));

        // Add button click listeners
        api.addButtonClickListener(new CompleteButtonClicked(this));
        api.addButtonClickListener(new UnclaimButtonClicked(this));

        api.addButtonClickListener(new CheckButtonClicked(this));
        api.addButtonClickListener(new PingButtonClicked(this));

        api.addButtonClickListener(new ResolveButtonClicked(this));

        api.addButtonClickListener(new CloseAndResolveButtonClicked(this));
        api.addButtonClickListener(new CloseAndPingButtonClicked(this));
        api.addButtonClickListener(new CloseAndCheckButtonClicked(this));

        // Add modal listeners
        api.addModalSubmitListener(new PingModalSubmit(this));
        api.addModalSubmitListener(new ResolveModalSubmit(this));
    }
}