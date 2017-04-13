package com.quantal.basecomponents.objectmapper;

/**
 * Created by dman on 08/03/2017.
 */

import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Orika mapper exposed as a Spring Bean. This mapper will not map nulls
 * / skip nulls
 *
 * @author quophyie
 *
 */
public class NullSkippingOrikaBeanMapper extends OrikaBeanMapper {


  private boolean mapNulls = false;
  public NullSkippingOrikaBeanMapper(/*@Value("${app.orikamapper.map-nulls}") final Boolean mapNulls */) {
    super();
   // this.mapNulls = mapNulls;
  }

  /**
   * Configures the mapper factory builder
   * {@inheritDoc}
   */
  @Override
  protected void configureFactoryBuilder(final DefaultMapperFactory.Builder factoryBuilder) {
    // If this.mapNulls is false, then null values will not be mapped i.e they will be skipped
    factoryBuilder.mapNulls(this.mapNulls);
  }
}