package com.github.AndrewAlbizati.events.button.ping;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.enums.Status;
import com.github.AndrewAlbizati.exceptions.claim.CheckedClaimNotFoundException;
import com.github.AndrewAlbizati.models.CheckedClaim;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.sql.SQLException;

public class CloseAndResolveButtonClicked implements ButtonClickListener {
    private final Bot bot;
    public CloseAndResolveButtonClicked(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onButtonClick(ButtonClickEvent buttonEvent) {
        if (!buttonEvent.getButtonInteraction().getCustomId().equals("close-resolved")) {
            return;
        }

        try {
            ServerThreadChannel stc = buttonEvent.getButtonInteraction().getMessage().getServerThreadChannel()
                    .orElseThrow(() -> new CheckedClaimNotFoundException("Ping thread couldn't be found, please ensure that this button was clicked in a valid ServerThreadChannel"));
            CheckedClaim checkedClaim = CheckedClaim.fromPingThreadId(bot.getConnection(), stc.getId())
                    .orElseThrow(() -> new CheckedClaimNotFoundException("CheckedClaim not found, check the ping thread ID"));
            checkedClaim.changeStatus(bot.getConnection(), Status.RESOLVED);

            stc.removeThreadMember(checkedClaim.tech().discordId());
            stc.removeThreadMember(checkedClaim.lead().discordId());

            buttonEvent.getButtonInteraction().acknowledge();
        } catch (SQLException | CheckedClaimNotFoundException e) {
            e.printStackTrace();
            buttonEvent.getButtonInteraction().createImmediateResponder()
                    .setContent("Error! Please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
