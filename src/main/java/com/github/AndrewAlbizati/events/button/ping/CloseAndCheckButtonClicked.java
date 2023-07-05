package com.github.AndrewAlbizati.events.button.ping;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.enums.Status;
import com.github.AndrewAlbizati.exceptions.PingNotFoundException;
import com.github.AndrewAlbizati.exceptions.claim.CheckedClaimNotFoundException;
import com.github.AndrewAlbizati.models.CheckedClaim;
import com.github.AndrewAlbizati.models.Ping;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

import java.sql.SQLException;

public class CloseAndCheckButtonClicked implements ButtonClickListener {
    private final Bot bot;
    public CloseAndCheckButtonClicked(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onButtonClick(ButtonClickEvent buttonEvent) {
        if (!buttonEvent.getButtonInteraction().getCustomId().equals("close-checked")) {
            return;
        }

        try {
            ServerThreadChannel stc = buttonEvent.getButtonInteraction().getMessage().getServerThreadChannel()
                    .orElseThrow(() -> new CheckedClaimNotFoundException("Ping thread couldn't be found, please ensure that this button was clicked in a valid ServerThreadChannel"));
            CheckedClaim checkedClaim = CheckedClaim.fromPingThreadId(bot.getConnection(), stc.getId())
                    .orElseThrow(() -> new CheckedClaimNotFoundException("CheckedClaim not found, please check the ping thread ID"));
            checkedClaim.changeStatus(bot.getConnection(), Status.CHECKED);

            Ping p = Ping.fromThreadId(bot.getConnection(), checkedClaim.pingThreadId())
                    .orElseThrow(() -> new PingNotFoundException());
            p.removeFromDatabase(bot.getConnection());

            stc.removeThreadMember(checkedClaim.tech().discordId());
            stc.removeThreadMember(checkedClaim.lead().discordId());

            buttonEvent.getButtonInteraction().acknowledge();
        } catch (SQLException | CheckedClaimNotFoundException | PingNotFoundException e) {
            e.printStackTrace();
            buttonEvent.getButtonInteraction().createImmediateResponder()
                    .setContent("Error! Please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
