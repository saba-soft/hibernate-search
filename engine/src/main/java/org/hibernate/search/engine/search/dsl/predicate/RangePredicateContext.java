/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.dsl.predicate;


/**
 * The context used when starting to define a range predicate.
 */
public interface RangePredicateContext extends SearchPredicateNoFieldContext<RangePredicateContext> {

	/**
	 * Target the given field in the range predicate.
	 * <p>
	 * Multiple fields may be targeted by the same predicate:
	 * the predicate will match if <em>any</em> targeted field matches.
	 * <p>
	 * When targeting multiple fields, those fields must have compatible types.
	 * See <a href="SearchPredicateFactoryContext.html#commonconcepts-parametertype">there</a> for more information.
	 *
	 * @param absoluteFieldPath The absolute path (from the document root) of the targeted field.
	 * @return A {@link RangePredicateFieldSetContext} allowing to define field-specific settings
	 * (such as the {@link RangePredicateFieldSetContext#boostedTo(float) boost}),
	 * or simply to continue the definition of the range predicate
	 * ({@link RangePredicateFieldSetContext#from(Object) bounds}, ...).
	 */
	default RangePredicateFieldSetContext onField(String absoluteFieldPath) {
		return onFields( absoluteFieldPath );
	}

	/**
	 * Target the given fields in the range predicate.
	 * <p>
	 * Equivalent to {@link #onField(String)} followed by multiple calls to
	 * {@link RangePredicateFieldSetContext#orField(String)},
	 * the only difference being that calls to {@link RangePredicateFieldSetContext#boostedTo(float)}
	 * and other field-specific settings on the returned context will only need to be done once
	 * and will apply to all the fields passed to this method.
	 *
	 * @param absoluteFieldPaths The absolute paths (from the document root) of the targeted fields.
	 * @return A {@link RangePredicateFieldSetContext} (see {@link #onField(String)} for details).
	 *
	 * @see #onField(String)
	 */
	RangePredicateFieldSetContext onFields(String ... absoluteFieldPaths);

	/**
	 * Alternative version of {@link #onField(String)} to target the given field in the range predicate.
	 * <p>
	 * Using this method it is possible to bypass any {@code DslConverter} defined on the field,
	 * in order to provide range boundary values within ({@code from}, {@code to}, {@code below}, {@code above})
	 * using the same format the date field is stored on the backend.
	 * <p>
	 * If no {@code DslConverter} are defined on the field,
	 * it will have the same behaviour of {@link #onField(String)}.
	 *
	 * @param absoluteFieldPath The absolute path (from the document root) of the targeted field.
	 * @return A {@link RangePredicateFieldSetContext} (see {@link #onField(String)} for details).
	 */
	default RangePredicateFieldSetContext onRawField(String absoluteFieldPath) {
		return onRawFields( absoluteFieldPath );
	}

	/**
	 * Alternative version of {@link #onFields(String...)} to target the given fields in the range predicate.
	 * <p>
	 * Equivalent to {@link #onRawField(String)} followed by multiple calls to
	 * {@link RangePredicateFieldSetContext#orRawField(String)},
	 * the only difference being that calls to {@link RangePredicateFieldSetContext#boostedTo(float)}
	 * and other field-specific settings on the returned context will only need to be done once
	 * and will apply to all the fields passed to this method.
	 *
	 * @param absoluteFieldPaths The absolute paths (from the document root) of the targeted fields.
	 * @return A {@link RangePredicateFieldSetContext} (see {@link #onField(String)} for details).
	 *
	 * @see #onRawField(String)
	 * @see #onFields(String...)
	 * @see #onField(String)
	 */
	RangePredicateFieldSetContext onRawFields(String... absoluteFieldPaths);
}