/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.loading.spi;

import java.util.List;
import java.util.Set;

import org.hibernate.search.engine.common.timing.spi.Deadline;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeIdentifier;

/**
 * A loader of POJO entities from an external source (database, ...).
 *
 * @param <E> A supertype of the type of loaded entities.
 */
public interface PojoLoader<E> {

	/**
	 * Loads the entities corresponding to the given identifiers, blocking the current thread while doing so.
	 *
	 * @param identifiers A list of identifiers for objects to load.
	 * @param deadline The deadline for loading the entities, or null if there is no deadline.
	 * @return A list of entities, in the same order the references were given.
	 * {@code null} is inserted when an object is not found or has an excluded types
	 * (see {@link org.hibernate.search.mapper.pojo.loading.spi.PojoLoadingContext#createLoader(Set)}).
	 */
	List<?> loadBlocking(List<?> identifiers, Deadline deadline);

	/**
	 * Casts a loaded entity returned by {@link #loadBlocking(List, Deadline)}
	 * to the expected type, or returns {@code null} if it has an unexpected type.
	 * <p>
	 * Under some circumstances, the loading may return entities that do not have the exact type expected by users.
	 * <p>
	 * For example, let's consider entity types A, B, C, D, with B, C, and D extending A
	 * Let's imagine an instance of type B and with id 4 is deleted from the database
	 * and replaced with an instance of type D and id 4.
	 * If a search on entity types B and C is performed before the index is refreshed,
	 * we might be requested to load entity B with id 4,
	 * and since we're working with the common supertype A,
	 * loading will succeed but will yield an entity of type D with id 4.
	 * <p>
	 * Now, the entity will still be an instance of A, but... the user doesn't care about A:
	 * the user asked for a search on entities B and C.
	 * Returning D might be a problem, especially if the user intends to call methods defined on an interface I,
	 * implemented by B and C, but not D.
	 * This will be a problem since that entity does not implement I.
	 * <p>
	 * The easiest way to avoid this problem is to just check the type of every loaded entity,
	 * to be sure it's the same type that was originally requested.
	 * Then we will be safe, because callers are expected to only pass entity references
	 * to types that were originally targeted by the search,
	 * and these types are known to implement any interface that the user could possibly rely on.
	 *
	 * @param <E2> The expected type for the entity instance.
	 * @param expectedType The expected type for the entity instance. Must be one of the types passed
	 * to {@link org.hibernate.search.mapper.pojo.loading.spi.PojoLoadingContext#createLoader(Set)}
	 * when creating this loader.
	 * @param loadedObject A loaded object, i.e. an element retrieved from the list
	 * returned by {@link #loadBlocking(List, Deadline)}.
	 * @return {@code true} if the loaded object is an instance of the expected type or a subtype.
	 */
	<E2 extends E> E2 castToExactTypeOrNull(PojoRawTypeIdentifier<E2> expectedType, Object loadedObject);

}
