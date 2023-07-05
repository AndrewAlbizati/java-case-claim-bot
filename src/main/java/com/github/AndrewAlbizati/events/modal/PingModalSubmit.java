package com.github.AndrewAlbizati.events.modal;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.enums.Status;
import com.github.AndrewAlbizati.exceptions.claim.CompletedClaimNotFoundException;
import com.github.AndrewAlbizati.models.CheckedClaim;
import com.github.AndrewAlbizati.models.CompletedClaim;
import com.github.AndrewAlbizati.models.Ping;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannelBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ModalSubmitEvent;
import org.javacord.api.listener.interaction.ModalSubmitListener;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PingModalSubmit implements ModalSubmitListener {
    private final Bot bot;
    public PingModalSubmit(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onModalSubmit(ModalSubmitEvent submitEvent) {
        if (!submitEvent.getModalInteraction().getCustomId().startsWith("ping-modal")) {
            return;
        }
        try {
            // Find completedClaim
            long messageId = Long.parseLong(submitEvent.getModalInteraction().getCustomId().split("-")[2]);
            CompletedClaim completedClaim = CompletedClaim.fromId(bot.getConnection(), messageId)
                    .orElseThrow(() -> new CompletedClaimNotFoundException("CompletedClaim not found, please check the message ID"));

            // Collect inputs
            List<String> inputs = submitEvent.getModalInteraction().getTextInputValues();
            String severity = inputs.get(0);
            String description = inputs.get(1);
            String todo = inputs.size() > 2 ? inputs.get(2) : "Please review these details and let us know if you have any questions!";

            // Create embed
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle(completedClaim.caseNum())
                    .setDescription("<@!" + completedClaim.tech().discordId() + ">, this case has been pinged by <@!" + submitEvent.getModalInteraction().getUser().getId() + ">.")
                    .addField("Reason", description, false)
                    .addField("To Do", todo, false)
                    .setColor(bot.getDangerEmbedColor())
                    .setFooter(severity + " severity level")
                    .setTimestamp(Instant.now());

            // Create thread
            ServerTextChannel stc = bot.getApi().getServerTextChannelById(bot.getClaimChannelId())
                    .orElseThrow();
            ServerThreadChannelBuilder stcb = new ServerThreadChannelBuilder(stc, ChannelType.SERVER_PRIVATE_THREAD, completedClaim.caseNum());
            stcb.create().whenCompleteAsync((thread, ex1) -> {
                // Add members to thread
                thread.addThreadMember(submitEvent.getModalInteraction().getUser().getId());
                thread.addThreadMember(completedClaim.tech().discordId());

                // Send message
                thread.sendMessage(eb, ActionRow.of(
                        Button.primary("resolve", "Resolve"))
                ).whenCompleteAsync((msg, ex2) -> {
                    // Create CheckedClaim object
                    CheckedClaim checkedClaim = new CheckedClaim(
                            bot.getConnection(),
                            msg.getId(),
                            completedClaim.caseNum(),
                            completedClaim.tech().discordId(),
                            submitEvent.getModalInteraction().getUser().getId(),
                            completedClaim.claimTime(),
                            completedClaim.completeTime(),
                            Timestamp.from(Instant.now()),
                            Status.PINGED,
                            thread.getId()
                    );

                    // Create Ping object
                    Ping ping = new Ping(
                            thread.getId(),
                            msg.getId(),
                            severity,
                            description
                    );

                    // Add Ping and CheckedClaim to DB, remove CompletedClaim from DB
                    try {
                        ping.addToDatabase(bot.getConnection());
                        checkedClaim.addToDatabase(bot.getConnection());
                        completedClaim.removeFromDatabase(bot.getConnection());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Create response, delete it
                    submitEvent.getModalInteraction().createImmediateResponder()
                            .setContent("👍")
                            .setFlags(MessageFlag.EPHEMERAL)
                            .respond().whenCompleteAsync((res, ex3) -> res.delete());
                });
            });

            // Delete checker message
            try {
                bot.getApi().getServerTextChannelById(bot.getCheckChannelId()).get().getMessageById(messageId).get().delete();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        } catch (CompletedClaimNotFoundException e) {
            e.printStackTrace();
            submitEvent.getModalInteraction().createImmediateResponder()
                    .setContent("Error! Please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
