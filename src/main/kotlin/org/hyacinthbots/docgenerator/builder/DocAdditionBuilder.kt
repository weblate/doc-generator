/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.builder

import org.hyacinthbots.docgenerator.annotations.DocAdditionBuilderDSL

@DocAdditionBuilderDSL
public open class DocAdditionBuilder {
	public open var commandResult: String? = null

	public open var extraInformation: String? = null
}
