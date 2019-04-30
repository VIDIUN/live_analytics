package com.vidiun.live.model.aggregation.functions.save;

import com.vidiun.live.infra.cache.SerializableSession;
import com.vidiun.live.model.aggregation.dao.LiveEntryEventDAO;
import com.vidiun.live.model.aggregation.dao.LiveEventDAO;
import com.vidiun.live.model.aggregation.dao.PartnerEventDAO;

public class PartnerHourlySave extends LiveEventSave {

	private static final long serialVersionUID = 5230447429205620876L;
	
	private static final String TABLE_NAME = "vidiun_live.hourly_live_events_partner";
	
	public PartnerHourlySave(SerializableSession session) {
		super(session);
	}

	@Override
	protected LiveEventDAO createLiveEventDAO() {
		return new PartnerEventDAO(TABLE_NAME, PartnerEventDAO.HOURLY_AGGR_TTL);
	}

}
