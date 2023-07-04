package com.github.AndrewAlbizati.commands;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.enums.PrivilegeLevel;
import com.github.AndrewAlbizati.models.User;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.sql.Date;
import java.time.LocalDate;

public class CreateUser implements SlashCommandCreateListener {
    private final Bot bot;

    public CreateUser(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        // Ignore other slash commands
        if (!interaction.getCommandName().equalsIgnoreCase("createuser")) {
            return;
        }

        event.getInteraction().respondWithModal("create-user-modal", "Add User",
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "firstname_input", "First Name", true)),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "lastname_input", "Last Name", true)),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "email_input", "Email", true)),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "birthday_input", "Birthday (MM/DD/YYYY)", true))
        );

        event.getApi().addModalSubmitListener(submitEvent -> {
            if (!submitEvent.getModalInteraction().getCustomId().equals("create-user-modal")) {
                return;
            }

            String firstName = null;
            String lastName = null;
            String email = null;
            String birthday = null;

            for (HighLevelComponent hlc : submitEvent.getModalInteraction().getComponents()) {
                LowLevelComponent llc = hlc.asActionRow().get().getComponents().get(0); // Get 0 since each row has one field

                switch (llc.asTextInput().get().getCustomId()) {
                    case "firstname_input" -> firstName = llc.asTextInput().get().getValue();
                    case "lastname_input" -> lastName = llc.asTextInput().get().getValue();
                    case "email_input" -> email = llc.asTextInput().get().getValue();
                    case "birthday_input" -> birthday = llc.asTextInput().get().getValue();
                }
            }

            if (firstName == null || lastName == null || email == null || birthday == null) {
                return;
            }

            int month = Integer.parseInt(birthday.split("/")[0]);
            int day = Integer.parseInt(birthday.split("/")[1]);
            int year = Integer.parseInt(birthday.split("/")[2]);

            User user = new User(
                    interaction.getUser().getId(),
                    firstName,
                    lastName,
                    email,
                    Date.valueOf(LocalDate.of(year, month, day)),
                    Date.valueOf(LocalDate.now()),
                    true,
                    PrivilegeLevel.TECH
            );

            if (user.addToDatabase(bot.getConnection())) {
                submitEvent.getModalInteraction().createImmediateResponder()
                        .setContent("👍")
                        .setFlags(MessageFlag.EPHEMERAL)
                        .respond();
            }
        });
    }
}