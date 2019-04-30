package com.vidiun.live.model.aggregation.threads;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import com.vidiun.live.model.aggregation.StatsEvent;
import com.vidiun.live.model.aggregation.filter.StatsEventsHourlyFilter;
import com.vidiun.live.model.aggregation.functions.map.LiveEntryAggrMap;
import com.vidiun.live.model.aggregation.functions.map.LiveEventMap;
import com.vidiun.live.model.aggregation.functions.reduce.LiveEventMaxAudience;
import com.vidiun.live.model.aggregation.functions.reduce.LiveEventReduce;
import com.vidiun.live.model.aggregation.functions.save.LiveEntryHourlyMaxAudienceSave;
import com.vidiun.live.model.aggregation.functions.save.LiveEventSave;
import com.vidiun.live.model.aggregation.keys.EventKey;

public class EntryRealTimeLiveAggregationCycle extends RealTimeLiveAggregationCycle {

	private static final long serialVersionUID = 2799690938410469735L;
	
	protected JavaPairRDD<EventKey, StatsEvent> peakAudienceEvent = null;
	
	private LiveEntryAggrMap aggrMapFunction;
	private LiveEventMaxAudience aggrReduceFunction; 
	private LiveEntryHourlyMaxAudienceSave aggrSaveFunction;
	
	private int iterCounter = 0;
	
	public EntryRealTimeLiveAggregationCycle(LiveEventMap mapFunction,
			LiveEventReduce reduceFunction, LiveEventSave saveFunction, 
			LiveEntryAggrMap aggrMapFunction, LiveEventMaxAudience aggrReduceFunction, 
			LiveEntryHourlyMaxAudienceSave aggrSaveFunction) {
		super(mapFunction, reduceFunction, saveFunction);
		this.aggrMapFunction =  aggrMapFunction;
		this.aggrReduceFunction =  aggrReduceFunction;
		this.aggrSaveFunction =  aggrSaveFunction;
	}

	
	@Override
	public void run() {
		super.run();
		
		JavaPairRDD<EventKey, StatsEvent> audience = aggregatedEvents.mapToPair(aggrMapFunction);
		
		if (peakAudienceEvent != null) {
			audience = audience.union(peakAudienceEvent);
		}
		
		JavaPairRDD<EventKey, StatsEvent> topAudience = audience.reduceByKey(aggrReduceFunction);
		topAudience.filter(new StatsEventsHourlyFilter());
		JavaRDD<Boolean> result = topAudience.mapPartitions(aggrSaveFunction);
		peakAudienceEvent = topAudience;
		if (iterCounter > 50) {
			peakAudienceEvent.checkpoint();
            iterCounter = 0;
		}
		++iterCounter;
		result.count();
		
			 
	}

}
