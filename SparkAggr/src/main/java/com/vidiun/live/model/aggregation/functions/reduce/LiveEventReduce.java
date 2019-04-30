package com.vidiun.live.model.aggregation.functions.reduce;

import org.apache.spark.api.java.function.Function2;

import com.vidiun.live.model.aggregation.StatsEvent;

public class LiveEventReduce implements Function2<StatsEvent, StatsEvent, StatsEvent> {
	
	private static final long serialVersionUID = -1553206822692180037L;

	@Override
     public StatsEvent call(StatsEvent stats, StatsEvent stats2) throws Exception {
		 return stats.merge(stats2);
     }
}
