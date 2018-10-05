package com.ncc.savior.virtueadmin.infrastructure.subnet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ncc.savior.virtueadmin.model.CidrBlock;

public class CidrBlockTest {

	@Test
	public void testNextCidrBlock1() {
		CidrBlock first = new CidrBlock(10, 0, 0, 1, 24);
		CidrBlock expectedSecond = new CidrBlock(10, 0, 1, 1, 24);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
	
	@Test
	public void testNextCidrBlock2() {
		CidrBlock first = new CidrBlock(10, 34, 56, 1, 24);
		CidrBlock expectedSecond = new CidrBlock(10, 34, 57, 1, 24);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
	
	@Test
	public void testNextCidrBlock3() {
		CidrBlock first = new CidrBlock(10, 0, 0, 1, 32);
		CidrBlock expectedSecond = new CidrBlock(10, 0, 0, 2, 32);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
	
	@Test
	public void testNextCidrBlock4() {
		CidrBlock first = new CidrBlock(10, 0, 0, 0, 28);
		CidrBlock expectedSecond = new CidrBlock(10, 0, 0,16, 28);
		CidrBlock second = CidrBlock.getNextCidrBlock(first);
		assertEquals(expectedSecond, second);
	}
}
