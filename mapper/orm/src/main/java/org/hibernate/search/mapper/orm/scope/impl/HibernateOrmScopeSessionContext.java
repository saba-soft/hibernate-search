/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.scope.impl;

import org.hibernate.search.engine.backend.common.spi.DocumentReferenceConverter;
import org.hibernate.search.engine.backend.session.spi.BackendSessionContext;
import org.hibernate.search.mapper.orm.common.EntityReference;
import org.hibernate.search.mapper.orm.massindexing.impl.HibernateOrmMassIndexingSessionContext;
import org.hibernate.search.mapper.orm.loading.impl.LoadingSessionContext;
import org.hibernate.search.mapper.orm.spi.BatchSessionContext;
import org.hibernate.search.mapper.pojo.scope.spi.PojoScopeSessionContext;

public interface HibernateOrmScopeSessionContext
		extends PojoScopeSessionContext, LoadingSessionContext, HibernateOrmMassIndexingSessionContext,
				BatchSessionContext {

	BackendSessionContext backendSessionContext();

	DocumentReferenceConverter<EntityReference> documentReferenceConverter();

}
