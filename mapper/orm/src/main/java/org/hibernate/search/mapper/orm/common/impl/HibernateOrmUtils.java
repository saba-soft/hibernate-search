/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.common.impl;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.AssertionFailure;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.search.mapper.orm.logging.impl.Log;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

public final class HibernateOrmUtils {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private HibernateOrmUtils() {
	}

	public static SessionFactoryImplementor toSessionFactoryImplementor(EntityManagerFactory entityManagerFactory) {
		try {
			return entityManagerFactory.unwrap( SessionFactoryImplementor.class );
		}
		catch (IllegalStateException e) {
			throw log.hibernateSessionFactoryAccessError( e.getMessage(), e );
		}
	}

	public static Session toSession(EntityManager entityManager) {
		try {
			return entityManager.unwrap( Session.class );
		}
		catch (IllegalStateException e) {
			throw log.hibernateSessionAccessError( e.getMessage(), e );
		}
	}

	public static SessionImplementor toSessionImplementor(EntityManager entityManager) {
		try {
			return entityManager.unwrap( SessionImplementor.class );
		}
		catch (IllegalStateException e) {
			throw log.hibernateSessionAccessError( e.getMessage(), e );
		}
	}

	public static boolean isSuperTypeOf(EntityPersister type1, EntityPersister type2) {
		return type1.isSubclassEntityName( type2.getEntityName() );
	}

	public static EntityPersister toRootEntityType(SessionFactoryImplementor sessionFactory,
			EntityPersister entityType) {
		/*
		 * We need to rely on Hibernate ORM's SPIs: this is complex stuff.
		 * For example there may be class hierarchies such as A > B > C
		 * where A and C are entity types and B is a mapped superclass.
		 * So we need to exclude non-entity types, and for that we need the Hibernate ORM metamodel.
		 */
		MetamodelImplementor metamodel = sessionFactory.getMetamodel();
		String rootEntityName = entityType.getRootEntityName();
		return metamodel.entityPersister( rootEntityName );
	}

	public static EntityPersister toMostSpecificCommonEntitySuperType(MetamodelImplementor metamodel,
			EntityPersister type1, EntityPersister type2) {
		/*
		 * We need to rely on Hibernate ORM's SPIs: this is complex stuff.
		 * For example there may be class hierarchies such as A > B > C
		 * where A and C are entity types and B is a mapped superclass.
		 * So even if we know the two types have a common superclass,
		 * we need to skip non-entity superclasses, and for that we need the Hibernate ORM metamodel.
		 */
		EntityPersister superTypeCandidate = type1;
		while ( superTypeCandidate != null && !isSuperTypeOf( superTypeCandidate, type2 ) ) {
			String superSuperTypeEntityName = superTypeCandidate.getEntityMetamodel().getSuperclass();
			superTypeCandidate = superSuperTypeEntityName == null ? null
					: metamodel.entityPersister( superSuperTypeEntityName ).getEntityPersister();
		}
		if ( superTypeCandidate == null ) {
			throw new AssertionFailure(
					"Cannot find a common entity supertype for " + type1.getEntityName()
							+ " and " + type2.getEntityName() + "."
							+ " There is a bug in Hibernate Search, please report it."
			);
		}
		return superTypeCandidate;
	}

	public static boolean hasAtMostOneConcreteSubType(SessionFactoryImplementor sessionFactory,
			EntityPersister parentType) {
		@SuppressWarnings("unchecked")
		Set<String> subClassEntityNames = parentType.getEntityMetamodel().getSubclassEntityNames();
		// Quick check to return true immediately if there's only one type
		if ( subClassEntityNames.size() == 1 ) {
			return true;
		}

		MetamodelImplementor metamodel = sessionFactory.getMetamodel();
		int concreteSubTypesCount = 0;
		for ( String subClassEntityName : subClassEntityNames ) {
			if ( !metamodel.entityPersister( subClassEntityName ).getEntityMetamodel().isAbstract() ) {
				if ( ++concreteSubTypesCount > 1 ) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean targetsAllConcreteSubTypes(SessionFactoryImplementor sessionFactory,
			EntityPersister parentType, Set<?> targetConcreteSubTypes) {
		@SuppressWarnings("unchecked")
		Set<String> subClassEntityNames = parentType.getEntityMetamodel().getSubclassEntityNames();
		// Quick check to return true immediately if all subtypes are concrete
		if ( subClassEntityNames.size() == targetConcreteSubTypes.size() ) {
			return true;
		}

		MetamodelImplementor metamodel = sessionFactory.getMetamodel();
		int concreteSubTypesCount = 0;
		for ( String subClassEntityName : subClassEntityNames ) {
			if ( !metamodel.entityPersister( subClassEntityName ).getEntityMetamodel().isAbstract() ) {
				++concreteSubTypesCount;
			}
		}
		return concreteSubTypesCount == targetConcreteSubTypes.size();
	}

}
