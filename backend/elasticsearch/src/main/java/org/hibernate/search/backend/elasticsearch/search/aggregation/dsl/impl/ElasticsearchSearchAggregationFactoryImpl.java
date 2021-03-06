/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.aggregation.dsl.impl;

import org.hibernate.search.backend.elasticsearch.search.aggregation.impl.ElasticsearchSearchAggregationBuilderFactory;
import org.hibernate.search.backend.elasticsearch.search.aggregation.dsl.ElasticsearchSearchAggregationFactory;
import org.hibernate.search.backend.elasticsearch.search.predicate.dsl.ElasticsearchSearchPredicateFactory;
import org.hibernate.search.engine.search.aggregation.dsl.AggregationFinalStep;
import org.hibernate.search.engine.search.aggregation.dsl.SearchAggregationFactory;
import org.hibernate.search.engine.search.aggregation.dsl.spi.DelegatingSearchAggregationFactory;
import org.hibernate.search.engine.search.aggregation.dsl.spi.SearchAggregationDslContext;

import com.google.gson.JsonObject;

public class ElasticsearchSearchAggregationFactoryImpl
		extends DelegatingSearchAggregationFactory<ElasticsearchSearchPredicateFactory>
		implements ElasticsearchSearchAggregationFactory {

	private final SearchAggregationDslContext<ElasticsearchSearchAggregationBuilderFactory, ?> dslContext;

	public ElasticsearchSearchAggregationFactoryImpl(SearchAggregationFactory delegate,
			SearchAggregationDslContext<ElasticsearchSearchAggregationBuilderFactory, ElasticsearchSearchPredicateFactory> dslContext) {
		super( delegate, dslContext );
		this.dslContext = dslContext;
	}

	@Override
	public AggregationFinalStep<JsonObject> fromJson(JsonObject jsonObject) {
		return new ElasticsearchJsonAggregationFinalStep(
				dslContext.builderFactory().fromJson( jsonObject )
		);
	}

	@Override
	public AggregationFinalStep<JsonObject> fromJson(String jsonString) {
		return new ElasticsearchJsonAggregationFinalStep(
				dslContext.builderFactory().fromJson( jsonString )
		);
	}

	// TODO HSEARCH-3661/HSEARCH-3662 implement extensions to the aggregation DSL for Elasticsearch

}
