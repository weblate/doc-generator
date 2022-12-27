/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

/**
 * Convenience object containing variables for all command types.
 */
public object CommandType {
	/** Represents Slash commands. */
	public val SLASH: CommandTypes = CommandTypes.SLASH

	/** Represents Message commands. */
	public val MESSAGE: CommandTypes = CommandTypes.MESSAGE

	/** Represents User commands. */
	public val USER: CommandTypes = CommandTypes.USER

	/** Represents all types of commands. */
	public val ALL: List<CommandTypes> = listOf(
		CommandTypes.SLASH,
		CommandTypes.MESSAGE,
		CommandTypes.USER
	)
}
