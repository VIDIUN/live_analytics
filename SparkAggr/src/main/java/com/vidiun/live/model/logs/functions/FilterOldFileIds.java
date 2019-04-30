package com.vidiun.live.model.logs.functions;

import java.util.concurrent.TimeUnit;

import org.apache.spark.api.java.function.Function;

import com.vidiun.live.infra.utils.DateUtils;
import com.vidiun.live.infra.utils.LiveConfiguration;

public class FilterOldFileIds implements Function<String, Boolean> {

	private static final long serialVersionUID = -7006214542175672609L;

	@Override
	public Boolean call(String fileId) throws Exception {
		if (fileId.contains(String.valueOf(DateUtils.getCurrentHourInMillis() - TimeUnit.HOURS.toMillis(LiveConfiguration.instance().getHoursToSave())))) {
			return false;
		}
		return true;
	}
	

}
