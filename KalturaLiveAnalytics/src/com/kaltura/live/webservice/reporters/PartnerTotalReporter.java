package com.kaltura.live.webservice.reporters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.kaltura.live.infra.utils.DateUtils;
import com.kaltura.live.model.aggregation.dao.LiveEntryEventDAO;
import com.kaltura.live.model.aggregation.dao.PartnerEventDAO;
import com.kaltura.live.webservice.model.AnalyticsException;
import com.kaltura.live.webservice.model.LiveReportInputFilter;
import com.kaltura.live.webservice.model.LiveStats;
import com.kaltura.live.webservice.model.LiveStatsListResponse;

public class PartnerTotalReporter extends BaseReporter {
	
	@Override
	public LiveStatsListResponse query(LiveReportInputFilter filter) {

		if(filter.isLive())
			return generateLiveReport(filter);
		else
			return generatePastReport(filter);
	}
	
	protected String generateLiveEntriesQuery(LiveReportInputFilter filter) {
		StringBuffer sb = new StringBuffer();
		sb.append("select * from kaltura_live.live_events where ");
		sb.append(addEntryIdsCondition(filter.getEntryIds()));
		sb.append(" and ");
		sb.append(addNowCondition());
		sb.append(";");
		
		String query = sb.toString();
		logger.debug(query);
		return query;
	}
	
	private LiveStatsListResponse generateLiveReport(
			LiveReportInputFilter filter) {
		String query = generateLiveEntriesQuery(filter);
		ResultSet results = session.getSession().execute(query);
		
		Iterator<Row> itr = results.iterator();
		
		long audience = 0;
		long secondsViewed = 0;
		long bufferTime = 0;
		long bitRate = 0;
		long bitrateCount = 0;
		while(itr.hasNext()) {
			LiveEntryEventDAO dao = new LiveEntryEventDAO(itr.next());
			audience += dao.getAlive();
			secondsViewed += dao.getAlive() * 10;
			bufferTime += dao.getBufferTime();
			bitRate += dao.getBitrate();
			bitrateCount += dao.getBitrateCount();
			
		}
		
		float avgBitrate = 0;
		if(bitrateCount > 0)
			avgBitrate = bitRate / bitrateCount;
		
		LiveStats entry = new LiveStats(0, audience, secondsViewed, bufferTime, avgBitrate, 0, 0);
		
		List<LiveStats> result = new ArrayList<LiveStats>();
		result.add(entry);
		return new LiveStatsListResponse(result);
	}

	protected String generatePastPartnerQuery(LiveReportInputFilter filter) {
		StringBuffer sb = new StringBuffer();
		sb.append("select * from kaltura_live.hourly_live_events_partner where partner_id = ");
		sb.append(filter.getPartnerId());
		sb.append(" and ");
		sb.append(addHoursBeforeCondition(DateUtils.getCurrentTime().getTime(), filter.getHoursBefore()));
		sb.append(";");
		
		String query = sb.toString();
		logger.debug(query);
		return query;
	}

	private LiveStatsListResponse generatePastReport(
			LiveReportInputFilter filter) {
		String query = generatePastPartnerQuery(filter);
		ResultSet results = session.getSession().execute(query);
		
		Iterator<Row> itr = results.iterator();
		List<LiveStats> result = new ArrayList<LiveStats>();
		while(itr.hasNext()) {
			PartnerEventDAO dao = new PartnerEventDAO(itr.next());
			float avgBitrate = 0;
			if(dao.getBitrateCount() > 0)
				avgBitrate = dao.getBitrate() / dao.getBitrateCount();
			LiveStats event = new LiveStats(dao.getPlays(), dao.getAlive(), dao.getAlive()* 10, dao.getBufferTime(),
					avgBitrate, dao.getEventTime().getTime(), 0);
			result.add(event);
		}
		
		return new LiveStatsListResponse(result);
	}
	
	@Override
	public void validateFilter(LiveReportInputFilter filter) throws AnalyticsException {
		
		String validation = "";
		
		if(filter.isLive()) {
			if(filter.getEntryIds() == null)
				validation = "Entry Ids can't be null. ";
		} else {
			if(filter.getPartnerId() < 0)
				validation += "Partner Id must be a positive number.";
			
			if(filter.getHoursBefore() < 0)
				validation += "Hourse before must be a positive number.";
		}
		
		if(!validation.isEmpty())
			throw new AnalyticsException("Illegal filter input: " + validation);
	}
	
	
}