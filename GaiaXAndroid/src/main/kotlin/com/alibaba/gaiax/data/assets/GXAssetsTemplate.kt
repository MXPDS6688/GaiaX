/*
 * Copyright (c) 2021, Alibaba Group Holding Limited;
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.gaiax.data.assets

import android.content.Context
import com.alibaba.gaiax.GXTemplateEngine
import com.alibaba.gaiax.data.GXITemplateSource
import com.alibaba.gaiax.template.GXTemplate

/**
 * @suppress
 */
class GXAssetsTemplate(val context: Context) : GXITemplateSource {

    private val templateCache = mutableMapOf<String, MutableList<GXTemplate>>()

    override fun getTemplate(templateItem: GXTemplateEngine.GXTemplateItem): GXTemplate? {
        // 1. 判断Assets.gaiax最终产物是否存在，如果存在直接使用，并返回
        val memoryTemplate = getFromCache(templateItem.bizId, templateItem.templateId)
        if (memoryTemplate != null) {
            return memoryTemplate
        }

        // 2.
        val bundlePath = if (templateItem.bundle.isNotEmpty()) templateItem.bundle else templateItem.bizId
        val indexPath = "${bundlePath}/${templateItem.templateId}/index.json"
        val cssPath = "${bundlePath}/${templateItem.templateId}/index.css"
        val databindingPath = "${bundlePath}/${templateItem.templateId}/index.databinding"
        val jsPath = "${bundlePath}/${templateItem.templateId}/index.js"

        val index = readFileFromAssets(indexPath)
        val css = readFileFromAssets(cssPath)
        val databinding = readFileFromAssets(databindingPath)
        val js = readFileFromAssets(jsPath)
        if (index != null) {
            val templatePathAssets = GXTemplate(
                templateItem.templateId,
                templateItem.bizId,
                -1,
                index,
                css ?: "",
                databinding ?: "",
                js ?: ""
            )
            addToCache(templatePathAssets)
            return templatePathAssets
        }

        return null
    }

    private fun readFileFromAssets(path: String): String? {
        return try {
            context.resources?.assets?.open(path)?.bufferedReader(Charsets.UTF_8).use { it?.readText() }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFromCache(templateBiz: String, templateId: String) = templateCache[templateBiz]?.filter { it.id == templateId }?.maxByOrNull { it.version }

    private fun addToCache(template: GXTemplate) {
        var bizTemplates = templateCache[template.biz]
        if (bizTemplates == null) {
            bizTemplates = mutableListOf()
            templateCache[template.biz] = bizTemplates
        }
        bizTemplates.add(template)
    }
}