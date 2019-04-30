package com.vidiun.live.model.aggregation.functions.save;

import com.vidiun.live.infra.cache.SerializableSession;
import com.vidiun.live.model.aggregation.dao.LiveEntryLocationEventDAO;
import com.vidiun.live.model.aggregation.dao.LiveEventDAO;

public class LiveEntryLocationSave extends LiveEventSave {
	
	private static final long serialVersionUID = -5105640123955409065L;

	public LiveEntryLocationSave(SerializableSession session) {
		super(session);
	}
	
	@Override
	protected LiveEventDAO createLiveEventDAO() {
		return new LiveEntryLocationEventDAO();
	}

}
