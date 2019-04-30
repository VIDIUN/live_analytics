package com.vidiun.live.model.aggregation.threads;

import com.vidiun.live.model.aggregation.filter.StatsEventsFilter;
import com.vidiun.live.model.aggregation.filter.StatsEventsRealTimeFilter;
import com.vidiun.live.model.aggregation.functions.map.LiveEventMap;
import com.vidiun.live.model.aggregation.functions.reduce.LiveEventReduce;
import com.vidiun.live.model.aggregation.functions.save.LiveEventSave;


public class RealTimeLiveAggregationCycle extends LiveAggregationCycle {

	private static final long serialVersionUID = -2335303060005193307L;
	
	public RealTimeLiveAggregationCycle(LiveEventMap mapFunction,
			LiveEventReduce reduceFunction, LiveEventSave saveFunction) {
		super(mapFunction, reduceFunction, saveFunction);
	}

	@Override
	protected StatsEventsFilter getFilterFunction() {
		return new StatsEventsRealTimeFilter();
	}
	
	

}
