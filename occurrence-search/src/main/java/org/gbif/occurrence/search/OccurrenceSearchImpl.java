package org.gbif.occurrence.search;


import org.gbif.api.model.checklistbank.NameUsageMatch;
import org.gbif.api.model.checklistbank.NameUsageMatch.MatchType;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.occurrence.Occurrence;
import org.gbif.api.model.occurrence.search.OccurrenceSearchParameter;
import org.gbif.api.model.occurrence.search.OccurrenceSearchRequest;
import org.gbif.api.service.checklistbank.NameUsageMatchingService;
import org.gbif.api.service.occurrence.OccurrenceSearchService;
import org.gbif.api.service.occurrence.OccurrenceService;
import org.gbif.common.search.builder.SolrQueryUtils;
import org.gbif.common.search.builder.SpellCheckResponseBuilder;
import org.gbif.common.search.exception.SearchException;
import org.gbif.common.search.util.QueryUtils;
import org.gbif.occurrence.search.solr.OccurrenceSolrField;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gbif.api.model.common.search.SearchConstants.DEFAULT_SUGGEST_LIMIT;
import static org.gbif.common.search.util.QueryUtils.buildTermQuery;
import static org.gbif.common.search.util.SolrConstants.SOLR_REQUEST_HANDLER;
import static org.gbif.occurrence.search.OccurrenceSearchRequestBuilder.QUERY_FIELD_MAPPING;

/**
 * Occurrence search service.
 * Executes {@link OccurrenceSearchRequest} by transforming the request into {@link SolrQuery}.
 */
public class OccurrenceSearchImpl implements OccurrenceSearchService {

  /**
   * Default limit value for auto-suggest services.
   */

  private static final Logger LOG = LoggerFactory.getLogger(OccurrenceSearchImpl.class);

  private static final Map<String, OccurrenceSearchParameter> FIELD_PARAMETER_MAPPING =
    new HashMap<String, OccurrenceSearchParameter>(QUERY_FIELD_MAPPING.size());

  static {
    for (Map.Entry<OccurrenceSearchParameter, OccurrenceSolrField> paramField : QUERY_FIELD_MAPPING.entrySet()) {
      FIELD_PARAMETER_MAPPING.put(paramField.getValue().getFieldName(), paramField.getKey());
    }
  }

  // Default order of results
  private static final Map<String, SolrQuery.ORDER> SORT_ORDER = new LinkedHashMap<String, SolrQuery.ORDER>(2);

  private final OccurrenceService occurrenceService;

  static {
    SORT_ORDER.put(OccurrenceSolrField.YEAR.getFieldName(), SolrQuery.ORDER.desc);
    SORT_ORDER.put(OccurrenceSolrField.MONTH.getFieldName(), SolrQuery.ORDER.asc);
  }

  private final SolrClient solrClient;

  private final OccurrenceSearchRequestBuilder occurrenceSearchRequestBuilder;
  private final NameUsageMatchingService nameUsageMatchingService;

  @Inject
  public OccurrenceSearchImpl(SolrClient solrClient, @Named(SOLR_REQUEST_HANDLER) String requestHandler,
                              OccurrenceService occurrenceService, NameUsageMatchingService nameUsageMatchingService,
                              @Named("max.offset") int maxOffset, @Named("max.limit") int maxLimit,
                              @Named("facets.enable") boolean facetsEnable) {
    this.solrClient = solrClient;
    occurrenceSearchRequestBuilder = new OccurrenceSearchRequestBuilder(requestHandler, SORT_ORDER,
                                                                        maxOffset, maxLimit, facetsEnable);
    this.occurrenceService = occurrenceService;
    this.nameUsageMatchingService = nameUsageMatchingService;
  }

  /**
   * Builds a SearchResponse instance using the current builder state.
   *
   * @return a new instance of a SearchResponse.
   */
  public SearchResponse<Occurrence, OccurrenceSearchParameter> buildResponse(QueryResponse queryResponse,
                                                                             Pageable request) {
    // Create response
    SearchResponse<Occurrence, OccurrenceSearchParameter> response =
      new SearchResponse<Occurrence, OccurrenceSearchParameter>(request);
    SolrDocumentList results = queryResponse.getResults();

    // set total count
    response.setCount(results.getNumFound());
    // Populates the results
    List<Occurrence> occurrences = Lists.newArrayListWithCapacity(results.size());
    for (SolrDocument doc : results) {
      // Only field key is returned in the result
      Integer occKey = (Integer) doc.getFieldValue(OccurrenceSolrField.KEY.getFieldName());
      Occurrence occ = occurrenceService.get(occKey);
      if (occ == null || occ.getKey() == null) {
        LOG.warn("Occurrence {} not found in store, but present in solr", occKey);
      } else {
        occurrences.add(occ);
      }
    }
    if (request.getLimit() > OccurrenceSearchRequestBuilder.MAX_PAGE_SIZE) {
      response.setLimit(OccurrenceSearchRequestBuilder.MAX_PAGE_SIZE);
    }
    if (queryResponse.getSpellCheckResponse() != null) {
      response.setSpellCheckResponse(SpellCheckResponseBuilder.build(queryResponse.getSpellCheckResponse()));
    }
    response.setResults(occurrences);

    if (occurrenceSearchRequestBuilder.isFacetsEnable()) {
      response.setFacets(SolrQueryUtils.getFacetsFromResponse(queryResponse, FIELD_PARAMETER_MAPPING));
    }
    return response;
  }

  @Override
  public SearchResponse<Occurrence, OccurrenceSearchParameter> search(@Nullable OccurrenceSearchRequest request) {
    try {
      if (hasReplaceableScientificNames(request)) {
        SolrQuery solrQuery = occurrenceSearchRequestBuilder.build(request);
        QueryResponse queryResponse = solrClient.query(solrQuery);
        return buildResponse(queryResponse, request);
      } else {
        return new SearchResponse<Occurrence, OccurrenceSearchParameter>(request);
      }
    } catch (SolrServerException | IOException e) {
      LOG.error("Error executing the search operation", e);
      throw new SearchException(e);
    }
  }

  @Override
  public List<String> suggestCatalogNumbers(String prefix, @Nullable Integer limit) {
    return suggestTermByField(prefix, OccurrenceSearchParameter.CATALOG_NUMBER, limit);
  }

  @Override
  public List<String> suggestCollectionCodes(String prefix, @Nullable Integer limit) {
    return suggestTermByField(prefix, OccurrenceSearchParameter.COLLECTION_CODE, limit);
  }


  @Override
  public List<String> suggestRecordedBy(String prefix, @Nullable Integer limit) {
    return suggestTermByField(prefix, OccurrenceSearchParameter.RECORDED_BY, limit);
  }

  @Override
  public List<String> suggestInstitutionCodes(String prefix, @Nullable Integer limit) {
    return suggestTermByField(prefix, OccurrenceSearchParameter.INSTITUTION_CODE, limit);
  }

  @Override
  public List<String> suggestRecordNumbers(String prefix, @Nullable Integer limit) {
    return suggestTermByField(prefix, OccurrenceSearchParameter.RECORD_NUMBER, limit);
  }

  @Override
  public List<String> suggestOccurrenceIds(String prefix, @Nullable Integer limit) {
    return suggestTermByField(prefix, OccurrenceSearchParameter.OCCURRENCE_ID, limit);
  }


  /**
   * Searches a indexed terms of a field that matched against the prefix parameter.
   *
   * @param prefix search term
   * @param parameter mapped field to be searched
   * @param limit of maximum matches
   * @return a list of elements that matched against the prefix
   */
  public List<String> suggestTermByField(String prefix, OccurrenceSearchParameter parameter, Integer limit) {
    try {
      String solrField = QUERY_FIELD_MAPPING.get(parameter).getFieldName();
      SolrQuery solrQuery = buildTermQuery(QueryUtils.parseQueryValue(prefix), solrField,
                                           Objects.firstNonNull(limit, DEFAULT_SUGGEST_LIMIT));
      QueryResponse queryResponse = solrClient.query(solrQuery);
      TermsResponse termsResponse = queryResponse.getTermsResponse();
      List<Term> terms = termsResponse.getTerms(solrField);
      List<String> suggestions = Lists.newArrayListWithCapacity(terms.size());
      for (Term term : terms) {
        suggestions.add(term.getTerm());
      }
      return suggestions;
    } catch (SolrServerException | IOException e) {
      LOG.error("Error executing/building the request", e);
      throw new SearchException(e);
    }
  }


  /**
   * Tries to get the corresponding name usage keys from the scientific_name parameter values.
   *
   * @return true: if the request doesn't contain any scientific_name parameter or if any scientific name was found
   *         false: if none scientific name was found
   */
  private boolean hasReplaceableScientificNames(OccurrenceSearchRequest request) {
    boolean hasValidReplaces = true;
    if (request.getParameters().containsKey(OccurrenceSearchParameter.SCIENTIFIC_NAME)) {
      hasValidReplaces = false;
      for (String value : request.getParameters().get(OccurrenceSearchParameter.SCIENTIFIC_NAME)) {
        NameUsageMatch nameUsageMatch = nameUsageMatchingService.match(value, null, null, true, false);
        if (nameUsageMatch.getMatchType() == MatchType.EXACT) {
          hasValidReplaces = true;
          request.addParameter(OccurrenceSearchParameter.TAXON_KEY, nameUsageMatch.getUsageKey());
        }
      }
    }
    return hasValidReplaces;
  }
}
