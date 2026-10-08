package com.example.data.repository

import com.example.data.local.dao.EpisodeDao
import com.example.data.local.dao.SeasonDao
import com.example.data.local.dao.SeriesDao
import com.example.data.local.entity.EpisodeEntity
import com.example.data.local.entity.SeasonEntity
import com.example.data.local.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

class SeriesRepository(
    private val seriesDao: SeriesDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao
) {
    val publishedSeries: Flow<List<SeriesEntity>> = seriesDao.getPublishedSeriesFlow()
    val featuredSeries: Flow<List<SeriesEntity>> = seriesDao.getFeaturedSeriesFlow()
    val trendingSeries: Flow<List<SeriesEntity>> = seriesDao.getTrendingSeriesFlow()
    val allSeriesForAdmin: Flow<List<SeriesEntity>> = seriesDao.getAllSeriesFlow()

    fun getSeriesByIdFlow(id: String): Flow<SeriesEntity?> = seriesDao.getSeriesByIdFlow(id)

    suspend fun getSeriesById(id: String): SeriesEntity? = seriesDao.getSeriesById(id)

    suspend fun saveSeries(series: SeriesEntity) {
        seriesDao.insertSeries(series)
    }

    suspend fun updateSeries(series: SeriesEntity) {
        seriesDao.updateSeries(series)
    }

    suspend fun togglePublish(series: SeriesEntity) {
        seriesDao.updateSeries(series.copy(isPublished = !series.isPublished))
    }

    suspend fun deleteSeries(id: String) {
        episodeDao.deleteEpisodesForSeries(id)
        seasonDao.deleteSeasonsForSeries(id)
        seriesDao.deleteSeries(id)
    }

    // Seasons
    fun getSeasonsForSeriesFlow(seriesId: String): Flow<List<SeasonEntity>> =
        seasonDao.getSeasonsForSeriesFlow(seriesId)

    suspend fun getSeasonsForSeries(seriesId: String): List<SeasonEntity> =
        seasonDao.getSeasonsForSeries(seriesId)

    suspend fun saveSeason(season: SeasonEntity) {
        seasonDao.insertSeason(season)
    }

    suspend fun deleteSeason(id: String) {
        seasonDao.deleteSeason(id)
    }

    // Episodes
    fun getEpisodesForSeasonFlow(seasonId: String): Flow<List<EpisodeEntity>> =
        episodeDao.getEpisodesForSeasonFlow(seasonId)

    fun getEpisodesForSeriesFlow(seriesId: String): Flow<List<EpisodeEntity>> =
        episodeDao.getEpisodesForSeriesFlow(seriesId)

    suspend fun getEpisodeById(id: String): EpisodeEntity? = episodeDao.getEpisodeById(id)

    fun getEpisodeByIdFlow(id: String): Flow<EpisodeEntity?> = episodeDao.getEpisodeByIdFlow(id)

    suspend fun saveEpisode(episode: EpisodeEntity) {
        episodeDao.insertEpisode(episode)
    }

    suspend fun updateEpisode(episode: EpisodeEntity) {
        episodeDao.updateEpisode(episode)
    }

    suspend fun deleteEpisode(id: String) {
        episodeDao.deleteEpisode(id)
    }

    suspend fun getTotalSeriesCount(): Int = seriesDao.getTotalSeriesCount()
    suspend fun getPublishedSeriesCount(): Int = seriesDao.getPublishedSeriesCount()
    suspend fun getUnpublishedSeriesCount(): Int = seriesDao.getUnpublishedSeriesCount()
    suspend fun getRecentSeries(limit: Int = 5): List<SeriesEntity> = seriesDao.getRecentSeries(limit)

    suspend fun getTotalSeasonsCount(): Int = seasonDao.getTotalSeasonsCount()
    suspend fun getTotalEpisodesCount(): Int = episodeDao.getTotalEpisodesCount()
    suspend fun getPublishedEpisodesCount(): Int = episodeDao.getPublishedEpisodesCount()
    suspend fun getUnpublishedEpisodesCount(): Int = episodeDao.getUnpublishedEpisodesCount()

    val allEpisodesFlow: Flow<List<EpisodeEntity>> = episodeDao.getAllEpisodesFlow()

    suspend fun toggleEpisodePublish(episode: EpisodeEntity) {
        episodeDao.updateEpisode(episode.copy(isPublished = !episode.isPublished))
    }
}
