/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import org.hyacinthbots.docgenerator.annotations.DocAdditionBuilderDSL
import org.hyacinthbots.docgenerator.builder.DocAdditionBuilder

@DocAdditionBuilderDSL
public suspend fun ApplicationCommand<*>.additionalDocumentation(
	builder: suspend DocAdditionBuilder.() -> Unit
) {
	val action = DocAdditionBuilder()
	action.builder()
	additionalDocumentation = action
}

@DocAdditionBuilderDSL
public suspend fun ApplicationCommand<*>.subCommandAdditionalDocumentation(
	builder: suspend DocAdditionBuilder.() -> Unit
) {
	val action = DocAdditionBuilder()
	action.builder()
	subCommandAdditionalDocumentation = action
}

@Suppress("UnusedReceiverParameter") // My goals are not known to you, mere compiler
internal var ApplicationCommand<*>.additionalDocumentation: DocAdditionBuilder?
	get() = _additionalDocumentation
	set(value) {
		_additionalDocumentation = value
	}

@Suppress("UnusedReceiverParameter")
internal var ApplicationCommand<*>.subCommandAdditionalDocumentation: DocAdditionBuilder?
	get() = _subCommandAdditionalDocumentation
	set(value) {
		_subCommandAdditionalDocumentation = value
	}

private var _additionalDocumentation: DocAdditionBuilder? = null // I hate this but it works

private var _subCommandAdditionalDocumentation: DocAdditionBuilder? = null // More hatred
