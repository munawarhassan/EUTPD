package com.pmi.tpd.web.rest.rsrc.api.euceg;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eu.ceg.CountryValue;
import org.eu.ceg.NationalMarketValue;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.euceg.core.refs.EcigProductTypeEnum;
import com.pmi.tpd.euceg.core.refs.EmissionNameEnum;
import com.pmi.tpd.euceg.core.refs.IngredientCategoryEnum;
import com.pmi.tpd.euceg.core.refs.IngredientFunctionEnum;
import com.pmi.tpd.euceg.core.refs.PackageTypeEnum;
import com.pmi.tpd.euceg.core.refs.ProductNumberTypeEnum;
import com.pmi.tpd.euceg.core.refs.TobaccoLeafCureMethod;
import com.pmi.tpd.euceg.core.refs.TobaccoLeafType;
import com.pmi.tpd.euceg.core.refs.TobaccoPartType;
import com.pmi.tpd.euceg.core.refs.TobaccoProductTypeEnum;
import com.pmi.tpd.euceg.core.refs.ToxicologicalDataAvailableEnum;
import com.pmi.tpd.euceg.core.refs.VoltageWattageAdjustableEnum;
import com.pmi.tpd.web.rest.RestApplication;
import com.pmi.tpd.web.rest.model.NameValuePair;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Path(RestApplication.API_RESOURCE_PATH + "/refs")
@Tag(description = "Endpoint for submission references", name = "business")
public class ReferenceResource {

  /**
   * @return
   */
  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("countries")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets list of countries")
  public List<NameValuePair<String, String>> getCountries() {
    final Map<String, String> map = Maps.newHashMap();
    for (final CountryValue countryCode : CountryValue.values()) {
      final Locale locale = new Locale("en", countryCode.name());
      map.put(countryCode.name(), locale.getDisplayCountry(Locale.ENGLISH));

    }
    return entriesSortedByValues(map);
  }

  /**
   * @return
   */
  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("countries/nationalMarket")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets list of national market countries")
  public List<NameValuePair<String, String>> getNationalMarkets() {
    final Map<String, String> map = Maps.newHashMap();
    for (final NationalMarketValue countryCode : NationalMarketValue.values()) {
      final Locale locale = new Locale("en", countryCode.name());
      map.put(countryCode.name(), locale.getDisplayCountry(Locale.ENGLISH));

    }
    return entriesSortedByValues(map);
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("tobaccoProductTypes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets list of tobacco product types")
  public Map<Integer, String> getTobaccoProductTypes() {
    return TobaccoProductTypeEnum.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("ecigProductTypes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets list of ecig product types")
  public Map<Integer, String> getEcigProductTypes() {
    return EcigProductTypeEnum.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("emissionNames")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets list of emission names")
  public Map<Integer, String> getEmissionNames() {
    return EmissionNameEnum.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("voltageWattageAdjustables")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets list of Voltage/Wattage Adjustables")
  public Map<Integer, String> getVoltageWattageAdjustables() {
    return VoltageWattageAdjustableEnum.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("packageTypes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Package Types")
  public Map<Integer, String> getPackageTypes() {
    return PackageTypeEnum.toKeyMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("productNumberTypes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Product Number Types")
  public Map<String, String> getProductNumberTypes() {
    return ProductNumberTypeEnum.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("tobaccoLeafCureMethods")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Tobacco Leaf Cure Methods")
  public Map<Integer, String> getTobaccoLeafCureMethods() {
    return TobaccoLeafCureMethod.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("tobaccoLeafTypes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Tobacco Leaf Types")
  public Map<Integer, String> getLeafTypes() {
    return TobaccoLeafType.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("tobaccoPartTypes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Tobacco Part Types")
  public Map<Integer, String> getTobaccoPartTypes() {
    return TobaccoPartType.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("toxicologicalDataAvailables")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Toxicological Data Availables")
  public Map<Integer, String> getToxicologicalDataAvailables() {
    return ToxicologicalDataAvailableEnum.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("ingredientCategories")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Ingredient Categories")
  public Map<Integer, String> getIngredientCategories() {
    return IngredientCategoryEnum.toMap();
  }

  @RolesAllowed(ApplicationConstants.Authorities.USER)
  @Path("ingredientFunctions")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Timed
  @PermitAll
  @Operation(summary = "Gets Map of Ingredient Functions")
  public Map<Integer, String> getIngredientFunctions() {
    return IngredientFunctionEnum.toMap();
  }

  static <K, V extends Comparable<? super V>> List<NameValuePair<K, V>> entriesSortedByValues(final Map<K, V> map) {
    final SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>((e1, e2) -> {
      final int res = e1.getValue().compareTo(e2.getValue());
      return res != 0 ? res : 1;
    });
    sortedEntries.addAll(map.entrySet());
    final List<NameValuePair<K, V>> m = Lists.newLinkedList();
    for (final Entry<K, V> entry : sortedEntries) {
      m.add(NameValuePair.create(entry.getKey(), entry.getValue()));
    }
    return m;
  }

}
