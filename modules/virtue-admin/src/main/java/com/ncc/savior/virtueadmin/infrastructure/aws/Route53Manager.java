package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.route53.AmazonRoute53Async;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;

/**
 * Wrapper class to simplify Route53 calls in AWS. Each instance can only impact
 * a single domain and that domain is added to all hostnames.
 * 
 *
 */
public class Route53Manager {
	private static final Logger logger = LoggerFactory.getLogger(Route53Manager.class);
	private AmazonRoute53Async client;
	private HostedZone hostedZone;
	private String hostedZoneId;
	private String domain;

	public Route53Manager(VirtueAwsEc2Provider ec2Provider, String domain) {
		client = ec2Provider.getRoute53Client();
		this.hostedZone = getHostedZoneForDomain(domain);
		if (hostedZone != null) {
			this.hostedZoneId = hostedZone.getId();
		}
		this.domain = domain;
	}

	// private void test() {
	// while (true) {
	// try {
	// Map<String, String> records = new TreeMap<String, String>();
	// records.put("test1", "192.168.0.1");
	// records.put("test2", "192.168.0.2");
	// records.put("test3", "192.168.0.3");
	// AddARecords(records);
	// deleteARecords(records.keySet());
	// JavaUtil.sleepAndLogInterruption(500);
	// } catch (Throwable t) {
	// logger.debug("error", t);
	//
	// }
	// }
	//
	// }

	public void deleteRecord(String dns) {
		String ip = getIpFromFromDns(dns);
		deleteRecord(dns, ip);
	}

	public void deleteARecords(Collection<String> hostnames) {
		Map<String, String> hostnamesToIp = getIpsFromHostnames(hostnames);
		deleteARecords(hostnamesToIp);
	}

	public Map<String, String> getIpsFromHostnames(Collection<String> hostnames) {
		// make copy so we can modify
		hostnames = new ArrayList<String>(hostnames);
		Map<String, String> hostnameToIps = new HashMap<String, String>();
		ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest();
		request.setHostedZoneId(hostedZoneId);
		request.setStartRecordType(RRType.A);
		request.setStartRecordName(domain);
		ListResourceRecordSetsResult result = client.listResourceRecordSets(request);
		for (ResourceRecordSet set : result.getResourceRecordSets()) {
			String nameLowerCase = set.getName();
			Iterator<String> itr = hostnames.iterator();
			while (itr.hasNext()) {
				String hostname = itr.next();
				String fqdn = getFqdnFromHostname(hostname);
				// logger.debug("Testing: " + nameLowerCase + " =?= " + fqdn);
				if (fqdn.equalsIgnoreCase(nameLowerCase)) {
					List<ResourceRecord> records = set.getResourceRecords();
					if (records.size() != 1) {
						logger.error("Getting IP for record=" + fqdn + " resulted in " + records.size() + " records.");
						continue;
					} else {
						String ip = records.get(0).getValue();
						hostnameToIps.put(hostname, ip);
						itr.remove();
						if (hostnames.isEmpty()) {
							return hostnameToIps;
						}
						break;
					}
				}
			}
		}
		if (!hostnames.isEmpty()) {
			logger.error("could not find record for all hostnames.  Hostnames without IPs=" + hostnames);
		}
		return hostnameToIps;
	}

	private String getFqdnFromHostname(String hostname) {
		if (!hostname.endsWith(domain)) {
			String dns = hostname + "." + domain;
			return dns;
		}
		return hostname;
	}

	public void deleteARecords(Map<String, String> records) {
		ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest().withHostedZoneId(hostedZoneId);
		ChangeBatch batch = new ChangeBatch().withComment("auto-generated via virtue server");
		for (Entry<String, String> entry : records.entrySet()) {
			String dns = entry.getKey();
			String ip = entry.getValue();
			batch.withChanges(getDeleteChange(dns, ip));
		}
		request.withChangeBatch(batch);
		client.changeResourceRecordSets(request);
	}

	public String getIpFromFromDns(String hostname) {
		String dns = hostname + "." + domain;
		ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest();
		request.setHostedZoneId(hostedZoneId);
		request.setStartRecordType(RRType.A);
		request.setStartRecordName(dns);
		ListResourceRecordSetsResult result = client.listResourceRecordSets(request);
		for (ResourceRecordSet set : result.getResourceRecordSets()) {
			if (set.getName().equalsIgnoreCase(dns)) {
				List<ResourceRecord> records = set.getResourceRecords();
				if (records.size() != 1) {
					logger.error("Getting IP for record=" + dns + " resulted in " + records.size() + " records.");
					return null;
				} else {
					return records.get(0).getValue();
				}
			}
		}
		logger.error("Unable to find IP for record=" + dns);
		return null;
	}

	private void deleteRecord(String dns, String ip) {
		ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest().withHostedZoneId(hostedZoneId)
				.withChangeBatch(new ChangeBatch().withComment("auto-generated via virtue server")
						.withChanges(getDeleteChange(dns, ip)));

		ChangeResourceRecordSetsResult result = client.changeResourceRecordSets(request);
		System.out.println(result.getChangeInfo().toString());
	}

	private HostedZone getHostedZoneForDomain(String domain) {
		try {
			ListHostedZonesResult hostedZonesResult = client.listHostedZones();
			List<HostedZone> hostedZones = hostedZonesResult.getHostedZones();
			for (HostedZone hz : hostedZones) {
				String name = hz.getName();
				if (domain.equals(name)) {
					return hz;
				}
			}
		} catch (Exception e) {
			logger.error("Error getting hosted zone for domain.  Using null", e);
		}
		return null;
	}

	public String AddARecord(String dns, String ip) {
		try {
			logger.debug("attemping to create A Record.  dns=" + dns + " ip=" + ip+ " hostedZoneId="+hostedZoneId);
			ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest()
					.withHostedZoneId(hostedZoneId).withChangeBatch(new ChangeBatch()
							.withComment("auto-generated via virtue server").withChanges(getAddARecordChange(dns, ip)));
			client.changeResourceRecordSets(request);
		} catch (RuntimeException e) {
			logger.error("failed to create A Record.  dns=" + dns + " ip=" + ip+ " hostedZoneId="+hostedZoneId, e);
		}
		return getFqdnFromHostname(dns);
	}

	public void AddARecords(Map<String, String> records) {
		ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest().withHostedZoneId(hostedZoneId);
		ChangeBatch batch = new ChangeBatch().withComment("auto-generated via virtue server");
		for (Entry<String, String> entry : records.entrySet()) {
			String dns = entry.getKey();
			String ip = entry.getValue();
			batch.withChanges(getAddARecordChange(dns, ip));
		}
		request.withChangeBatch(batch);
		client.changeResourceRecordSets(request);
	}

	private Change getDeleteChange(String hostname, String ip) {
		String dns = getFqdnFromHostname(hostname);
		return new Change().withAction(ChangeAction.DELETE).withResourceRecordSet(new ResourceRecordSet().withName(dns)
				.withType(RRType.A).withTTL(60L).withResourceRecords(new ResourceRecord().withValue(ip)));
	}

	private Change getAddARecordChange(String hostname, String ip) {
		String dns = getFqdnFromHostname(hostname);
		return new Change().withAction(ChangeAction.UPSERT).withResourceRecordSet(new ResourceRecordSet().withName(dns)
				.withType(RRType.A).withTTL(60L).withResourceRecords(new ResourceRecord().withValue(ip)));
	}
}
