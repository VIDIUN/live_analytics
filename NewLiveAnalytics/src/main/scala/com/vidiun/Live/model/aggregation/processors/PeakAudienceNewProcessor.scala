package com.vidiun.Live.model.aggregation.processors

import java.util.Date

import com.datastax.spark.connector._
import com.vidiun.Live.model.dao.LiveEvents
import com.vidiun.Live.utils.DateUtils
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

/**
 * Created by orlylampert on 7/14/16.
 */
object PeakAudienceNewProcessor {

  val entryPeakFieldsList = List(
    "entry_id",
    "event_time",
    "audience",
    "dvr_audience",
    "update_time")

  def toSomeColumns( columnNames: List[String] ) : SomeColumns =
  {
    SomeColumns(columnNames.map(x => new ColumnName(x) ): _*)
  }

  def process(sc: SparkContext, date: Long): Unit = {

    val startHour = new Date(DateUtils.roundTimeToHour(date))

    val liveNow = sc.cassandraTable("vidiun_live", "hourly_live_events").select("entry_id").where("event_time = ?", startHour)
    val hourlyPeak = sc.cassandraTable("vidiun_live", "live_entry_hourly_peak").select("entry_id", "event_time", "update_time", "audience", "dvr_audience").where("event_time = ?", startHour)

    val allEntries = liveNow.map(row => (row.getString("entry_id"), row.getString("entry_id"))).cogroup(hourlyPeak.map(row => (row.getString("entry_id"), PeakAudience(row.getString("entry_id"), row.getDate("event_time"), row.getDateOption("update_time").getOrElse(startHour), row.getLong("audience"), row.getLong("dvr_audience")))))
    allEntries.map(x => {
      if (x._2._2.isEmpty) {
        PeakAudience(x._1, startHour, new Date(date), 0, 0)
      } else {
        x._2._2.head
      }
    }).map(LiveEvents.selectPeak(_))
      .saveToCassandra("vidiun_live", "live_entry_hourly_peak", toSomeColumns(entryPeakFieldsList))
  }
}

case class PeakAudience (entryId: String, eventTime: Date, updateTime: Date, audience: Long, dvrAudience: Long) extends Serializable

