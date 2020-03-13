package com.tsukiseele.moeviewerr.dataholder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.utils.IOUtil
import com.tsukiseele.sakurawler.Sakurawler
import com.tsukiseele.sakurawler.SiteManager
import java.io.File

class FavoritesHolder private constructor(val favorites: MutableMap<String, MutableSet<Image>>) {
    companion object {
        private var instance: FavoritesHolder? = null
            get() {
                if (field == null)
                    field = FavoritesHolder(loadState())
                return field
            }

        @Synchronized
        fun get(): FavoritesHolder {
            return instance!!
        }

        fun getImages(): MutableList<Image> {
            return mutableListOf<Image>().apply {
                get().favorites.forEach { entry ->
                    addAll(entry.value)
                }
            }
        }

        fun getFavorites(): MutableMap<String, MutableSet<Image>> {
            return get().favorites
        }

        fun remove(image: Image) {
            val logger = File(Config.FILE_LOGGER)
            logger.appendText("/////////////////// START //////////////////\n")
            logger.appendText("Image: ${image}\n")
            image.crawler?.site?.title?.also {
                logger.appendText("siteName: ${it}\n")
                val fav = get().favorites
                if (fav.containsKey(it)) {
                    logger.appendText("fav.containsKey: ${fav.containsKey(it)}\n")
                    fav.get(it)?.apply {
                        forEach {
                            logger.appendText("loop: ${it}\n")
                            if (it.equals(image)) {
                                remove(image)
                                logger.appendText("removed: ${image}\n")
                                return@apply
                            }
                        }
                        logger.appendText("isEmpty?: ${it}\n")
                        if (isEmpty()) {
                            fav.remove(it)
                            logger.appendText("removeEmptyGroup: ${it}\n")
                        }
                    }
                }
            }
            logger.appendText("/////////////////// END //////////////////\n")
        }

        fun add(image: Image) {
            image.crawler?.site?.title?.also {
                val fav = get().favorites
                if (fav.containsKey(it))
                    fav.get(it)?.add(image)
                else
                    fav.put(it, mutableSetOf(image))
            }
        }

        private fun loadState(): MutableMap<String, MutableSet<Image>> {
            try {
                return Gson().fromJson<MutableMap<String, MutableSet<Image>>>(
                    File(Config.FILE_FAVORITES).readText(),
                    object : TypeToken<MutableMap<String, MutableSet<Image>>>() {}.type
                ).also {
                    for ((key, value) in it) {
                        SiteManager.findSiteByTitle(key)?.let {
                            val crawler = Sakurawler(it)
                            value.forEach {
                                it.crawler = crawler
                            }
                        } ?: continue
                    }
                }
            } catch (e: Exception) {

            }
            return mutableMapOf()
        }

        fun saveState(): Boolean {
            try {
                IOUtil.writeText(Config.FILE_FAVORITES, Gson().toJson(get().favorites))
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}
