package com.github.AndrewAlbizati.events.button.ping;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.enums.Status;
import com.github.AndrewAlbizati.models.CheckedClaim;
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
            long threadId = buttonEvent.getButtonInteraction().getMessage().getServerThreadChannel().get().getId();
            ServerThreadChannel stc = bot.getApi().getServerThreadChannelById(threadId).get();
            CheckedClaim checkedClaim = CheckedClaim.fromPingThreadId(bot.getConnection(), threadId);
            checkedClaim.changeStatus(bot.getConnection(), Status.CHECKED);

            stc.removeThreadMember(checkedClaim.tech().discordId());
            stc.removeThreadMember(checkedClaim.lead().discordId());

            buttonEvent.getButtonInteraction().acknowledge();
        } catch (SQLException e) {
            e.printStackTrace();
            buttonEvent.getButtonInteraction().createImmediateResponder()
                    .setContent("Error! Please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
