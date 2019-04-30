package com.vidiun.live.model.aggregation;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vidiun.live.infra.utils.DateUtils;
import com.vidiun.live.infra.utils.RequestUtils;
import com.vidiun.ip2location.Ip2LocationRecord;
import com.vidiun.ip2location.SerializableIP2LocationReader;

/**
 *	Represents a single stats event 
 */

public class StatsEvent implements Serializable {
	
	
	private static final long serialVersionUID = 2087162345237773624L;
	
	private static Logger LOG = LoggerFactory.getLogger(StatsEvent.class);
	
	/** Regular expression representing the apache log format */
	public static Pattern apacheLogRegex = Pattern.compile(
			"^([\\d.]+) \\[([\\w\\d:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) \"([^\"]+)\".*");
	
	/** Stats events fields */
	private Date eventTime = new Date(0);
	private int partnerId = 0;
	private String entryId = "N/A";
	private String country = "N/A";
	private String city = "N/A";
	private String referrer = "N/A";
	private long plays = 0;
	private long alive = 0;
    private long dvrAlive = 0;
	private long bitrate = 0;
	private long bitrateCount = 0;
	private double bufferTime = 0;
	private String ipAddress;





	/**
	 * Constructor by fields 
	 */
	public StatsEvent(Date eventTime, int partnerId, String entryId, String country, String city,  String referrer, long plays,
					  long alive, long dvrAlive, long bitrate, long bitrateCount, double bufferTime) {
		this.eventTime = eventTime;
		this.partnerId = partnerId;
		this.entryId = entryId;
		this.country = country;
		this.city = city;
		this.plays = plays;
	    this.alive = alive;
        this.dvrAlive = dvrAlive;
	    this.bitrate = bitrate;
	    this.bitrateCount = bitrateCount;
	    this.bufferTime = bufferTime;
	    this.referrer = referrer;
	}

	/**
	 * This function parses a single apache log line and creates a single stats event from it
	 * @param line
	 * @param reader
	 */
	public StatsEvent(String line , SerializableIP2LocationReader reader) {
		Matcher m = apacheLogRegex.matcher(line);
		
        if (m.find()) {
        	String date = m.group(2);
            eventTime = DateUtils.roundDate(date);
            
        	String query = m.group(3);
            try {
				query = URLDecoder.decode(query, "UTF-8");
				// remove the HTTP protocol at the end of the query string
				int querySuffixIndex = query.lastIndexOf(" HTTP/");
				if (querySuffixIndex >= 0)
					query = query.substring(0, querySuffixIndex);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.warn("Failed to decode query string: " + query, e );
			}

			if (query.toLowerCase().indexOf("service=livestats") > -1 && query.toLowerCase().indexOf("action=collect") > -1) {
            //if (query.indexOf("service=LiveStats") > -1 && query.indexOf("action=collect") > -1) {
	            ipAddress = m.group(1);
	           
	            try {
	            		Ip2LocationRecord ipRecord =  reader.getAll(ipAddress);
	            		country = ipRecord.getCountryLong();
	            		city = ipRecord.getCity();
	            	
	            } catch (Exception e) {
	            	LOG.error("Failed to parse IP", e);
	            	country = "N/A";
	            	city = "N/A";
	            }
	           
	            if (country == null) {
	            	country = "N/A";
	            }
	            if (city == null) {
	            	city = "N/A";
	            }
	            
	            Map<String, String> paramsMap = RequestUtils.splitQuery(query);
	            if (paramsMap != null && paramsMap.size() > 0) {
	            	try {
						entryId = paramsMap.containsKey("event:entryId") ? paramsMap.get("event:entryId") : "N/A";
						partnerId = Integer.parseInt(paramsMap.containsKey("event:partnerId") ? paramsMap.get("event:partnerId") : "0");
						bufferTime = Double.parseDouble(paramsMap.containsKey("event:bufferTime") ? paramsMap.get("event:bufferTime") : "0");

						bitrate = Long.parseLong(paramsMap.containsKey("event:bitrate") ? paramsMap.get("event:bitrate") : "-1");
						referrer = paramsMap.containsKey("event:referrer") ? paramsMap.get("event:referrer") : "N/A";
						bitrateCount = 1;
						if (bitrate < 0) {
							bitrate = 0;
							bitrateCount = 0;
						}
						int eventIndex = Integer.parseInt(paramsMap.containsKey("event:eventIndex") ? paramsMap.get("event:eventIndex") : "0");

						int eventTypeInt = Integer.parseInt(paramsMap.containsKey("event:eventType") ? paramsMap.get("event:eventType") : "1");
                        LiveEventType eventType = (eventTypeInt ==  1) ? LiveEventType.LIVE_EVENT: LiveEventType.DVR_EVENT;

						if ( eventType == LiveEventType.LIVE_EVENT )
						{
							plays = eventIndex == 1 ? 1 : 0;
							alive = eventIndex > 1 ? 1 : 0;
						}
						else
						{
							dvrAlive = eventIndex > 1 ? 1 : 0;
						}


						int seconds = 5; // default 5 so that offset is 0
						if ( paramsMap.containsKey("event:startTime") )
						{
							String clientEventTime = paramsMap.get("event:startTime");

							if ( clientEventTime.length() > 10 )
							{
//								int gmtPos = clientEventTime.length() - 3;
//
//								if ( clientEventTime.substring(gmtPos).equalsIgnoreCase("GMT") )

								//this check is for avoiding fault when using old viewer sending long startTime format
								if ( clientEventTime.lastIndexOf(':') >= 0 )
								{
									String secondsString = clientEventTime.substring(clientEventTime.lastIndexOf(':') + 1,
											clientEventTime.lastIndexOf(' '));
									seconds = Integer.parseInt(secondsString);
								}
							}
						}
						//String clientEventTime = paramsMap.containsKey("event:startTime") ? paramsMap.get("event:startTime") : "0";

			            //Calendar calendar = Calendar.getInstance();
			            //calendar.setTimeInMillis(clientEventTime);
			            //int seconds = calendar.get(Calendar.SECOND);
			            //seconds = seconds % 10;
			            int secondsLastDigit = seconds % 10;
						int offset = 5 - secondsLastDigit;
			            
			            eventTime = DateUtils.roundDate(date, offset);
			            
	            	} catch (NumberFormatException ex) {
	            		LOG.error("Failed to parse line " + line );
	            	}
	            }
            }
        }
        
	}
	
	
	/**
	 * Merges two stats events into a single one
	 * @param other The other stats events we'd like to merge with
	 * @return The merged stats events
	 */
	public StatsEvent merge(StatsEvent other) {
		return new StatsEvent(eventTime, partnerId, entryId, country, city, referrer, plays + other.plays,
				alive + other.alive, dvrAlive + other.dvrAlive, bitrate + other.bitrate, bitrateCount + other.bitrateCount,
				bufferTime + other.bufferTime);
	}

    public StatsEvent maxAudience(StatsEvent other) {
        return new StatsEvent(eventTime, partnerId, entryId, "", "", "", 0, Math.max(alive, +other.alive), Math.max(dvrAlive, other.dvrAlive), 0, 0, 0);
    }

	public Date getEventTime() {
		return this.eventTime;
	}
	
	public int getPartnerId() {
		return this.partnerId;
	}
	
	public String getEntryId() {
		return entryId;
	}
	
	public String getCountry() {
		return this.country;
	}
	
	public void setCountry(String c) {
		this.country = c;
	}
	
	public String getCity() {
		return this.city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getReferrer()
	{
		return this.referrer;
	}
	
	public long getPlays() {
		return this.plays;
	}
	
	public long getAlive() {
		return this.alive;
	}

    public long getDVRAlive() {
        return this.dvrAlive;
    }

	public long getBitrate() {
		return this.bitrate;
	}
	
	public long getBitrateCount() {
		return this.bitrateCount;
	}
	
	public double getBufferTime() {
		return this.bufferTime;
	}
	
	public String getIpAddress() {
		return this.ipAddress;
	}
	
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public void setPlays(long plays) {
		this.plays = plays;
	}

	public void setAlive(long alive) {
		this.alive = alive;
	}

	public void setBitrate(long bitrate) {
		this.bitrate = bitrate;
	}

	public void setBitrateCount(long bitrateCount) {
		this.bitrateCount = bitrateCount;
	}

	public void setBufferTime(long bufferTime) {
		this.bufferTime = bufferTime;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	@Override
	public String toString() {
		return "StatsEvent [eventTime=" + eventTime + ", partnerId="
				+ partnerId + ", entryId=" + entryId + ", country=" + country
				+ ", city=" + city + ", referrer=" + referrer + ", plays="
				+ plays + ", alive=" + alive + ", bitrate=" + bitrate
				+ ", bitrateCount=" + bitrateCount + ", bufferTime="
				+ bufferTime + ", ipAddress=" + ipAddress + "]";
	}
	
	
 }
