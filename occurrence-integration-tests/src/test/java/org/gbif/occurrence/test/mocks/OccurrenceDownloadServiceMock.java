package org.gbif.occurrence.test.mocks;

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.occurrence.Download;
import org.gbif.api.model.registry.DatasetOccurrenceDownloadUsage;
import org.gbif.api.service.registry.OccurrenceDownloadService;
import org.gbif.api.vocabulary.Country;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.RandomStringUtils;

public class OccurrenceDownloadServiceMock implements OccurrenceDownloadService {

  private Map<String,Download> downloads = new HashMap<>();


  private Map<String,List<DatasetOccurrenceDownloadUsage>> usages = new HashMap<>();

  private static final String DOI_PREFIX = "10.5072";

  private DOI randomDoi() {
    String suffix = "dl." + RandomStringUtils.random(6, "23456789abcdefghjkmnpqrstuvwxyz");
    return new DOI(DOI_PREFIX, suffix);
  }

  @Override
  public void create(@NotNull @Valid Download download) {
    String key = UUID.randomUUID().toString();
    download.setKey(key);
    download.setDoi(randomDoi());
    download.setStatus(Download.Status.SUCCEEDED);
    downloads.put(key, download);
  }

  @Override
  public Download get(@NotNull String key) {
    return downloads.get(key);
  }

  private PagingResponse<Download> filterDownloads(Pageable pageable, Predicate<Download> filter) {
    PagingResponse<Download> response = new PagingResponse<>();
    Stream<Download> downloadStream = downloads.values().stream();

    downloadStream = downloadStream.filter(filter);

    if (Objects.nonNull(pageable)) {
      response.setLimit(pageable.getLimit());
      response.setOffset(pageable.getOffset());
      downloadStream = downloadStream.skip(pageable.getOffset())
                        .limit(pageable.getLimit());
    }


    List<Download> results = downloadStream.collect(Collectors.toList());

    response.setCount((long)results.size());
    response.setResults(results);
    return response;
  }


  @Override
  public PagingResponse<Download> list(@Nullable Pageable pageable, @Nullable Set<Download.Status> statuses) {
    return filterDownloads(pageable, d -> statuses.contains(d.getStatus()));
  }

  @Override
  public PagingResponse<Download> listByUser(@NotNull String user, @Nullable Pageable pageable,
                                             @Nullable Set<Download.Status> statuses) {
    return filterDownloads(pageable, d -> user.equals(d.getRequest().getCreator()) && statuses.contains(d.getStatus()));
  }

  @Override
  public void update(@NotNull @Valid Download download) {
    downloads.replace(download.getKey(), download);
  }

  @Override
  public PagingResponse<DatasetOccurrenceDownloadUsage> listDatasetUsages(
    @NotNull String downloadKey, @Nullable Pageable pageable
  ) {
    PagingResponse<DatasetOccurrenceDownloadUsage> response = new PagingResponse<>(pageable);
    List<DatasetOccurrenceDownloadUsage> downloadUsages = usages.get(downloadKey).stream()
                                                            .skip(pageable.getOffset())
                                                            .limit(pageable.getLimit())
                                                            .collect(Collectors.toList());
    response.setCount((long)downloadUsages.size());
    response.setResults(downloadUsages);
    return response;
  }

  @Override
  public String getCitation(@NotNull String downloadKey) {
    Download download = downloads.get(downloadKey);
    return "GBIF Occurrence Download " + download.getDoi().getUrl().toString() + '\n';
  }

  @Override
  public Map<Integer, Map<Integer, Long>> getDownloadsByUserCountry(@Nullable Date fromDate, @Nullable Date toDate, @Nullable Country country) {
    return null;
  }

  @Override
  public Map<Integer, Map<Integer, Long>> getDownloadedRecordsByDataset(
    @Nullable Date fromDate, @Nullable Date toDate, @Nullable Country country, @Nullable UUID datasetKey
  ) {
    return null;
  }

  @Override
  public void createUsages(@NotNull String downloadKey, @NotNull Map<UUID, Long> downloadUsages) {
    usages.put(downloadKey,
    downloadUsages.entrySet().stream().map(entry -> {
      UUID datasetKey = entry.getKey();
      DatasetOccurrenceDownloadUsage datasetOccurrenceDownloadUsage = new DatasetOccurrenceDownloadUsage();
      datasetOccurrenceDownloadUsage.setDownloadKey(downloadKey);
      datasetOccurrenceDownloadUsage.setDatasetKey(datasetKey);
      datasetOccurrenceDownloadUsage.setNumberRecords(entry.getValue());
      datasetOccurrenceDownloadUsage.setDownload(downloads.get(downloadKey));
      datasetOccurrenceDownloadUsage.setDatasetCitation(datasetKey.toString());
      datasetOccurrenceDownloadUsage.setDatasetTitle(datasetKey.toString());
      return datasetOccurrenceDownloadUsage;
    }).collect(Collectors.toList()));
  }
}
