/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.impl;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.stream.Stream;

import org.hibernate.search.engine.environment.bean.BeanReference;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.RoutingKeyBinderRef;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.declaration.RoutingKeyBinding;
import org.hibernate.search.mapper.pojo.bridge.mapping.impl.AnnotationInitializingBeanDelegatingBinder;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.RoutingKeyBinder;
import org.hibernate.search.mapper.pojo.logging.impl.Log;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

class RoutingKeyBridgeProcessor extends TypeAnnotationProcessor<Annotation> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	RoutingKeyBridgeProcessor(AnnotationProcessorHelper helper) {
		super( helper );
	}

	@Override
	public Stream<? extends Annotation> extractAnnotations(PojoRawTypeModel<?> typeModel) {
		return typeModel.getAnnotationsByMetaAnnotationType( RoutingKeyBinding.class );
	}

	@Override
	public void process(TypeMappingStep mappingContext, PojoRawTypeModel<?> typeModel, Annotation annotation) {
		RoutingKeyBinder<?> binder = createRoutingKeyBinder( annotation );
		mappingContext.routingKeyBinder( binder );
	}

	private <A extends Annotation> RoutingKeyBinder createRoutingKeyBinder(A annotation) {
		RoutingKeyBinding bridgeMapping = annotation.annotationType().getAnnotation( RoutingKeyBinding.class );
		RoutingKeyBinderRef bridgeReferenceAnnotation = bridgeMapping.binder();
		Optional<BeanReference<? extends RoutingKeyBinder>> binderReference = helper.toBeanReference(
				RoutingKeyBinder.class,
				RoutingKeyBinderRef.UndefinedBinderImplementationType.class,
				bridgeReferenceAnnotation.type(), bridgeReferenceAnnotation.name()
		);

		if ( !binderReference.isPresent() ) {
			throw log.missingBinderReferenceInBridgeMapping(
					bridgeMapping.annotationType(), annotation.annotationType()
			);
		}

		RoutingKeyBinder<A> binder =
				new AnnotationInitializingBeanDelegatingBinder<>( binderReference.get() );
		binder.initialize( annotation );
		return binder;
	}
}