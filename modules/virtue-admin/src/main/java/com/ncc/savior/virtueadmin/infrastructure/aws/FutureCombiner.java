/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to combine {@link CompletableFuture}s of a certain class and
 * creates a future that will return the collection of those instances returned
 * from the original futures.
 * 
 *
 * @param <T>
 */
public class FutureCombiner<T> {
	private static final Logger logger = LoggerFactory.getLogger(FutureCombiner.class);

	private Collection<CompletableFuture<T>> futures;
	private List<T> results;

	public FutureCombiner() {
		this.futures = new ArrayList<CompletableFuture<T>>();
		this.results = Collections.synchronizedList(new ArrayList<T>());
	}

	public void addFuture(CompletableFuture<T> cf) {
		futures.add(cf);

	}

	public CompletableFuture<Collection<T>> combineFutures(CompletableFuture<Collection<T>> future) {
		CompletableFuture<Void> current = CompletableFuture.completedFuture(null);
		for (CompletableFuture<T> f : futures) {
			Consumer<? super T> action = new Consumer<T>() {

				@Override
				public void accept(T t) {
					logger.debug("adding " + t);
					results.add(t);
				}
			};
			CompletableFuture<Void> cf2 = f.thenAccept(action);
			current = current.thenAcceptBoth(cf2, (v1, v2) -> {
			});
			f.exceptionally((th) -> {
				future.completeExceptionally(th);
				return null;
			});
		}

		current.thenAccept((Void) -> {
			future.complete(results);
		});
		return future;
	}

}
