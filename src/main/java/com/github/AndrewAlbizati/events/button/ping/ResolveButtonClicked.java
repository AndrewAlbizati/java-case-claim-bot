package com.github.AndrewAlbizati.events.button.ping;

import com.github.AndrewAlbizati.Bot;
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

        long threadId = buttonEvent.getButtonInteraction().getMessage().getServerThreadChannel().get().getId();

        buttonEvent.getInteraction().respondWithModal("resolve-modal-" + threadId, "Resolve Ping",
                ActionRow.of(TextInput.create(TextInputStyle.PARAGRAPH, "assessment", "Assessment (Optional)", false))
        );
    }
}
