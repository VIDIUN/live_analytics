package com.vidiun.live.model.aggregation.functions.save;

import com.vidiun.live.infra.cache.SerializableSession;
import com.vidiun.live.model.aggregation.dao.LiveEntryEventDAO;
import com.vidiun.live.model.aggregation.dao.LiveEventDAO;

/**
 * Save function wrapping for Live entry
 */
public class LiveEntrySave extends LiveEventSave {
	
	private static final long serialVersionUID = 3189544783672808202L;
	
	private static final String TABLE_NAME = "vidiun_live.live_events";
	
	public LiveEntrySave(SerializableSession session) {
		super(session);
	}
	
	@Override
	protected LiveEventDAO createLiveEventDAO() {
		return new LiveEntryEventDAO(TABLE_NAME, LiveEntryEventDAO.AGGR_TTL);
	}
}
