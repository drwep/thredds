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

package thredds.server.cdmrf;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import thredds.TestWithLocalServer;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.constants.AxisType;
import ucar.nc2.ft2.coverage.*;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.util.Misc;
import ucar.unidata.test.util.NeedsCdmUnitTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Description
 *
 * @author John
 * @since 8/17/2015
 */
@RunWith(Parameterized.class)
@Category(NeedsCdmUnitTest.class)
public class TestGridCoverageRemote {

  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> getTestParameters() {
    List<Object[]> result = new ArrayList<>();

    result.add(new Object[]{"cdmrfeature/grid/grib.v5/NDFD/CONUS_5km/NDFD_CONUS_5km_20131212_0000.grib2", "Categorical_Rain_surface",
            "2013-12-12T00:00:00Z",  "2013-12-17T12:00:00Z", null, null});

    result.add(new Object[]{"cdmrfeature/grid/gribCollection.v5/GFS_CONUS_80km/Best", "Temperature_isobaric",
            "2012-02-28T00:00:00Z",  "2012-02-28T00:00:00Z", null, 850.0});        // set runtime for best

    result.add(new Object[]{"cdmrfeature/grid/gribCollection.v5/GFS_CONUS_80km/Best", "Temperature_isobaric",
            null,  "2012-02-28T00:00:00Z", null, 850.0});

    return result;
  }

  String endpoint;
  String covName;
  CalendarDate rt_val;
  CalendarDate time_val;
  Double time_offset, vert_level;

  public TestGridCoverageRemote(String endpoint, String covName, String rt_val, String time_val, Double time_offset, Double vert_level) {
    this.endpoint = "cdmrFeature:" + TestWithLocalServer.withPath(endpoint);

    this.covName = covName;
    this.rt_val = rt_val == null ? null : CalendarDate.parseISOformat(null, rt_val);
    this.time_val = time_val == null ? null : CalendarDate.parseISOformat(null, time_val);
    this.time_offset = time_offset;
    this.vert_level = vert_level;
  }

  @Test
  public void testReadGridCoverageSlice() throws IOException, InvalidRangeException {
    System.out.printf("Test Dataset %s coverage %s%n", endpoint, covName);

    try (CoverageDatasetCollection cc = CoverageDatasetFactory.open(endpoint)) {
      Assert.assertNotNull(endpoint, cc);
      Assert.assertEquals(1, cc.getCoverageDatasets().size());
      CoverageDataset gcs = cc.getCoverageDatasets().get(0);
      Assert.assertNotNull("gcs", gcs);
      Coverage cover = gcs.findCoverage(covName);
      Assert.assertNotNull(covName, cover);

      readOne(cover, rt_val, time_val, time_offset, vert_level);
    }
  }

  // LOOK replicated from cdm_test/TestCOverageSubset
  void readOne(Coverage cover, CalendarDate rt_val, CalendarDate time_val, Double time_offset, Double vert_level) throws IOException, InvalidRangeException {
    System.out.printf("%n===Request Subset %s runtime=%s time=%s timeOffset=%s vert=%s %n", cover.getName(), rt_val, time_val, time_offset, vert_level);

    SubsetParams subset = new SubsetParams();
    if (rt_val != null)
      subset.set(SubsetParams.runtime, rt_val);
    if (time_val != null)
      subset.set(SubsetParams.time, time_val);
    if (time_offset != null)
      subset.set(SubsetParams.timeOffset, time_offset);
    if (vert_level != null)
      subset.set(SubsetParams.vertCoord, vert_level);

    GeoReferencedArray geoArray = cover.readData(subset);
    CoverageCoordSys geoCs = geoArray.getCoordSysForData();
    System.out.printf("%n%s%n", geoArray);
    System.out.printf("%ngeoArray shape=%s%n", Misc.showInts(geoArray.getData().getShape()));

    if (rt_val != null) {
      CoverageCoordAxis runAxis = geoCs.getAxis(AxisType.RunTime);
      if (runAxis != null) {
        Assert.assertEquals(1, runAxis.getNcoords());
        double val = runAxis.getStartValue();
        CalendarDate runDate = runAxis.makeDate(val);
        Assert.assertEquals(rt_val, runDate);
      }
    }

    if (time_val != null || time_offset != null) {
      CoverageCoordAxis timeAxis = geoCs.getAxis(AxisType.TimeOffset);
      if (timeAxis != null) {
        TimeOffsetAxis timeOffsetAxis = (TimeOffsetAxis) timeAxis;
        CoverageCoordAxis1D runAxis = (CoverageCoordAxis1D) geoCs.getAxis(AxisType.RunTime);
        Assert.assertNotNull(AxisType.RunTime.toString(), runAxis);
        Assert.assertEquals(1, runAxis.getNcoords());
        double val = runAxis.getStartValue();
        CalendarDate runDate = runAxis.makeDate(val);
        Assert.assertEquals(rt_val, runDate);
        Assert.assertEquals(1, timeOffsetAxis.getNcoords());

        if (time_val != null) {
          if (timeOffsetAxis.isInterval()) {
            CalendarDate edge1 = timeOffsetAxis.makeDate(runDate, timeOffsetAxis.getCoordEdge1(0));
            CalendarDate edge2 = timeOffsetAxis.makeDate(runDate, timeOffsetAxis.getCoordEdge2(0));

            Assert.assertTrue(edge1.toString(), !edge1.isAfter(time_val));
            Assert.assertTrue(edge2.toString(), !edge2.isBefore(time_val));

          } else {
            double val2 = timeOffsetAxis.getCoord(0);
            CalendarDate forecastDate = timeOffsetAxis.makeDate(runDate, val2);
            Assert.assertEquals(time_val, forecastDate);
          }

        } else {
          if (timeOffsetAxis.isInterval()) {
            Assert.assertTrue(timeOffsetAxis.getCoordEdge1(0) <= time_offset);
            Assert.assertTrue(timeOffsetAxis.getCoordEdge2(0) >= time_offset);

          } else {
            double val2 = timeOffsetAxis.getCoord(0);
            Assert.assertEquals(val2, time_offset, Misc.maxReletiveError);
          }
        }
      }

      if (vert_level != null) {
        CoverageCoordAxis zAxis = geoCs.getZAxis();
        Assert.assertNotNull(AxisType.Pressure.toString(), zAxis);
        Assert.assertEquals(1, zAxis.getNcoords());
        double val = zAxis.getStartValue();
        Assert.assertEquals(vert_level.doubleValue(), val, Misc.maxReletiveError);
      }
    }

  }

}
