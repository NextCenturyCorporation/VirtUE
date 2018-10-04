package com.ncc.savior.virtueadmin.infrastructure.subnet;

import com.amazonaws.services.cognitoidentity.model.InvalidParameterException;
import com.ncc.savior.util.JavaUtil;

public class CidrBlock {

	private static final String DOT = ".";
	private static final String SLASH = "/";
	private int a;
	private int b;
	private int c;
	private int d;
	private int netmask;

	public CidrBlock(int a, int b, int c, int d, int netmask) {
		if (!validRange(a, b, c, d, netmask)) {
			String cidr = a + DOT + b + DOT + c + DOT + d + SLASH + netmask;
			throw new InvalidParameterException("'" + cidr + "' is not a valid CIDR String.");
		}
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.netmask = netmask;
	}

	public static CidrBlock fromString(String cidrString) {
		int a = 0, b = 0, c = 0, d = 0, netmask = 0;
		if (JavaUtil.isNotEmpty(cidrString)) {
			cidrString = cidrString.trim();
			String[] slashSplit = cidrString.split(SLASH);
			if (slashSplit.length == 2) {
				String netmaskString = slashSplit[1];
				netmask = Integer.parseInt(netmaskString);
				String ipString = slashSplit[0];
				String[] ipSplit = ipString.split("\\.");
				if (ipSplit.length == 4) {
					a = Integer.parseInt(ipSplit[0]);
					b = Integer.parseInt(ipSplit[1]);
					c = Integer.parseInt(ipSplit[2]);
					d = Integer.parseInt(ipSplit[3]);
					return new CidrBlock(a, b, c, d, netmask);
				}
			}
		}
		throw new InvalidParameterException("'" + cidrString + "' is not a valid CIDR String.");
	}

	private static boolean validRange(int a, int b, int c, int d, int netmask) {
		boolean valid = (a >= 0) && (a <= 255);
		valid &= (b >= 0) && (b <= 255);
		valid &= (c >= 0) && (c <= 255);
		valid &= (d >= 0) && (d <= 255);
		valid &= (netmask >= 1) && (netmask <= 32);
		return valid;
	}

	public static CidrBlock getNextCidrBlock(CidrBlock cidr) {
		int netmask = cidr.getNetmask();
		int a = cidr.getA() << 24;
		int b = cidr.getB() << 16;
		int c = cidr.getC() << 8;
		int d = cidr.getD();
		int ipAsInt = a + b + c + d;
		int bitsToChange = 32 - netmask;
		int addition = 1 << bitsToChange;
		
		int newIpAsInt = ipAsInt + addition;
//		System.out.println(Integer.toString(ipAsInt, 2));
//		System.out.println(Integer.toString(addition, 2));
//		System.out.println(Integer.toString(newIpAsInt, 2));
		return CidrBlock.fromIntegerAndMask(newIpAsInt, netmask);
	}

	private static CidrBlock fromIntegerAndMask(int ipAsInt, int netmask) {
		int d = ipAsInt & 255;
		int c = (ipAsInt >> 8) & 255;
		int b = (ipAsInt >> 16) & 255;
		int a = (ipAsInt >> 24) & 255;
		return new CidrBlock(a, b, c, d, netmask);
	}

	public int getA() {
		return a;
	}

	public int getB() {
		return b;
	}

	public int getC() {
		return c;
	}

	public int getD() {
		return d;
	}

	public int getNetmask() {
		return netmask;
	}

	@Override
	public String toString() {
		return a + DOT + b + DOT + c + DOT + d + SLASH + netmask;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CidrBlock other = (CidrBlock) obj;
		if (a != other.a)
			return false;
		if (b != other.b)
			return false;
		if (c != other.c)
			return false;
		if (d != other.d)
			return false;
		if (netmask != other.netmask)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + b;
		result = prime * result + c;
		result = prime * result + d;
		result = prime * result + netmask;
		return result;
	}

}
