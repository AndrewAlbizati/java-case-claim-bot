package com.github.AndrewAlbizati.events.modal;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.exceptions.claim.CheckedClaimNotFoundException;
import com.github.AndrewAlbizati.models.CheckedClaim;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ModalSubmitEvent;
import org.javacord.api.listener.interaction.ModalSubmitListener;

public class ResolveModalSubmit implements ModalSubmitListener {
    private final Bot bot;
    public ResolveModalSubmit(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onModalSubmit(ModalSubmitEvent submitEvent) {
        if (!submitEvent.getModalInteraction().getCustomId().startsWith("resolve-modal")) {
            return;
        }

        try {
            // Find CheckedClaim
            long threadId = Long.parseLong(submitEvent.getModalInteraction().getCustomId().split("-")[2]);
            CheckedClaim checkedClaim = CheckedClaim.fromPingThreadId(bot.getConnection(), threadId)
                    .orElseThrow(() -> new CheckedClaimNotFoundException("CheckedClaim not found, check the ping thread ID"));

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Case Assessment")
                    .setColor(bot.getPrimaryEmbedColor())
                    .setDescription("<@!" + checkedClaim.tech().discordId() + "> has resolved this case.");
            if (submitEvent.getModalInteraction().getTextInputValues().size() > 0 && submitEvent.getModalInteraction().getTextInputValues().get(0).length() != 0) {
                eb.addField("Assessment", submitEvent.getModalInteraction().getTextInputValues().get(0));
            }

            submitEvent.getModalInteraction().createImmediateResponder()
                    .addEmbed(eb)
                    .addComponents(ActionRow.of(
                                    Button.success("close-resolved", "Close and Mark Resolved"),
                                    Button.danger("close-pinged", "Close and Keep Pinged"),
                                    Button.secondary("close-checked", "Close and Mark Checked")
                            )
                    ).respond();
        } catch (CheckedClaimNotFoundException e) {
            e.printStackTrace();
            submitEvent.getModalInteraction().createImmediateResponder()
                    .setContent("Error! Please try again.")
                    .respond();
        }
    }
}
