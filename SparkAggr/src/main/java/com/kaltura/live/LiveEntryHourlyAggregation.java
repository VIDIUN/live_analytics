package com.kaltura.live;

import scala.Tuple2;



public class LiveEntryHourlyAggregation extends LiveAggregation {
	
	@Override
	public Tuple2<EventKey, StatsEvent> call(StatsEvent s) {
		return new Tuple2<EventKey, StatsEvent>(new EntryKey(s.getEntryId(), s.roundHourDate(s.getEventTime()), s.getPartnerId()) , s);
		
	}
	
}
