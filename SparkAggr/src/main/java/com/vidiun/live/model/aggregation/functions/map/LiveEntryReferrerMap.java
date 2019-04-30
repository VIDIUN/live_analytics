package com.vidiun.live.model.aggregation.functions.map;

import com.vidiun.live.infra.utils.DateUtils;
import com.vidiun.live.model.aggregation.StatsEvent;
import com.vidiun.live.model.aggregation.keys.EntryReferrerKey;
import com.vidiun.live.model.aggregation.keys.EventKey;

import scala.Tuple2;

public class LiveEntryReferrerMap extends LiveEventMap{
	
	private static final long serialVersionUID = 3784007470981992581L;

	@Override
	public Tuple2<EventKey, StatsEvent> call(StatsEvent s) throws Exception {
		return new Tuple2<EventKey, StatsEvent>(new EntryReferrerKey(s.getEntryId(), DateUtils.roundHourDate(s.getEventTime()), s.getPartnerId(), s.getReferrer()), s);
	}

}
