package com.vidiun.live.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import com.vidiun.live.webservice.model.AnalyticsException;
import com.vidiun.live.webservice.model.LiveEntriesListResponse;
import com.vidiun.live.webservice.model.LiveEventsListResponse;
import com.vidiun.live.webservice.model.LiveReportInputFilter;
import com.vidiun.live.webservice.model.LiveReportPager;
import com.vidiun.live.webservice.model.LiveReportType;
import com.vidiun.live.webservice.model.LiveStatsListResponse;

@WebService
@SOAPBinding(style = Style.RPC)
public interface LiveAnalytics{
	
	@WebMethod 
	@RequestWrapper(localName = "getReportRequest")
	@ResponseWrapper(localName = "getReportResponse")
	public LiveStatsListResponse getReport(
			@WebParam(name = "reportType")
			LiveReportType reportType,
			@WebParam(name = "filter")
			LiveReportInputFilter filter,
			@WebParam(name = "pager")
			LiveReportPager pager) throws AnalyticsException;
	
	@WebMethod 
	@RequestWrapper(localName = "getEventsRequest")
	@ResponseWrapper(localName = "getEventsResponse")
	public LiveEventsListResponse getEvents(
			@WebParam(name = "reportType")
			LiveReportType reportType,
			@WebParam(name = "filter")
			LiveReportInputFilter filter,
			@WebParam(name = "pager")
			LiveReportPager pager) throws AnalyticsException;
	
	@WebMethod
	@RequestWrapper(localName = "getLiveEntriesRequest")
	@ResponseWrapper(localName = "getLiveEntriesResponse")
	public LiveEntriesListResponse getLiveEntries(
			@WebParam(name = "partnerId")
			Integer partnerId);
	
}