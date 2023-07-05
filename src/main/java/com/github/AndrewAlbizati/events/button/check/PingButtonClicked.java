package com.github.AndrewAlbizati.events.button.check;

import com.github.AndrewAlbizati.Bot;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

public class PingButtonClicked implements ButtonClickListener {
    private final Bot bot;

    public PingButtonClicked(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onButtonClick(ButtonClickEvent buttonEvent) {
        if (!buttonEvent.getButtonInteraction().getCustomId().equals("ping")) {
            return;
        }

        // Create new embed with danger color
        EmbedBuilder eb = buttonEvent.getButtonInteraction().getMessage().getEmbeds().get(0).toBuilder();
        eb.setColor(bot.getDangerEmbedColor());

        // Update checker message
        buttonEvent.getButtonInteraction().getMessage().createUpdater()
                .removeAllEmbeds()
                .addEmbed(eb)
                .removeAllComponents()
                .addComponents(ActionRow.of(
                        Button.secondary("check", "Check"),
                        Button.secondary("ping", "Ping")
                ))
                .applyChanges();

        // Send ping modal
        buttonEvent.getInteraction().respondWithModal("ping-modal-" + buttonEvent.getButtonInteraction().getMessage().getId(), "Ping Case",
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "ping_severity", "Ping Severity", true)),
                ActionRow.of(TextInput.create(TextInputStyle.PARAGRAPH, "ping_description", "Ping Description", true)),
                ActionRow.of(TextInput.create(TextInputStyle.PARAGRAPH, "ping_todo", "To Do", false))
        );
    }
}
