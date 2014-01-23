package org.gbif.occurrence.search.writers;

import org.gbif.api.model.occurrence.Occurrence;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.occurrence.common.constants.FieldName;
import org.gbif.occurrence.persistence.constants.HBaseTableConstants;
import org.gbif.occurrence.persistence.hbase.HBaseFieldUtil;
import org.gbif.occurrence.common.converter.BasisOfRecordConverter;

import java.io.IOException;

import com.google.common.base.Predicate;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Utility class for writing occurrence records into an HBase table.
 */
public class HBaseOccurrenceWriter implements Predicate<Occurrence> {


  // HBase table
  private final HTableInterface hTable;

  // Column family
  private final static byte[] CF = Bytes.toBytes(HBaseTableConstants.OCCURRENCE_COLUMN_FAMILY);

  private static final BasisOfRecordConverter BOR_CONVERTER = new BasisOfRecordConverter();


  /**
   * Default constructor.
   */
  public HBaseOccurrenceWriter(HTableInterface hTable) {
    this.hTable = hTable;
  }

  /**
   * Reads and processes the occurrence object.
   */
  @Override
  public boolean apply(Occurrence input) {
    try {
      write(input);
      return true;
    } catch (IOException e) {
      return false;
    }
  }


  /**
   * Writes the occurrence record into the hbase table.
   *
   * @param occ occurrence object that will be written to hbase
   * @throws IOException
   */
  private void write(Occurrence occ) throws IOException {
    Put put = new Put(Bytes.toBytes(occ.getKey()));


    if (occ.getAltitude() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_ALTITUDE).getColumnName()),
        Bytes.toBytes(occ.getAltitude()));
    }

    if (occ.getBasisOfRecord() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_BASIS_OF_RECORD).getColumnName()),
        Bytes.toBytes(BOR_CONVERTER.fromEnum(occ.getBasisOfRecord())));
    }
    if (occ.getField(DwcTerm.catalogNumber) != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.CATALOG_NUMBER).getColumnName()),
        Bytes.toBytes(occ.getField(DwcTerm.catalogNumber)));
    }

    if (occ.getClassKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_CLASS_ID).getColumnName()),
        Bytes.toBytes(occ.getClassKey()));
    }
    if (occ.getClazz() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_CLASS).getColumnName()),
        Bytes.toBytes(occ.getClazz()));
    }

    if (occ.getField(DwcTerm.collectionCode) != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.COLLECTION_CODE).getColumnName()),
        Bytes.toBytes(occ.getField(DwcTerm.collectionCode)));
    }

    // deprecated - remove
//    if (occ.getDataProviderId() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.DATA_PROVIDER_ID).getColumnName()),
//        Bytes.toBytes(occ.getDataProviderId()));
//    }
//
//    if (occ.getDataResourceId() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.DATA_RESOURCE_ID).getColumnName()),
//        Bytes.toBytes(occ.getDataResourceId()));
//    }

    if (occ.getDatasetKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.DATASET_KEY).getColumnName()),
        Bytes.toBytes(occ.getDatasetKey().toString()));
    }

    if (occ.getDepth() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_DEPTH).getColumnName()),
        Bytes.toBytes(occ.getDepth()));
    }

    if (occ.getFamily() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_FAMILY).getColumnName()),
        Bytes.toBytes(occ.getFamily()));
    }

    if (occ.getFamilyKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_FAMILY_ID).getColumnName()),
        Bytes.toBytes(occ.getFamilyKey()));
    }

    if (occ.getGenus() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_GENUS).getColumnName()),
        Bytes.toBytes(occ.getGenus()));
    }

    if (occ.getGenusKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_GENUS_ID).getColumnName()),
        Bytes.toBytes(occ.getGenusKey()));
    }

    // TODO geospatial issue has changed a lot
//    if (occ.getGeospatialIssue() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_GEOSPATIAL_ISSUE).getColumnName()),
//        Bytes.toBytes(occ.getGeospatialIssue()));
//    }

    if (occ.getField(DwcTerm.institutionCode) != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.INSTITUTION_CODE).getColumnName()),
        Bytes.toBytes(occ.getField(DwcTerm.institutionCode)));
    }

    if (occ.getKingdom() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_KINGDOM).getColumnName()),
        Bytes.toBytes(occ.getKingdom()));
    }

    if (occ.getKingdomKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_KINGDOM_ID).getColumnName()),
        Bytes.toBytes(occ.getKingdomKey()));
    }

    if (occ.getLatitude() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_LATITUDE).getColumnName()),
        Bytes.toBytes(occ.getLatitude()));
    }

    if (occ.getLongitude() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_LONGITUDE).getColumnName()),
        Bytes.toBytes(occ.getLongitude()));
    }

    if (occ.getField(DwcTerm.locality) != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.LOCALITY).getColumnName()),
        Bytes.toBytes(occ.getField(DwcTerm.locality)));
    }

    if (occ.getCountry() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_ISO_COUNTRY_CODE).getColumnName()),
        Bytes.toBytes(occ.getCountry().getIso2LetterCode()));
    }

    if (occ.getField(DwcTerm.county) != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.COUNTY).getColumnName()),
        Bytes.toBytes(occ.getField(DwcTerm.county)));
    }

    if (occ.getStateProvince() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.STATE_PROVINCE).getColumnName()),
        Bytes.toBytes(occ.getStateProvince()));
    }

    // TODO: continent is now an enum
//    if (occ.getContinent() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.CONTINENT_OCEAN).getColumnName()),
//        Bytes.toBytes(occ.getContinent()));
//    }

    if (occ.getField(DwcTerm.recordedBy) != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.COLLECTOR_NAME).getColumnName()),
        Bytes.toBytes(occ.getField(DwcTerm.recordedBy)));
    }

    if (occ.getField(DwcTerm.identifiedBy) != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.IDENTIFIER_NAME).getColumnName()),
        Bytes.toBytes(occ.getField(DwcTerm.identifiedBy)));
    }

    if (occ.getDateIdentified() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.IDENTIFICATION_DATE).getColumnName()),
        Bytes.toBytes(occ.getDateIdentified().getTime()));
    }

    if (occ.getModified() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_MODIFIED).getColumnName()),
        Bytes.toBytes(occ.getModified().getTime()));
    }

    if (occ.getMonth() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_MONTH).getColumnName()),
        Bytes.toBytes(occ.getMonth()));
    }

    if (occ.getTaxonKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_NUB_ID).getColumnName()),
        Bytes.toBytes(occ.getTaxonKey()));
    }

    if (occ.getEventDate() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_OCCURRENCE_DATE).getColumnName()),
        Bytes.toBytes(occ.getEventDate().getTime()));
    }

    if (occ.getOrder() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_ORDER).getColumnName()),
        Bytes.toBytes(occ.getOrder()));
    }

    if (occ.getOrderKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_ORDER_ID).getColumnName()),
        Bytes.toBytes(occ.getOrderKey()));
    }

    // TODO: other issue now deprecated
//    if (occ.getOtherIssue() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_OTHER_ISSUE).getColumnName()),
//        Bytes.toBytes(occ.getOtherIssue()));
//    }

    if (occ.getPublishingOrgKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.OWNING_ORG_KEY).getColumnName()),
        Bytes.toBytes(occ.getPublishingOrgKey().toString()));
    }

    if (occ.getPhylum() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_PHYLUM).getColumnName()),
        Bytes.toBytes(occ.getPhylum()));
    }

    if (occ.getPhylumKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_PHYLUM_ID).getColumnName()),
        Bytes.toBytes(occ.getPhylumKey()));
    }

    // deprecated
//    if (occ.getResourceAccessPointId() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.RESOURCE_ACCESS_POINT_ID).getColumnName()),
//        Bytes.toBytes(occ.getResourceAccessPointId()));
//    }

    if (occ.getScientificName() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_SCIENTIFIC_NAME).getColumnName()),
        Bytes.toBytes(occ.getScientificName()));
    }

    if (occ.getSpecies() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_SPECIES).getColumnName()),
        Bytes.toBytes(occ.getSpecies()));
    }

    if (occ.getSpeciesKey() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_SPECIES_ID).getColumnName()),
        Bytes.toBytes(occ.getSpeciesKey()));
    }

    // TODO: deprecated
//    if (occ.getTaxonomicIssue() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_TAXONOMIC_ISSUE).getColumnName()),
//        Bytes.toBytes(occ.getTaxonomicIssue()));
//    }

    // TODO: deprecated
//    if (occ.getUnitQualifier() != null) {
//      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.UNIT_QUALIFIER).getColumnName()),
//        Bytes.toBytes(occ.getUnitQualifier()));
//    }

    if (occ.getYear() != null) {
      put.add(CF, Bytes.toBytes(HBaseFieldUtil.getHBaseColumn(FieldName.I_YEAR).getColumnName()),
        Bytes.toBytes(occ.getYear()));
    }

    hTable.put(put);
    hTable.flushCommits();
  }

}
