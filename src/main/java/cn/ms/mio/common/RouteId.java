package cn.ms.mio.common;

import java.util.Set;

import cn.ms.neural.util.micro.ConcurrentHashSet;

/**
 * The Route Id.
 * 
 * @author lry
 */
public class RouteId {

	public static final String SERVICE_KEY = "Service";
	public static final String VERSION_KEY = "Version";
	public static final String GROUP_KEY = "Group";

	public static final String VERSION_DEFVAL = "1.0.0";
	public static final String GROUP_DEFVAL = "def";
	public static final String ACCESSID_SEQ = ":";
	public static final String ANY_VALUE = "*";

	private String service;
	private String version = VERSION_DEFVAL;
	private String group = GROUP_DEFVAL;

	public RouteId(String service) {
		this.service = service;
	}

	public RouteId(String service, String version) {
		this.service = service;
		this.version = version;
	}

	public RouteId(String service, String version, String group) {
		this.service = service;
		this.version = version;
		this.group = group;
	}

	public String toRouteId() {
		return getService() + ACCESSID_SEQ + getVersion() + ACCESSID_SEQ
				+ getGroup();
	}

	public Set<String> toRouteIds() {
		Set<String> keys = new ConcurrentHashSet<String>();
		// serviceId:*:*
		keys.add(getService() + ACCESSID_SEQ + ANY_VALUE + ACCESSID_SEQ + ANY_VALUE);
		if (!ANY_VALUE.equals(getVersion()) || !ANY_VALUE.equals(getGroup())) {
			if (ANY_VALUE.equals(getVersion())) {// serviceId:*:group
				keys.add(getService() + ACCESSID_SEQ + ANY_VALUE + ACCESSID_SEQ + getGroup());
			} else if (ANY_VALUE.equals(getGroup())) {// serviceId:version:*
				keys.add(getService() + ACCESSID_SEQ + getVersion() + ACCESSID_SEQ + ANY_VALUE);
			} else {// serviceId:verison:group ==> serviceId:verison:group、serviceId:*:group、serviceId:version:*
				keys.add(getService() + ACCESSID_SEQ + getVersion() + ACCESSID_SEQ + getGroup());
				keys.add(getService() + ACCESSID_SEQ + ANY_VALUE + ACCESSID_SEQ + getGroup());
				keys.add(getService() + ACCESSID_SEQ + getVersion() + ACCESSID_SEQ + ANY_VALUE);
			}
		}

		return keys;
	}

	// setter、getter
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public String toString() {
		return "AccessId [service=" + service + ", version=" + version
				+ ", group=" + group + "]";
	}

}
