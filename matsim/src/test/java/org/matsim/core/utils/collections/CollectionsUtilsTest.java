/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.utils.collections;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mrieser
 */
public class CollectionsUtilsTest {

	private final static Logger log = Logger.getLogger(CollectionsUtilsTest.class);

	@Test
	public void testSetToString() {
		Set<String> set = new LinkedHashSet<String>();
		set.add("Aaa");
		set.add("Bbb");
		set.add("Ddd");
		set.add("Ccc");
		Assert.assertEquals("Aaa,Bbb,Ddd,Ccc", CollectionUtils.setToString(set));
	}

	@Test
	public void testArrayToString() {
		String[] array = new String[] {"Aaa", "Bbb", "Ddd", "Ccc"};
		Assert.assertEquals("Aaa,Bbb,Ddd,Ccc", CollectionUtils.arrayToString(array));
	}

	@Test
	public void testStringToSet() {
		String[] testStrings = new String[] {
				"Aaa,Bbb,Ddd,Ccc",
				",Aaa,Bbb,Ddd,Ccc",
				"Aaa,Bbb,Ddd,Ccc,",
				" ,Aaa,Bbb,Ddd,Ccc, ",
				" , Aaa , Bbb , Ddd , Ccc , ",
				" , Aaa ,	Bbb ,		Ddd , Ccc , ",
				",,, Aaa ,	Bbb ,,		Ddd , Ccc ,,",
				" ,, , Aaa ,	Bbb ,,		Ddd , Ccc ,, ",
			};
		for (String str : testStrings) {
			log.info("testing String: " + str);
			Set<String> set = CollectionUtils.stringToSet(str);
			Assert.assertEquals(4, set.size());
			Iterator<String> iter = set.iterator();
			Assert.assertEquals("Aaa", iter.next());
			Assert.assertEquals("Bbb", iter.next());
			Assert.assertEquals("Ddd", iter.next());
			Assert.assertEquals("Ccc", iter.next());
			Assert.assertFalse(iter.hasNext());
		}
	}

	@Test
	public void testStringToArray() {
		String[] testStrings = new String[] {
				"Aaa,Bbb,Ddd,Ccc",
				",Aaa,Bbb,Ddd,Ccc",
				"Aaa,Bbb,Ddd,Ccc,",
				" ,Aaa,Bbb,Ddd,Ccc, ",
				" , Aaa , Bbb , Ddd , Ccc , ",
				" , Aaa ,	Bbb ,		Ddd , Ccc , ",
				",,, Aaa ,	Bbb ,,		Ddd , Ccc ,,",
				" ,, , Aaa ,	Bbb ,,		Ddd , Ccc ,, ",
			};
		for (String str : testStrings) {
			log.info("testing String: " + str);
			String[] array = CollectionUtils.stringToArray(str);
			Assert.assertEquals(4, array.length);
			Assert.assertEquals("Aaa", array[0]);
			Assert.assertEquals("Bbb", array[1]);
			Assert.assertEquals("Ddd", array[2]);
			Assert.assertEquals("Ccc", array[3]);
		}
	}
}
