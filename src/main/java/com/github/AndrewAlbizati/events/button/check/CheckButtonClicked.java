package com.github.AndrewAlbizati.events.button.check;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.enums.Status;
import com.github.AndrewAlbizati.exceptions.CheckerMessageNotFoundException;
import com.github.AndrewAlbizati.models.CheckedClaim;
import com.github.AndrewAlbizati.models.CompletedClaim;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class CheckButtonClicked implements ButtonClickListener {
    private final Bot bot;
    public CheckButtonClicked(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onButtonClick(ButtonClickEvent buttonEvent) {
        if (!buttonEvent.getButtonInteraction().getCustomId().equals("check")) {
            return;
        }

        try {
            CompletedClaim completedClaim = CompletedClaim.fromId(bot.getConnection(), buttonEvent.getButtonInteraction().getMessage().getId());
            CheckedClaim checkedClaim = new CheckedClaim(
                    bot.getConnection(),
                    completedClaim.checkerMessageId(),
                    completedClaim.caseNum(),
                    completedClaim.tech().discordId(),
                    buttonEvent.getInteraction().getUser().getId(),
                    completedClaim.claimTime(),
                    completedClaim.completeTime(),
                    Timestamp.from(Instant.now()),
                    Status.CHECKED,
                    null
            );

            completedClaim.removeFromDatabase(bot.getConnection());
            checkedClaim.addToDatabase(bot.getConnection());

            TextChannel checkChannel = bot.getApi().getTextChannelById(bot.getCheckChannelId()).get();

            Message m = bot.getApi().getMessageById(completedClaim.checkerMessageId(), checkChannel).get();
            m.delete();

            buttonEvent.getButtonInteraction().acknowledge();
        } catch (CheckerMessageNotFoundException | SQLException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            buttonEvent.getInteraction().createImmediateResponder()
                    .setContent("Error!")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
