package com.vidiun.live.model.aggregation.functions.map;

import org.apache.spark.api.java.function.PairFunction;

import com.vidiun.live.model.aggregation.StatsEvent;
import com.vidiun.live.model.aggregation.keys.EventKey;

/**
 *  This is base class represent a live aggregation functionality
 */
public abstract class LiveEventMap implements PairFunction<StatsEvent, EventKey, StatsEvent> {

	private static final long serialVersionUID = -4519772938926660372L;

	
	// The call function generates the key for each stats event, by which the spark will later aggregate the results.


    
}
