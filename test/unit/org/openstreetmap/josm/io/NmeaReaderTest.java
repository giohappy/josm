// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.io;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxConstants;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.testutils.JOSMTestRules;
import org.openstreetmap.josm.tools.date.DateUtils;
import org.xml.sax.SAXException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests of {@link NmeaReader} class.
 */
public class NmeaReaderTest {
    /**
     * Set the timezone and timeout.
     */
    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public JOSMTestRules test = new JOSMTestRules();

    /**
     * Tests reading a nmea file.
     * @throws Exception if any error occurs
     */
    @Test
    public void testReader() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        final NmeaReader in = new NmeaReader(new FileInputStream("data_nodist/btnmeatrack_2016-01-25.nmea"));
        assertEquals(30, in.getNumberOfCoordinates());
        assertEquals(0, in.getParserMalformed());

        final List<WayPoint> wayPoints = new ArrayList<>(in.data.tracks.iterator().next().getSegments().iterator().next().getWayPoints());
        assertEquals("2016-01-25T05:05:09.2Z", wayPoints.get(0).get(GpxConstants.PT_TIME));
        assertEquals("2016-01-25T05:05:09.4Z", wayPoints.get(1).get(GpxConstants.PT_TIME));
        assertEquals("2016-01-25T05:05:09.6Z", wayPoints.get(2).get(GpxConstants.PT_TIME));
        assertEquals(wayPoints.get(0).getTime(), DateUtils.fromString(wayPoints.get(0).get(GpxConstants.PT_TIME).toString()));

        final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        assertEquals("2016-01-25T06:05:09.200+01", iso8601.format(wayPoints.get(0).getTime()));
        assertEquals("2016-01-25T06:05:09.400+01", iso8601.format(wayPoints.get(1).getTime()));
        assertEquals("2016-01-25T06:05:09.600+01", iso8601.format(wayPoints.get(2).getTime()));

        assertEquals(new LatLon(46.98807, -1.400525), wayPoints.get(0).getCoor());
        assertEquals("38.9", wayPoints.get(0).get(GpxConstants.PT_ELE));
        assertEquals("16", wayPoints.get(0).get(GpxConstants.PT_SAT));
        assertEquals("3d", wayPoints.get(0).get(GpxConstants.PT_FIX));
        assertEquals("0.7", wayPoints.get(0).get(GpxConstants.PT_HDOP).toString().trim());
        assertEquals(null, wayPoints.get(0).get(GpxConstants.PT_VDOP));
        assertEquals(null, wayPoints.get(0).get(GpxConstants.PT_PDOP));
    }

    private static void compareWithReference(int ticket, String filename, int numCoor) throws IOException, SAXException {
        GpxData gpx = GpxReaderTest.parseGpxData(TestUtils.getRegressionDataFile(ticket, filename+".gpx"));
        NmeaReader in = new NmeaReader(new FileInputStream(TestUtils.getRegressionDataFile(ticket, filename+".nmea")));
        assertEquals(numCoor, in.getNumberOfCoordinates());
        assertEquals(0, in.getParserMalformed());
        assertEquals(in.data.dataSources, gpx.dataSources);
        assertEquals(1, gpx.tracks.size());
        assertEquals(1, in.data.tracks.size());
        GpxTrack gpxTrack = gpx.tracks.iterator().next();
        GpxTrack nmeaTrack = in.data.tracks.iterator().next();
        assertEquals(gpxTrack.getBounds(), nmeaTrack.getBounds());
        assertEquals(1, gpxTrack.getSegments().size());
        assertEquals(1, nmeaTrack.getSegments().size());
        GpxTrackSegment gpxSeg = gpxTrack.getSegments().iterator().next();
        GpxTrackSegment nmeaSeg = nmeaTrack.getSegments().iterator().next();
        assertEquals(gpxSeg.getBounds(), nmeaSeg.getBounds());
        assertEquals(numCoor, gpxSeg.getWayPoints().size());
        assertEquals(numCoor, nmeaSeg.getWayPoints().size());
        WayPoint gpxWpt = gpxSeg.getWayPoints().iterator().next();
        WayPoint nmeaWpt = nmeaSeg.getWayPoints().iterator().next();
        assertEquals(gpxWpt.getCoor().getRoundedToOsmPrecision(), nmeaWpt.getCoor().getRoundedToOsmPrecision());
    }

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/1433">Bug #1433</a>.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTicket1433() throws Exception {
        compareWithReference(1433, "2008-08-14-16-04-58", 1241);
    }

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/1853">Bug #1853</a>.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTicket1853() throws Exception {
        compareWithReference(1853, "PosData-20081216-115434", 1285);
    }

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/2147">Bug #2147</a>.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTicket2147() throws Exception {
        compareWithReference(2147, "WG20080203171807.log", 487);
    }
}
