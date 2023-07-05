package com.github.AndrewAlbizati.events.button.claim;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.exceptions.claim.ActiveClaimNotFoundException;
import com.github.AndrewAlbizati.models.ActiveClaim;
import com.github.AndrewAlbizati.models.CompletedClaim;
import org.javacord.api.entity.channel.TextChannel;
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

        // Remove ActiveClaim from the database
        try {
            ActiveClaim activeClaim = ActiveClaim.fromId(bot.getConnection(), buttonEvent.getButtonInteraction().getMessage().getId())
                    .orElseThrow(() -> new ActiveClaimNotFoundException("ActiveClaim not found, please check the message ID"));

            activeClaim.removeFromDatabase(bot.getConnection());

            // Delete claim message
            buttonEvent.getButtonInteraction().getMessage().delete();

            // Create claim embed
            EmbedBuilder eb1 = new EmbedBuilder()
                    .setDescription("Has been marked complete by " + buttonEvent.getInteraction().getUser().getMentionTag())
                    .setFooter("Completed")
                    .setAuthor(activeClaim.caseNum(), null, buttonEvent.getInteraction().getUser().getAvatar().getUrl().toString())
                    .setColor(bot.getPrimaryEmbedColor())
                    .setTimestamp(Instant.now());

            // Send complete message
            buttonEvent.getButtonInteraction().createImmediateResponder()
                    .addEmbed(eb1)
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();

            TextChannel claimChannel = bot.getApi().getTextChannelById(bot.getCheckChannelId()).get();

            // Create checker embed
            EmbedBuilder eb2 = new EmbedBuilder()
                    .setDescription("Has been marked as complete by <@!" + activeClaim.tech().discordId() + ">")
                    .setFooter("Completed")
                    .setAuthor(activeClaim.caseNum(), null, buttonEvent.getInteraction().getUser().getAvatar().getUrl().toString())
                    .setColor(bot.getPrimaryEmbedColor())
                    .setTimestamp(Instant.now());

            // Send Checker message
            claimChannel.sendMessage(eb2, ActionRow.of(
                    Button.success("check", "Check"),
                    Button.danger("ping", "Ping"))
            ).whenCompleteAsync((msg, ex) -> {
                // Create new CompletedClaim, store in database
                CompletedClaim completedClaim = new CompletedClaim(msg.getId(), activeClaim.caseNum(), activeClaim.tech(), activeClaim.claimTime(), Timestamp.valueOf(LocalDateTime.now()));
                try {
                    completedClaim.addToDatabase(bot.getConnection());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (ActiveClaimNotFoundException e) {
            // Claim couldn't be found
            e.printStackTrace();
            buttonEvent.getInteraction().createImmediateResponder()
                    .setContent("Case not found, please contact the bot developers.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        } catch (SQLException e) {
            // Error removing the ActiveClaim from the database
            e.printStackTrace();
            buttonEvent.getInteraction().createImmediateResponder()
                    .setContent("There was an error, please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
