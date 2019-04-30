package com.vidiun.live.model.aggregation.functions.map;

import com.vidiun.live.model.aggregation.StatsEvent;
import com.vidiun.live.model.aggregation.keys.EntryLocationKey;
import com.vidiun.live.model.aggregation.keys.EventKey;

import scala.Tuple2;

public class LiveEntryLocationMap extends LiveEventMap{
	
	private static final long serialVersionUID = -2596392749426895360L;

	@Override
	public Tuple2<EventKey, StatsEvent> call(StatsEvent s) throws Exception {
		return new Tuple2<EventKey, StatsEvent>(new EntryLocationKey(s.getEntryId(), s.getEventTime(), s.getPartnerId(), s.getCountry(), s.getCity()), s);
	}

}
