package org.gbif.occurrence.search.heatmap;

import org.gbif.api.model.occurrence.search.OccurrenceSearchRequest;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Request class for issuing heat map search request to the occurrence search service.
 * Warning:
 */
public class OccurrenceHeatmapRequest extends  OccurrenceSearchRequest {

  public enum Mode {
    GEO_BOUNDS, GEO_CENTROID;
  }

  private String geometry;

  private int zoom;

  private Mode mode = Mode.GEO_BOUNDS;

  /**
   * Default empty constructor, required for serialization.
   */
  public OccurrenceHeatmapRequest(){
    super(0,0);
  }

  /**
   * The region to compute the heatmap on, specified using the rectangle-range syntax or WKT.
   * It defaults to the world. ex: ["-180 -90" TO "180 90"].
   */
  public String getGeometry() {
    return geometry;
  }

  public void setGeometry(String geometry) {
    this.geometry = geometry;
  }

  /**
   * Heatmap zoom/gridLevel level
   * @return
   */
  public int getZoom() {
    return zoom;
  }

  public void setZoom(int zoom) {
    this.zoom = zoom;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("geometry", geometry).add("zoom",zoom)
            .add("mode", mode).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OccurrenceHeatmapRequest)) {
      return false;
    }

    OccurrenceHeatmapRequest that = (OccurrenceHeatmapRequest) obj;
    return Objects.equal(geometry, that.geometry)
           && Objects.equal(zoom, that.zoom)
           && Objects.equal(mode, that.mode);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), geometry, zoom, mode);
  }

}
