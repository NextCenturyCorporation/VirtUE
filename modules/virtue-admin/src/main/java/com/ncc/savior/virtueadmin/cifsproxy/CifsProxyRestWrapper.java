package com.ncc.savior.virtueadmin.cifsproxy;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.CifsShareCreationParameter;
import com.ncc.savior.virtueadmin.model.CifsVirtueCreationParameter;
import com.ncc.savior.virtueadmin.model.FileSystem;
import com.ncc.savior.virtueadmin.model.VirtueInstance;

public class CifsProxyRestWrapper {
	private static final Logger logger = LoggerFactory.getLogger(CifsProxyRestWrapper.class);
	private ObjectMapper jsonMapper;

	public CifsProxyRestWrapper() {
		jsonMapper = new ObjectMapper();
	}

	public CifsShareCreationParameter createShare(String cifsProxyHostname, String username, String password,
			String virtueId, FileSystem fs) {
		try {

			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("Content-Type", "application/json");
			String shareUrl = "http://" + cifsProxyHostname + ":8080/share";
			ObjectNode node = jsonMapper.createObjectNode();
			ArrayNode permissions = jsonMapper.createArrayNode();
			if (fs.getExecutePerm()) {
				permissions.add("EXECUTE");
			}
			if (fs.getReadPerm()) {
				permissions.add("READ");
			}
			if (fs.getWritePerm()) {
				permissions.add("WRITE");
			}
			logger.debug("**hardcode");

			String server = "EC2AMAZ-H6GG6ER";
			String path = "shared";

			node.put("name", fs.getName());
			node.put("virtueId", virtueId);
			// lower case hostname. IP will not work.
			node.put("server", server.toLowerCase());
			node.put("path", path);
			node.set("permissions", permissions);
			node.put("type", "CIFS");
			KerberosRestTemplate krt = new KerberosRestTemplate(null, username, password, null);
//			ResponseEntity<CifsShareCreationParameter> resp = krt.postForEntity(shareUrl, shareEntity,
//					CifsShareCreationParameter.class);
			CifsShareCreationParameter shareOutput = kerberosRequestWithRetries(krt, shareUrl, node.toString(), 3,
					CifsShareCreationParameter.class);
			logger.debug("CIFS returned " + shareOutput);
			return shareOutput;
		} catch (RestClientException e) {
			String msg = "Error creating share for CIFS Proxy";
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg, e);
		}

	}

	public CifsVirtueCreationParameter createVirtue(String cifsHostname, String username, String password,
			VirtueInstance virtue) {
		try {
			KerberosRestTemplate krt = new KerberosRestTemplate(null, username, password, null);

			CifsVirtueCreationParameter cscp = new CifsVirtueCreationParameter(virtue.getName(), virtue.getId());
			String virtueUrl = "http://" + cifsHostname + ":8080/virtue";

			String cscpStr = jsonMapper.writeValueAsString(cscp);

			CifsVirtueCreationParameter output = kerberosRequestWithRetries(krt, virtueUrl, cscpStr, 3,
					CifsVirtueCreationParameter.class);
			return output;
		} catch (IOException | RestClientException e) {
			String msg = "Error creating share for CIFS Proxy";
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg, e);
		}
	}

	public CifsVirtueCreationParameter getVirtueParams(String cifsHostname, String username, String password,
			String virtueId) {
		try {
			KerberosRestTemplate krt = new KerberosRestTemplate(null, username, password, null);
			String getVirtueUri = "http://" + cifsHostname + ":8080/virtue/" + virtueId;
			ResponseEntity<CifsVirtueCreationParameter> resp = krt.getForEntity(getVirtueUri,
					CifsVirtueCreationParameter.class);
			return resp.getBody();
		} catch (RestClientException e) {
			String msg = "Error deleting virtue='" + virtueId + "' for CIFS Proxy";
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg, e);
		}
	}

	public void deleteShare(String cifsHostname, String username, String password, String exportedShareName) {
		try {
			KerberosRestTemplate krt = new KerberosRestTemplate(null, username, password, null);
			String deleteShareUri = "http://" + cifsHostname + ":8080/share/" + exportedShareName;
			krt.delete(deleteShareUri);
		} catch (RestClientException e) {
			String msg = "Error deleting share='" + exportedShareName + "' for CIFS Proxy";
			logger.error(msg, e);
			// error will be thrown if no share exists. So we may want to ignore or check.
			// for now we'll ignore.
//			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg, e);
		}
	}

	public void deleteVirtue(String cifsHostname, String username, String password, String virtueId) {
		try {
			KerberosRestTemplate krt = new KerberosRestTemplate(null, username, password, null);
			String deleteVirtueUri = "http://" + cifsHostname + ":8080/virtue/" + virtueId;
			krt.delete(deleteVirtueUri);
		} catch (RestClientException e) {
			String msg = "Error deleting virtue='" + virtueId + "' for CIFS Proxy";
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg, e);
		}
	}

	private <R> R kerberosRequestWithRetries(KerberosRestTemplate krt, String virtueUrl, String body, int retries,
			Class<R> returnType) {
		MultiValueMap<String, String> headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		HttpEntity<String> virtueEntity = new HttpEntity<String>(body, headers);
		try {
			logger.debug("sending: " + body);
			ResponseEntity<R> resp = krt.postForEntity(virtueUrl, virtueEntity, returnType);
			R output = resp.getBody();
			logger.debug("response: " + output);
			return output;
		} catch (RestClientException e) {
			if (retries > 0) {
				logger.error("Error making kerberos request to CIFS Proxy.  Retrying", e);
				JavaUtil.sleepAndLogInterruption(1000);
				return kerberosRequestWithRetries(krt, virtueUrl, body, retries - 1, returnType);
			} else {
				String msg = "Error making kerberos request to CIFS Proxy.  url=" + virtueUrl + " error="
						+ e.getLocalizedMessage();
				throw new SaviorException(SaviorErrorCode.CIFS_PROXY_ERROR, msg);
			}
		}

	}

}
