package com.vidiun.live.model.aggregation.functions.save;

import java.util.ArrayList;
import java.util.Iterator;

import scala.Tuple2;

import com.vidiun.live.infra.cache.SerializableSession;
import com.vidiun.live.model.aggregation.StatsEvent;
import com.vidiun.live.model.aggregation.dao.LiveEntryEventDAO;
import com.vidiun.live.model.aggregation.dao.LiveEventDAO;
import com.vidiun.live.model.aggregation.dao.LivePartnerEntryDAO;
import com.vidiun.live.model.aggregation.keys.EventKey;

public class LiveEntryHourlySave extends LiveEventSave {
	
	private static final long serialVersionUID = 2456382584468986687L;
	
	private static final String TABLE_NAME = "vidiun_live.hourly_live_events";
	
	public LiveEntryHourlySave(SerializableSession session) {
		super(session);
	}
	
	@Override
	protected LiveEventDAO createLiveEventDAO() {
		return new LiveEntryEventDAO(TABLE_NAME, LiveEntryEventDAO.HOURLY_AGGR_TTL);
	}
	
	@Override
	public Iterable<Boolean> call(Iterator<Tuple2<EventKey, StatsEvent>> it) throws Exception {
		LivePartnerEntryDAO livePartnerEntry = new LivePartnerEntryDAO();
		 while (it.hasNext()) {
			Tuple2<EventKey, StatsEvent> row = it.next(); 
			EventKey key = row._1;
			StatsEvent stats = row._2;
			key.manipulateStatsEventByKey(stats);
		    event.saveOrUpdate(this.session, stats);
		    livePartnerEntry.saveOrUpdate(this.session, stats);
		 }
		 
		 // TODO - we might want to return something that indicates the status of the update.
		 return new ArrayList<Boolean>();
	}

}
