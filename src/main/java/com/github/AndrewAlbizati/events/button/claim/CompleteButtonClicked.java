package com.github.AndrewAlbizati.events.button.claim;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.models.ActiveClaim;
import com.github.AndrewAlbizati.models.CompletedClaim;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

public class CompleteButtonClicked implements ButtonClickListener {
    private final Bot bot;
    public CompleteButtonClicked(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onButtonClick(ButtonClickEvent buttonEvent) {
        if (!buttonEvent.getButtonInteraction().getCustomId().equals("complete")) {
            return;
        }

        // Remove from ActiveClaims table
        ActiveClaim activeClaim = ActiveClaim.fromId(bot.getConnection(), buttonEvent.getButtonInteraction().getMessage().getId());

        // Remove ActiveClaim from the database
        try {
            activeClaim.removeFromDatabase(bot.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
            buttonEvent.getInteraction().createImmediateResponder()
                    .setContent("There was an error, please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }

        // Delete claim message
        buttonEvent.getButtonInteraction().getMessage().delete();

        // Send complete message
        buttonEvent.getButtonInteraction().createImmediateResponder()
                .setContent("Complete!")
                .setFlags(MessageFlag.EPHEMERAL)
                .respond();

        TextChannel claimChannel = bot.getApi().getTextChannelById(bot.getCheckChannelId()).get();

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(activeClaim.caseNum())
                .setDescription("Has been marked as complete by <@!" + activeClaim.tech().discordId() + ">")
                .setFooter("Completed")
                .setTimestamp(Instant.now());

        // Send message
        try {
            Message m = claimChannel.sendMessage(eb, ActionRow.of(
                    Button.success("check", "Check"),
                    Button.danger("ping", "Ping"))).get();

            CompletedClaim completedClaim = new CompletedClaim(m.getId(), activeClaim.caseNum(), activeClaim.tech(), activeClaim.claimTime(), Timestamp.valueOf(LocalDateTime.now()));
            completedClaim.addToDatabase(bot.getConnection());
        } catch (SQLException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
