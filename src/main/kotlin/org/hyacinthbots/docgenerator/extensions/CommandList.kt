/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.rest.builder.message.EmbedBuilder
import org.hyacinthbots.docgenerator.addArguments
import org.hyacinthbots.docgenerator.builder.DocAdditionBuilder
import org.hyacinthbots.docgenerator.formatPermissionsSet
import org.hyacinthbots.docgenerator.subCommandAdditionalDocumentation
import org.hyacinthbots.docgenerator.translate

public class CommandList : Extension() {
	override val name: String = "command-list"

	override suspend fun setup() {
		val pagesObj = Pages()

		bot.extensions.values.toMutableList().forEach {
			it.slashCommands.forEach { slashCommand ->
				if (slashCommand.subCommands.isNotEmpty()) {
					slashCommand.subCommands.forEach { subCommand ->
						val provider = subCommand.translationsProvider
						val bundle = subCommand.extension.bundle
						var arguments: String? = addArguments(subCommand, provider, bundle, null)
						if (arguments?.isEmpty() == true) arguments = null
						var subExtraDocs = subCommand.subCommandAdditionalDocumentation[subCommand.name]
						pagesObj.addPage(
							Page {
								title = "${subCommand.parentCommand?.name?.translate(provider, null, bundle)}" +
										" ${subCommand.name.translate(provider, null, bundle)}"
								description = subCommand.description
								createEmbed(arguments, subCommand, provider, bundle, subExtraDocs)
							}
						)
						subExtraDocs = null
					}
				} else {
					var arguments: String?
					val provider = slashCommand.translationsProvider
					val bundle = slashCommand.extension.bundle
					arguments = addArguments(slashCommand, provider, bundle, null)
					if (arguments?.isEmpty() == true) arguments = null
					var extraDocs = slashCommand.subCommandAdditionalDocumentation[slashCommand.name]
					pagesObj.addPage(
						Page {
							title = slashCommand.name.translate(provider, null, bundle)
							description = slashCommand.description.translate(provider, null, bundle)
							createEmbed(arguments, slashCommand, provider, bundle, extraDocs)
						}
					)
					extraDocs = null
				}
			}
		}

		publicSlashCommand {
			name = "command-list"
			description = "Shows a list of ${kord.getSelf().username}'s commands!"

			action {
				val paginator = PublicResponsePaginator(
					pages = pagesObj,
					keepEmbed = true,
					owner = event.interaction.user,
					timeoutSeconds = 500,
					locale = this.getLocale(),
					interaction = interactionResponse
				)

				paginator.send()
			}
		}
	}

	/**
	 * Builds the embed content for the command list.
	 *
	 * @param args The command arguments string
	 * @param command The command being documented
	 * @param provider The translation provider
	 * @param bundle The bundle to get the translations from
	 * @param extraDocs Any extra documentation for the command
	 */
	private fun EmbedBuilder.createEmbed(
		args: String?,
		command: SlashCommand<*, *, *>,
		provider: TranslationsProvider,
		bundle: String?,
		extraDocs: DocAdditionBuilder?
	) {
		field {
			name = "Arguments"
			value = args ?: "None"
		}
		field {
			name = "Permissions"
			value = if (command.requiredPerms.isNotEmpty()) {
				"* ${
					"header.permissions.bot".translate(provider, null, bundle)
				}:${command.requiredPerms.formatPermissionsSet(null)}\n"
			} else {
				"None"
			}
		}
		if (extraDocs?.commandResult != null) {
			field {
				name = "Result"
				value =
					"* **${"header.result".translate(provider, null, bundle)}**:${
						extraDocs.commandResult!!.translate(provider, null, bundle)
					}\n"
			}
		}
	}
}
