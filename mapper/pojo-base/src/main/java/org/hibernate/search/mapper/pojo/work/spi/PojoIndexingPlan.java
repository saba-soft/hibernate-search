/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.work.spi;

import java.util.BitSet;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.engine.backend.work.execution.spi.IndexIndexingPlanExecutionReport;
import org.hibernate.search.mapper.pojo.model.path.spi.PojoPathFilter;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeIdentifier;

/**
 * An interface for indexing entities in the context of a session in a POJO mapper.
 * <p>
 * This class is stateful: it queues operations internally to apply them at a later time.
 * <p>
 * When {@link #process()} is called,
 * the entities will be processed and index documents will be built
 * and stored in an internal buffer.
 * <p>
 * When {@link #executeAndReport()} is called,
 * the operations will be actually sent to the index.
 * <p>
 * Note that {@link #executeAndReport()} will implicitly trigger processing of documents that weren't processed yet,
 * if any, so calling {@link #process()} is not necessary if you call {@link #executeAndReport()} just next.
 * <p>
 * Implementations may not be thread-safe.
 */
public interface PojoIndexingPlan<R> {

	/**
	 * Add an entity to the index, assuming that the entity is absent from the index.
	 * <p>
	 * <strong>Note:</strong> depending on the backend, this may lead to errors or duplicate entries in the index
	 * if the entity was actually already present in the index before this call.
	 * When in doubt, you should rather use {@link #addOrUpdate(PojoRawTypeIdentifier, Object, String, Object)}.
	 *
	 * @param typeIdentifier The identifier of the entity type.
	 * @param providedId A value to extract the document ID from.
	 * Generally the expected value is the entity ID, but a different value may be expected depending on the mapping.
	 * If {@code null}, Hibernate Search will attempt to extract the ID from the entity.
	 * @param providedRoutingKey The routing key to route the add request to the appropriate index shard.
	 * Leave {@code null} if sharding is disabled
	 * or to have Hibernate Search compute the value through the assigned {@link org.hibernate.search.mapper.pojo.bridge.RoutingBridge}.
	 * @param entity The entity to add to the index.
	 */
	void add(PojoRawTypeIdentifier<?> typeIdentifier, Object providedId, String providedRoutingKey, Object entity);

	/**
	 * Update an entity in the index, or add it if it's absent from the index.
	 *
	 * @param typeIdentifier The identifier of the entity type.
	 * @param providedId A value to extract the document ID from.
	 * Generally the expected value is the entity ID, but a different value may be expected depending on the mapping.
	 * If {@code null}, Hibernate Search will attempt to extract the ID from the entity.
	 * @param providedRoutingKey The routing key to route the addOrUpdate request to the appropriate index shard.
	 * Leave {@code null} if sharding is disabled
	 * or to have Hibernate Search compute the value through the assigned {@link org.hibernate.search.mapper.pojo.bridge.RoutingBridge}.
	 * @param entity The entity to update in the index.
	 */
	void addOrUpdate(PojoRawTypeIdentifier<?> typeIdentifier, Object providedId, String providedRoutingKey, Object entity);

	/**
	 * Update an entity in the index, or add it if it's absent from the index,
	 * but try to avoid reindexing if the given dirty paths
	 * are known not to impact the indexed form of that entity.
	 *
	 * @param typeIdentifier The identifier of the entity type.
	 * @param providedId A value to extract the document ID from.
	 * Generally the expected value is the entity ID, but a different value may be expected depending on the mapping.
	 * If {@code null}, Hibernate Search will attempt to extract the ID from the entity.
	 * @param providedRoutingKey The routing key to route the addOrUpdate request to the appropriate index shard.
	 * Leave {@code null} if sharding is disabled
	 * or to have Hibernate Search compute the value through the assigned {@link org.hibernate.search.mapper.pojo.bridge.RoutingBridge}.
	 * @param entity The entity to update in the index.
	 * @param dirtyPaths The paths to consider dirty, as a {@link BitSet}.
	 * You can build such a {@link BitSet} by obtaining the
	 * {@link org.hibernate.search.mapper.pojo.mapping.building.spi.PojoTypeExtendedMappingCollector#dirtyFilter(PojoPathFilter) dirty filter}
	 * for the entity type and calling one of the {@code filter} methods.
	 */
	void addOrUpdate(PojoRawTypeIdentifier<?> typeIdentifier, Object providedId, String providedRoutingKey,
			Object entity, BitSet dirtyPaths);

	/**
	 * Delete an entity from the index.
	 * <p>
	 * Entities to reindex as a result of this operation will not be resolved.
	 * <p>
	 * No effect on the index if the entity is not in the index.
	 *
	 * @param typeIdentifier The identifier of the entity type.
	 * @param providedId A value to extract the document ID from.
	 * Generally the expected value is the entity ID, but a different value may be expected depending on the mapping.
	 * If the provided ID is {@code null},
	 * Hibernate Search will attempt to extract the ID from the entity (which must be non-{@code null} in that case).
	 * @param providedRoutingKey The routing key to route the delete request to the appropriate index shard.
	 * Leave {@code null} if sharding is disabled,
	 * or if you don't use a custom {@link org.hibernate.search.mapper.pojo.bridge.RoutingBridge},
	 * or to have Hibernate Search compute the value through the assigned {@link org.hibernate.search.mapper.pojo.bridge.RoutingBridge}
	 * (requires a non-null {@code entity}).
	 * @param entity The entity to delete from the index. May be {@code null} if {@code providedId} is non-{@code null}.
	 * @throws IllegalArgumentException If both {@code providedId} and {@code entity} are {@code null}.
	 */
	void delete(PojoRawTypeIdentifier<?> typeIdentifier, Object providedId, String providedRoutingKey, Object entity);

	/**
	 * Extract all data from objects passed to the indexing plan so far,
	 * create documents to be indexed and put them into an internal buffer,
	 * without writing them to the indexes.
	 * <p>
	 * In particular, ensure that all data is extracted from the POJOs
	 * and converted to the backend-specific format.
	 * <p>
	 * Calling this method is optional: the {@link #executeAndReport()} method
	 * will perform the processing if necessary.
	 */
	void process();

	/**
	 * Write all pending changes to the index now,
	 * without waiting for a Hibernate ORM flush event or transaction commit,
	 * and clear the plan so that it can be re-used.
	 *
	 * @return A {@link CompletableFuture} that will be completed with an execution report when all the works are complete.
	 */
	CompletableFuture<IndexIndexingPlanExecutionReport<R>> executeAndReport();

	/**
	 * Discard all plans of indexing.
	 */
	void discard();

	/**
	 * Discard all plans of indexing, except for parts that were already {@link #process() processed}.
	 */
	void discardNotProcessed();

}
