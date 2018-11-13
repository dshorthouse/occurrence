package org.gbif.occurrence.download.service.hive.validation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.gbif.occurrence.download.service.hive.validation.Query.Issue;


/**
 * SQL Download query is verified in case it has license and datasetkey for selection field. If the
 * Rule is failed {@linkplain Query.Issue} is raised.
 *
 */
public class DatasetKeyAndLicenseRequiredRule implements Rule {

  @Override
  public RuleContext apply(QueryContext value) {

    Predicate<List<String>> condition1 = x -> (x.size() == 1 && x.get(0).equals("*"));
    Predicate<List<String>> condition2 = y -> y.containsAll(Arrays.asList("DATASETKEY", "LICENSE"));
    Predicate<List<String>> condition3 = y -> y.containsAll(Arrays.asList("datasetkey", "license"));
    
    return condition1.or(condition2).or(condition3).test(value.selectFieldNames()) ? Rule.preserved() : Rule.violated(Issue.DATASET_AND_LICENSE_REQUIRED);
  }

}