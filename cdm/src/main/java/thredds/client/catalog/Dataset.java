/*
 * Copyright 1998-2015 University Corporation for Atmospheric Research/Unidata
 *
 *   Portions of this software were developed by the Unidata Program at the
 *   University Corporation for Atmospheric Research.
 *
 *   Access and use of this software shall impose the following obligations
 *   and understandings on the user. The user is granted the right, without
 *   any fee or cost, to use, copy, modify, alter, enhance and distribute
 *   this software, and any derivative works thereof, and its supporting
 *   documentation for any purpose whatsoever, provided that this entire
 *   notice appears in all copies of the software, derivative works and
 *   supporting documentation.  Further, UCAR requests that the user credit
 *   UCAR/Unidata in any publications that result from the use of this
 *   software or in any product that includes this software. The names UCAR
 *   and/or Unidata, however, may not be used in any advertising or publicity
 *   to endorse or promote any products or commercial entity unless specific
 *   written permission is obtained from UCAR/Unidata. The user also
 *   understands that UCAR/Unidata is not obligated to provide the user with
 *   any support, consulting, training or assistance of any kind with regard
 *   to the use, operation and performance of this software nor to provide
 *   the user with any updates, revisions, new versions or "bug fixes."
 *
 *   THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *   IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *   INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *   FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *   NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *   WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package thredds.client.catalog;

import net.jcip.annotations.Immutable;
import thredds.client.catalog.builder.AccessBuilder;
import thredds.client.catalog.builder.DatasetBuilder;
import ucar.nc2.units.DateRange;
import ucar.nc2.units.DateType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A Client Catalog Dataset
 * @author caron
 * @since 1/7/2015
 */
@Immutable
public class Dataset extends DatasetNode {
  public static final String Access = "Access";
  public static final String Alias = "Alias";
  public static final String Authority = "Authority";
  public static final String CollectionType = "CollectionType";
  public static final String Contributors = "Contributors";
  public static final String Creators = "Creators";
  public static final String DataFormatType = "DataFormatType";
  public static final String Datasets = "Datasets";
  public static final String DataSize = "DataSize";
  public static final String Dates = "Dates";
  public static final String Documentation = "Documentation";
  public static final String Expires = "Expires";
  public static final String FeatureType = "FeatureType";
  public static final String GeospatialCoverage = "GeospatialCoverage";
  public static final String Harvest = "Harvest";
  public static final String Id = "Id";
  public static final String Keywords = "Keywords";
  public static final String MetadataOther = "MetadataOther";
  public static final String Projects = "Projects";
  public static final String Properties = "Properties";
  public static final String Publishers = "Publishers";
  public static final String ResourceControl = "ResourceControl";
  public static final String ServiceName = "ServiceName";
  public static final String Services = "Services";
  public static final String ThreddsMetadata = "ThreddsMetadata";
  public static final String ThreddsMetadataInheritable = "ThreddsMetadataInheritable";
  public static final String TimeCoverage = "TimeCoverage";
  public static final String VariableGroups = "VariableGroups";
  public static final String VariableMapLink = "VariableMapLink";
  public static final String Version = "Version";
  public static final String UrlPath = "UrlPath";

  public Dataset(DatasetNode parent, String name, Map<String, Object> flds, List<AccessBuilder> accessBuilders, List<DatasetBuilder> datasetBuilders) {
    super(parent, name, flds, datasetBuilders);

    if (accessBuilders != null && accessBuilders.size() > 0) {
      List<Access> access = new ArrayList<>(accessBuilders.size());
      for (AccessBuilder acc : accessBuilders)
        access.add ( acc.makeAccess(this));
      flds.put(Access, Collections.unmodifiableList(access));
    }
  }

  /////////////////////////////////////////////////////

  public List<Access> getAccess() {
    List<Access> access = (List<Access>) flds.get(Access);
    return access == null ? new ArrayList<Access>(0) : access;
  }

  public Access getAccess(ServiceType type) {
    for (Access acc : getAccess())
      if (acc.getService().getType() == type) return acc;
    return null;
  }

  public boolean hasAccess() {
    List<Access> access = getAccess();
    return !access.isEmpty();
  }

  /**
   * Get access element that matches the given access standard URL.
   * Match on a.getStandardUrlName().
   *
   * @param accessURL find theis access URL string
   * @return InvAccess or null if no match.
   */
  public Access findAccess(String accessURL) {
    for (Access a : getAccess()) {
      if (accessURL.equals(a.getStandardUrlName()))
        return a;
    }
    return null;
  }

  /**
   * Get URL to this dataset. Dataset must have an ID.
   * Form is catalogURL#DatasetID
   *
   * @return URL to this dataset.
   */
  public String getCatalogUrl() {
    Catalog parent = getParentCatalog();
    if (parent == null) return null;
    String baseUri = parent.getUriString();
    if (baseUri == null) return null;
    return baseUri + "#" + getId();
  }

   /////////////////////////////////////////////////////
  // non-inheritable metadata
  public String getCollectionType() {
    return (String) flds.get(CollectionType);
  }
  public boolean isDatasetScan() {
    return false;
  }
  public boolean isHarvest() {
    Boolean result = (Boolean) flds.get(Harvest);
    return (result != null) && result;
  }
  public String getId() {
    return (String) flds.get(Id);
  }
  public String getUrlPath() {
    return (String) flds.get(UrlPath);
  }

  /////////////////////////////////////////////////////
  // inheritable metadata
  Object getInheritedField(String fldName) {
    Object value = flds.get(fldName);
    if (value != null) return value;
    ThreddsMetadata tmi = (ThreddsMetadata) flds.get(ThreddsMetadataInheritable);
    if (tmi != null) {
      value = tmi.get(fldName);
      if (value != null) return value;
    }
    Dataset parent = getParentDataset();
    return (parent == null) ? null : parent.getInheritedField( fldName);
  }

  List getInheritedFieldAsList(String fldName) {
    Object value = getInheritedField(fldName);
    if (value == null) return new ArrayList(0);
    if (value instanceof List) return (List) value;
    List list1 = new ArrayList(1);
    list1.add(value);
    return list1;
  }

  ///////////////////////////////////////////////////////////
  public String getAuthority() {
    return (String) getInheritedField(Authority);
  }

  public String getDataFormatType() {
    return (String) getInheritedField(DataFormatType);
  }

  public long getDataSize() {
    Long size = (Long) getInheritedField(DataSize);
    return (size == null) ? -1 : size;
  }

  public boolean hasDataSize() {
    Long size = (Long) getInheritedField(DataSize);
    return (size != null) && size > 0;
  }

  public ucar.nc2.constants.FeatureType getFeatureType() {
    String name = getFeatureTypeName();
    ucar.nc2.constants.FeatureType ft;
    try {
      return ucar.nc2.constants.FeatureType.valueOf(name);
    } catch (Exception e) {
      return null;
    }
  }

  public String getFeatureTypeName() {
    return (String) getInheritedField(FeatureType);
  }

 public ThreddsMetadata.GeospatialCoverage getGeospatialCoverage() {
    return (ThreddsMetadata.GeospatialCoverage) getInheritedField(GeospatialCoverage);
  }

  public String getResourceControl() {
    return (String) getInheritedField(ResourceControl);
  }

  public DateRange getTimeCoverage() {
    return (DateRange) getInheritedField(TimeCoverage);
  }

  public URI getVariableMapLink() {
    return (URI) getInheritedField(VariableMapLink);
  }

  ///////////////////////////////////////////

  public List<ThreddsMetadata.Source> getCreators() {
    return (List<ThreddsMetadata.Source>) getInheritedFieldAsList(Dataset.Creators);
  }

  public List<ThreddsMetadata.Contributor> getContributors() {
    return (List<ThreddsMetadata.Contributor>) getInheritedFieldAsList(Dataset.Contributors);
  }

  public List<DateType> getDates() {
    return (List<DateType>) getInheritedFieldAsList(Dates);
  }

  public List<Documentation> getDocumentation() {
    return (List<Documentation>) getInheritedFieldAsList(Documentation);
  }

  public List<ThreddsMetadata.Vocab> getKeywords() {
    return (List<ThreddsMetadata.Vocab>) getInheritedFieldAsList(Keywords);
  }

  public List<ThreddsMetadata.MetadataOther> getMetadataOther() {
    return (List<ThreddsMetadata.MetadataOther>) getInheritedFieldAsList(MetadataOther);
  }

  /* public List<ThreddsMetadata.MetadataOther> getMetadata(Metadata.Type want) {
    List<ThreddsMetadata.MetadataOther> result = new ArrayList<>();
    for (ThreddsMetadata.MetadataOther m : getMetadata())
      if (m.getType() == want) result.add(m);
    return result;
  } */

  public List<ThreddsMetadata.Vocab> getProjects() {
    return (List<ThreddsMetadata.Vocab>) getInheritedFieldAsList(Projects);
  }

  public List<Property> getProperties() {
    return (List<Property>) getInheritedFieldAsList(Properties);
  }

  public List<ThreddsMetadata.Source> getPublishers() {
    return (List<ThreddsMetadata.Source>) getInheritedFieldAsList(Dataset.Publishers);
  }

  public List<ThreddsMetadata.VariableGroup> getVariables() {
    return (List<ThreddsMetadata.VariableGroup>) getInheritedFieldAsList(Dataset.VariableGroups);
  }

  public String getDocumentation(String type) {
    for (Documentation doc : getDocumentation()) {
      String dtype = doc.getType();
      if ((dtype != null) && dtype.equalsIgnoreCase(type)) return doc.getInlineContent();
    }
    return null;
  }

  /**
   * @return specific type of documentation = history
   */
  public String getHistory() {
    return getDocumentation("history");
  }

  /**
   * @return specific type of documentation = processing_level
   */
  public String getProcessing() {
    return getDocumentation("processing_level");
  }

  /**
   * @return specific type of documentation = rights
   */
  public String getRights() {
    return getDocumentation("rights");
  }

  /**
   * @return specific type of documentation = summary
   */
  public String getSummary() {
    return getDocumentation("summary");
  }

}
