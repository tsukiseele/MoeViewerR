package com.tsukiseele.sakurawler.core

/**
 * @author TsukiSeele
 * @date 2020.02.18
 * 继承于Model
 * 该类允许使用Map结构存放数据而非POJO
 */
abstract class ModelMap : Model() {
    abstract val attrs: MutableMap<String?, String?>

    override fun fillTo(model: Model?) {
        model ?: return
        if (model is ModelMap)
            for (entry in attrs)
                if (!model.attrs.containsKey(entry.key))
                    model.attrs[entry.key] = entry.value
    }

    override fun fillToAll(models: List<Model?>?) {
        models ?: return
        for (model in models)
            fillTo(model)
    }

    override fun coverTo(model: Model?) {
        model ?: return
        if (model is ModelMap)
            for (entry in attrs)
                model.attrs[entry.key] = entry.value
    }

    override fun coverToAll(models: List<Model?>?) {
        models ?: return
        for (model in models)
            coverTo(model)
    }
}