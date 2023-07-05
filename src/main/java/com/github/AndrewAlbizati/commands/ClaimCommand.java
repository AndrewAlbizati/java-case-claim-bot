package com.github.AndrewAlbizati.commands;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.exceptions.claim.ClaimNotFoundException;
import com.github.AndrewAlbizati.exceptions.InvalidCaseNumberException;
import com.github.AndrewAlbizati.exceptions.claim.ActiveClaimNotFoundException;
import com.github.AndrewAlbizati.models.ActiveClaim;
import com.github.AndrewAlbizati.models.User;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ClaimCommand implements SlashCommandCreateListener {
    private final Bot bot;

    public ClaimCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        // Ignore other slash commands
        if (!interaction.getCommandName().equalsIgnoreCase("claim")) {
            return;
        }

        String caseNum = interaction.getOptionByIndex(0).orElseThrow().getStringValue().orElseThrow();

        // Invalid case number
        if (caseNum.length() != 8) {
            interaction.createImmediateResponder()
                    .setContent("Error with case length")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }

        // Test if case has already been claimed
        try {
            ActiveClaim c = ActiveClaim.fromCaseNum(bot.getConnection(), caseNum)
                    .orElseThrow(() -> new ActiveClaimNotFoundException("ActiveClaim not found, please check the case number"));
            interaction.createImmediateResponder()
                    .setContent("Case **" + c.caseNum() + "** has already been claimed by <@!" + c.tech().discordId() + ">.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        } catch (InvalidCaseNumberException e) {
            interaction.createImmediateResponder()
                    .setContent("Error with case length")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        } catch (ClaimNotFoundException ignored) {}

        Optional<User> u = User.fromId(bot.getConnection(), interaction.getUser().getId());
        if (u.isEmpty()) {
            interaction.createImmediateResponder()
                    .setContent("Please use the /join command to get started.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }

        // Create embed
        EmbedBuilder eb = new EmbedBuilder()
                .setDescription("Is being worked on by " + interaction.getUser().getMentionTag())
                .setFooter("Claimed")
                .setAuthor(caseNum, null, interaction.getUser().getAvatar().getUrl().toString())
                .setColor(bot.getPrimaryEmbedColor())
                .setTimestamp(Instant.now());

        // Send message
        try {
            Message m = interaction.createImmediateResponder()
                    .addEmbed(eb)
                    .addComponents(ActionRow.of(
                            Button.success("complete", "Complete"),
                            Button.danger("unclaim", "Unclaim")))
                    .respond().get().update().get();

            // Create a new ActiveClaim, add to database
            ActiveClaim claim = new ActiveClaim(bot.getConnection(), m.getId(), caseNum, interaction.getUser().getId(), Timestamp.valueOf(LocalDateTime.now()));
            claim.addToDatabase(bot.getConnection());
        } catch (SQLException | ExecutionException | InterruptedException e) {
            // Error sending message
            interaction.createImmediateResponder()
                    .setContent("Error claiming the case!")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            throw new RuntimeException(e);
        }
    }
}