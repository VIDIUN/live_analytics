Installation guide:
-------------------
1. Copy the configuration file from : VidiunLiveInfra/configuration/config.properties
	to a path of your choice, and expose it as a system variable named VIDIUN_CONF_PATH
	Remember to update the configuration according to your system configuration.
2. Install tomcat (preferably 7)
3. Configure Tomcat (see section 'TomCat Configuration')
4. Export WAR file (Soon to be a maven script)
5. Deploy the war {$TOMCAT}/webapps
6. Verify web service was deplyed succesfully by entering - http://<tom-cat host>:<tomcat port>/VidiunLiveAnalytics/VidiunLiveAnalytics
7. Set the host name and execute the test client.

System variables:
----------------------
In order to expose a system variable, create a file named - /etc/profile.d/vidiun.sh
and add the following content:
VIDIUN_CONF_PATH=@CONFIGURATION_DIR@
export VIDIUN_CONF_PATH

Restart the machine and the exposure will hold.

Tomcat configuration:
----------------------
1. Go here http://jax-ws.java.net/.
2. Download JAX-WS RI distribution.
3. Unzip it and copy following JAX-WS dependencies to Tomcat library folder “{$TOMCAT}/lib“.
	gmbal-api-only.jar
	ha-api.jar
	jaxb-core.jar
	jaxb-impl.jar
	jaxws-api.jar
	jaxws-rt.jar
	management-api.jar
	policy.jar
	stax-ex.jar
	streambuffer.jar
	
Logs view:
-----------
Or: I don't see logs, what should i do?
Edit the log4j properties to debug, and re-deploy.
