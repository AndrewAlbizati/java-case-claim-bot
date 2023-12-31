package com.github.AndrewAlbizati.events.button.claim;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.exceptions.claim.ActiveClaimNotFoundException;
import com.github.AndrewAlbizati.models.ActiveClaim;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.sql.SQLException;

public class UnclaimButtonClicked implements ButtonClickListener {
    private final Bot bot;
    public UnclaimButtonClicked(Bot bot) {
        this.bot = bot;
    }
    @Override
    public void onButtonClick(ButtonClickEvent buttonEvent) {
        if (!buttonEvent.getButtonInteraction().getCustomId().equals("unclaim")) {
            return;
        }

        // Remove activeclaim from the database
        try {
            ActiveClaim c = ActiveClaim.fromId(bot.getConnection(), buttonEvent.getButtonInteraction().getMessage().getId())
                    .orElseThrow(() -> new ActiveClaimNotFoundException("ActiveClaim not found, please check the message ID"));

            c.removeFromDatabase(bot.getConnection());
        } catch (SQLException | ActiveClaimNotFoundException e) {
            e.printStackTrace();
            buttonEvent.getInteraction().createImmediateResponder()
                    .setContent("There was an error, please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }

        // Delete claim message
        buttonEvent.getButtonInteraction().getMessage().delete();
        buttonEvent.getButtonInteraction().acknowledge();
    }
}
