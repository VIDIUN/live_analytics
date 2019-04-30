package com.vidiun.live;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vidiun.live.infra.cache.SerializableSession;
import com.vidiun.live.infra.utils.DateUtils;
import com.vidiun.live.infra.utils.LiveConfiguration;
import com.vidiun.live.model.aggregation.StatsEvent;
import com.vidiun.live.model.aggregation.functions.map.LiveEntryAggrMap;
import com.vidiun.live.model.aggregation.functions.map.LiveEntryHourlyMap;
import com.vidiun.live.model.aggregation.functions.map.LiveEntryLocationMap;
import com.vidiun.live.model.aggregation.functions.map.LiveEntryMap;
import com.vidiun.live.model.aggregation.functions.map.LiveEntryReferrerMap;
import com.vidiun.live.model.aggregation.functions.map.PartnerHourlyMap;
import com.vidiun.live.model.aggregation.functions.map.StatsEventMap;
import com.vidiun.live.model.aggregation.functions.reduce.LiveEventMaxAudience;
import com.vidiun.live.model.aggregation.functions.reduce.LiveEventReduce;
import com.vidiun.live.model.aggregation.functions.save.LiveEntryHourlyMaxAudienceSave;
import com.vidiun.live.model.aggregation.functions.save.LiveEntryHourlySave;
import com.vidiun.live.model.aggregation.functions.save.LiveEntryLocationSave;
import com.vidiun.live.model.aggregation.functions.save.LiveEntryReferrerSave;
import com.vidiun.live.model.aggregation.functions.save.LiveEntrySave;
import com.vidiun.live.model.aggregation.functions.save.PartnerHourlySave;
import com.vidiun.live.model.aggregation.threads.EntryRealTimeLiveAggregationCycle;
import com.vidiun.live.model.aggregation.threads.HourlyLiveAggregationCycle;
import com.vidiun.live.model.aggregation.threads.LiveAggregationCycle;
import com.vidiun.live.model.aggregation.threads.RealTimeLiveAggregationCycle;
import com.vidiun.live.model.logs.functions.FilterOldFileIds;
import com.vidiun.live.model.logs.functions.GetNewFileIds;
import com.vidiun.live.model.logs.functions.LoadNewFiles;

/**
 * This class is the main class responsible for the spark aggregation
 * That's a temporal class used to test the system and will be replaced. one day.
 */
public class SparkAggregation {

	private static Logger LOG = LoggerFactory.getLogger(SparkAggregation.class);
	private static LiveConfiguration config;
	public static void main(String[] args) throws Exception {
		
		config = LiveConfiguration.instance();
		
		validateArguments(args);
		final JavaSparkContext jsc = initializeEnvironment();

		SerializableSession session = new SerializableSession(config.getCassandraNodeName());
		
		// Generate aggregation threads
		LiveAggregationCycle entryAggr = new EntryRealTimeLiveAggregationCycle(new LiveEntryMap(), new LiveEventReduce(), new LiveEntrySave(session),
				new LiveEntryAggrMap(), new LiveEventMaxAudience(), new LiveEntryHourlyMaxAudienceSave(session));
		HourlyLiveAggregationCycle entryHourlyAggr = new HourlyLiveAggregationCycle(new LiveEntryHourlyMap(), new LiveEventReduce(), new LiveEntryHourlySave(session));
		LiveAggregationCycle locationEntryAggr = new RealTimeLiveAggregationCycle(new LiveEntryLocationMap(), new LiveEventReduce(), new LiveEntryLocationSave(session));
		HourlyLiveAggregationCycle referrerHourlyAggr = new HourlyLiveAggregationCycle(new LiveEntryReferrerMap(), new LiveEventReduce(), new LiveEntryReferrerSave(session));
		HourlyLiveAggregationCycle partnerHourlyAggr = new HourlyLiveAggregationCycle(new PartnerHourlyMap(), new LiveEventReduce(), new PartnerHourlySave(session));
		
		long executionStartTime = System.currentTimeMillis();

		JavaRDD<String> loadedDates = null;
		Set<Long> hoursToLoad = new HashSet<Long>();
		int iterCount = 0;
		boolean resume = true;
		while (resume) {
			
			hoursToLoad.add(DateUtils.getCurrentHourInMillis());
			List<Long> hoursToLoadList = new ArrayList<Long>();
			hoursToLoadList.addAll(hoursToLoad);
			JavaRDD<Long> dates = jsc.parallelize(hoursToLoadList, 8);
			
			JavaRDD<String> fileIdsToLoad = dates.flatMap(new GetNewFileIds(session));

			// get new file names to load
			if (loadedDates != null) {
				fileIdsToLoad = fileIdsToLoad.subtract(loadedDates);
			}

			// if new files were added
			LOG.debug("Before fileIdsToLoad.count()");
			long fileIdsToLoadCount = fileIdsToLoad.count();
			LOG.debug("After fileIdsToLoad.count()");
			if (fileIdsToLoadCount > 0) {
				long startTime = System.currentTimeMillis();
				// load new files
				// need to repartitions according to the number of new files to load
				JavaRDD<String> loadedLines = fileIdsToLoad.flatMap(new LoadNewFiles());
				
				if (loadedDates != null) {
					fileIdsToLoad = fileIdsToLoad.union(loadedDates).coalesce(8);
					loadedDates.unpersist();
					fileIdsToLoad.filter(new FilterOldFileIds());
				}
				
				loadedDates = fileIdsToLoad;
				loadedDates.cache();
				if (iterCount > 50) {
					loadedDates.checkpoint();
					iterCount = 0;
				}
				++iterCount;
				LOG.debug("Before loadedDates.count()");
				loadedDates.count();
				LOG.debug("After loadedDates.count()");

				// Map each line events to statsEvent object
				JavaRDD<StatsEvent> loadedEvents = loadedLines
						.repartition(24)
						.mapPartitions(
								new StatsEventMap(config.getIp2locationPath()));
				
				loadedEvents.cache();
				LOG.debug("Before loadedEvents.count() - map log lines to StatsEvent");
				long loadedEventCount = loadedEvents.count();
				LOG.debug("After loadedEvents.count()");

				if (loadedEventCount > 0)
				{
					LOG.debug("Start Aggregate new events");
					long aggrStartTime = System.currentTimeMillis();
					entryAggr.init(loadedEvents);
					entryHourlyAggr.init(loadedEvents);
					locationEntryAggr.init(loadedEvents);
					referrerHourlyAggr.init(loadedEvents);
					partnerHourlyAggr.init(loadedEvents);
					
					LOG.debug("Before entry aggregation");
					entryAggr.run();
					LOG.debug("After entry aggregation");
					LOG.debug("Before entry hourly aggregation");
					entryHourlyAggr.run();
					LOG.debug("After entry hourly aggregation");
					LOG.debug("Before location aggregation");
					locationEntryAggr.run();
					LOG.debug("After location aggregation");
					LOG.debug("Before referrer aggregation");
					referrerHourlyAggr.run();
					LOG.debug("After referrer aggregation");
					LOG.debug("Before partner aggregation");
					partnerHourlyAggr.run();
					LOG.debug("After partner aggregation");
					long aggrEndTime = System.currentTimeMillis();
									 
					LOG.debug("Aggregation Iteration time (msec): "
							+ (aggrEndTime - aggrStartTime));
				}
				
				loadedEvents.unpersist();

				long endTime = System.currentTimeMillis();
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				LOG.debug(dateFormat.format(date) + " : Iteration time (msec): "
						+ (endTime - startTime));
				
			}

		}
		
		Thread.sleep(1000 * 120);
		System.exit(0);

	}

	private static JavaSparkContext initializeEnvironment() {
		LiveConfiguration config = LiveConfiguration.instance();
		System.setProperty("spark.default.parallelism", config.getSparkParallelism());
		System.setProperty("spark.cores.max", config.getSparkMaxCores());
		System.setProperty("spark.executor.memory", config.getSparkExectorMem());

		String[] jars = { config.getRepositoryHome() + "/spark-aggr.jar",
				config.getRepositoryHome() + "/cassandra-driver-core-2.0.7.jar"};
//		String[] jars = { config.getRepositoryHome() + "/spark-aggr-1.0.0.jar",
//				 config.getRepositoryHome() + "/cassandra-driver-core-2.0.7.jar",
//				 config.getRepositoryHome() + "/live-model-1.0.0.jar",
//				 config.getRepositoryHome() + "/live-infra-1.0.0.jar",
//				 config.getRepositoryHome() + "/ip-2-location-1.0.0.jar" };
		Map<String, String> env = new HashMap<String, String>();
		env.put("VIDIUN_CONF_PATH", System.getenv().get("VIDIUN_CONF_PATH"));
		
		final JavaSparkContext jsc = new JavaSparkContext(config.getSparkMaster(),
                "SparkAggr", config.getSparkHome(), jars, env);
		jsc.setCheckpointDir(config.getCheckpointDir());
		return jsc;
	}

	private static void validateArguments(String[] args) {
		if (args.length > 1) {
			System.err.println("Usage: SparkAggr <master>");
			System.exit(1);
		}
	}

}
