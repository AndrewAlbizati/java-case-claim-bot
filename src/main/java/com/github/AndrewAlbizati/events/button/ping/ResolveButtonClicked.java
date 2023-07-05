package com.github.AndrewAlbizati.events.button.ping;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.exceptions.claim.CheckedClaimNotFoundException;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

public class ResolveButtonClicked implements ButtonClickListener {
    private final Bot bot;
    public ResolveButtonClicked(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onButtonClick(ButtonClickEvent buttonEvent) {
        if (!buttonEvent.getButtonInteraction().getCustomId().equals("resolve")) {
            return;
        }

        try {
            ServerThreadChannel stc = buttonEvent.getButtonInteraction().getMessage().getServerThreadChannel()
                    .orElseThrow(() -> new CheckedClaimNotFoundException("CheckedClaim not found, please ensure that the button was clicked in a valid ServerThreadChannel"));

            buttonEvent.getInteraction().respondWithModal("resolve-modal-" + stc.getId(), "Resolve Ping",
                    ActionRow.of(TextInput.create(TextInputStyle.PARAGRAPH, "assessment", "Assessment (Optional)", false))
            );
        } catch (CheckedClaimNotFoundException e) {
            buttonEvent.getInteraction().createImmediateResponder()
                    .setContent("Error! Please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }
}
