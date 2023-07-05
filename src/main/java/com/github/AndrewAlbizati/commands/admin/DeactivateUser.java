package com.github.AndrewAlbizati.commands.admin;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.exceptions.UserNotFoundException;
import com.github.AndrewAlbizati.models.User;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class DeactivateUser implements SlashCommandCreateListener {
    private final Bot bot;

    public DeactivateUser(Bot bot) {
        this.bot = bot;
    }

    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        // Ignore other slash commands
        if (!interaction.getCommandName().equalsIgnoreCase("deactivateuser")) {
            return;
        }

        try {
            User user = User.fromId(bot.getConnection(), interaction.getUser().getId())
                    .orElseThrow(() -> new UserNotFoundException());

            if (user != null && user.deactivate(bot.getConnection())) {
                interaction.createImmediateResponder()
                        .setContent("👍")
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            }
        } catch (UserNotFoundException e) {
            interaction.createImmediateResponder()
                    .setContent("Error! Please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}