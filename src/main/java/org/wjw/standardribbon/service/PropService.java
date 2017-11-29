package org.wjw.standardribbon.service;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface PropService {
	@Headers({ "Accept: */*" })
	@RequestLine("GET /{propname}")
	String getProp(@Param("propname") String propname);
	
}
