package com.vidiun.live.webservice.model;

public class EntryLiveStats extends LiveStats {
	
	protected String entryId;
	protected long peakAudience;
    protected long peakDvrAudience;
	
	public EntryLiveStats() {
		super();
	}

	public EntryLiveStats(long plays, long audience, long dvrAudience, long secondsViewed,
			float bufferTime, float avgBitrate, long timestamp,
			String entryId) {
		super(plays, audience, dvrAudience, secondsViewed, bufferTime, avgBitrate,
				timestamp);
		this.entryId = entryId;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public void setPeakAudience(long peakAudience) {
		this.peakAudience = peakAudience;
	}
	
	public long getPeakAudience() {
		return peakAudience;
	}

    public void setPeakDvrAudience(long peakDvrAudience) {
        this.peakDvrAudience = peakDvrAudience;
    }

    public long getPeakDvrAudience() {
        return peakDvrAudience;
    }
}
