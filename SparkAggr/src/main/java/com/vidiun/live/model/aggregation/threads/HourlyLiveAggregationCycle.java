package com.vidiun.live.model.aggregation.threads;

import com.vidiun.live.model.aggregation.filter.StatsEventsHourlyFilter;
import com.vidiun.live.model.aggregation.filter.StatsEventsFilter;
import com.vidiun.live.model.aggregation.functions.map.LiveEventMap;
import com.vidiun.live.model.aggregation.functions.reduce.LiveEventReduce;
import com.vidiun.live.model.aggregation.functions.save.LiveEventSave;


/**
 *	This thread is responsible to  
 */
public class HourlyLiveAggregationCycle extends LiveAggregationCycle {

	private static final long serialVersionUID = -6634875852998471397L;
	
	public HourlyLiveAggregationCycle(LiveEventMap mapFunction, LiveEventReduce reduceFunction, LiveEventSave saveFunction) {
		super(mapFunction, reduceFunction, saveFunction);
	}

	@Override
	protected StatsEventsFilter getFilterFunction() {
		return new StatsEventsHourlyFilter();
	}
	
}
