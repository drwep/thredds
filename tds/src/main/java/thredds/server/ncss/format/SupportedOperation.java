/*
 * Copyright 1998-2015 John Caron and University Corporation for Atmospheric Research/Unidata
 *
 *  Portions of this software were developed by the Unidata Program at the
 *  University Corporation for Atmospheric Research.
 *
 *  Access and use of this software shall impose the following obligations
 *  and understandings on the user. The user is granted the right, without
 *  any fee or cost, to use, copy, modify, alter, enhance and distribute
 *  this software, and any derivative works thereof, and its supporting
 *  documentation for any purpose whatsoever, provided that this entire
 *  notice appears in all copies of the software, derivative works and
 *  supporting documentation.  Further, UCAR requests that the user credit
 *  UCAR/Unidata in any publications that result from the use of this
 *  software or in any product that includes this software. The names UCAR
 *  and/or Unidata, however, may not be used in any advertising or publicity
 *  to endorse or promote any products or commercial entity unless specific
 *  written permission is obtained from UCAR/Unidata. The user also
 *  understands that UCAR/Unidata is not obligated to provide the user with
 *  any support, consulting, training or assistance of any kind with regard
 *  to the use, operation and performance of this software nor to provide
 *  the user with any updates, revisions, new versions or "bug fixes."
 *
 *  THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 *  INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 *  FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *  NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 *  WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package thredds.server.ncss.format;

import thredds.server.ncss.exception.UnsupportedResponseFormatException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static thredds.server.ncss.format.SupportedFormat.*;

/**
 * An enum of the various operations for netcdf subset service, and what download formats are allowed
 */
@SuppressWarnings("ProblematicWhitespace")
public enum SupportedOperation {
  DATASET_INFO_REQUEST("Dataset info request", XML_FILE),
  DATASET_BOUNDARIES_REQUEST("Dataset grid boundaries request", WKT, JSON),
  GRID_REQUEST("Grid data request", NETCDF3, NETCDF4, NETCDF4EXT),
  GRID_AS_POINT_REQUEST("Grid as point request", CSV_STREAM, CSV_FILE, XML_STREAM, XML_FILE, NETCDF3, NETCDF4, NETCDF4EXT),
  POINT_REQUEST("Point data request", CSV_STREAM, CSV_FILE, XML_STREAM, XML_FILE, NETCDF3, NETCDF4, NETCDF4EXT),
  STATION_REQUEST("Station data request", CSV_STREAM, CSV_FILE, XML_STREAM, XML_FILE, NETCDF3, NETCDF4, NETCDF4EXT, WATERML2);

  private final String operationName;
  private final List<SupportedFormat> supportedFormats;

  SupportedOperation(String operationName, SupportedFormat... formats) {
    this.operationName = operationName;
    this.supportedFormats = Arrays.asList(formats);
    assert this.supportedFormats.size() > 0;
  }

  public String getName() {
    return operationName;
  }

  public List<SupportedFormat> getSupportedFormats() {
    List<SupportedFormat> result = new ArrayList<>();
    for (SupportedFormat sf : supportedFormats) {
      if (FormatsAvailabilityService.isFormatAvailable(sf))
        result.add(sf);
    }
    return result;
  }

  public SupportedFormat getDefaultFormat() {
    for (SupportedFormat sf : supportedFormats) {
      if (FormatsAvailabilityService.isFormatAvailable(sf))
        return sf;
    }
    return null;
  }

  public @Nonnull
  SupportedFormat getSupportedFormat(String want) throws UnsupportedResponseFormatException {
    if (want == null || want.equals("")) {
      SupportedFormat sf = getDefaultFormat();
      if (sf != null) return sf;
      throw new UnsupportedResponseFormatException("No default Format available");
    }

    for (SupportedFormat f : getSupportedFormats()) {
      if (f.isAlias(want) && FormatsAvailabilityService.isFormatAvailable(f)) {
        return f;
      }
    }

    throw new UnsupportedResponseFormatException("Format " + want + " is not supported for " + getName());
  }
}
