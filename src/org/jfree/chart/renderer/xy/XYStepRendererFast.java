/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -------------------
 * XYStepRenderer.java
 * -------------------
 * (C) Copyright 2002-2005, by Roger Studner and Contributors.
 *
 * Original Author:  Roger Studner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Matthias Rose;
 *
 * $Id: XYStepRenderer.java,v 1.7.2.3 2005/12/02 11:59:43 mungady Exp $
 *
 * Changes
 * -------
 * 13-May-2002 : Version 1, contributed by Roger Studner (DG);
 * 25-Jun-2002 : Updated import statements (DG);
 * 22-Jul-2002 : Added check for null data items (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem() method signature (DG);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 28-Oct-2003 : Added tooltips, code contributed by Matthias Rose
 *               (RFE 824857) (DG);
 * 10-Feb-2004 : Removed working line (use line from state object instead) (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState.  Renamed
 *               XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 19-Jan-2005 : Now accesses only primitives from dataset (DG);
 * 15-Mar-2005 : Fix silly bug in drawItem() method (DG);
 * 19-Sep-2005 : Extend XYLineAndShapeRenderer (fixes legend shapes), added
 *               support for series visibility, and use getDefaultEntityRadius()
 *               for entity hotspot size (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastXYPlot.PlotIndexes;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * Line/Step item renderer for an {@link XYPlot} that reduces the number of
 * redundant line draws.  This class draws lines between data points, only
 * allowing horizontal or vertical lines (steps).
 *
 * Many changes for efficiency.
 */
public class XYStepRendererFast extends XYStepRenderer {

    /** For serialization. */
    private static final long serialVersionUID = -8918141928884796008L;

    /**
     * Constructs a new renderer with no tooltip or URL generation.
     */
    public XYStepRendererFast() {
        this(null, null);
    }

    /**
     * Constructs a new renderer.
     *
     * @param toolTipGenerator  the item label generator.
     * @param urlGenerator  the URL generator.
     */
    public XYStepRendererFast(final XYToolTipGenerator toolTipGenerator,
                              final XYURLGenerator urlGenerator) {
        super(toolTipGenerator, urlGenerator);


    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the vertical axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index (ignored here).
     * @param currentIndex the index in the series.
     */
    public void drawItem(final Graphics2D g2, final XYItemRendererState state,
                         final Rectangle2D dataArea,
                         final PlotRenderingInfo info, final XYPlot plot,
                         final ValueAxis domainAxis, final ValueAxis rangeAxis,
                         final XYDataset dataset, final int series,
                         final int item, final CrosshairState crosshairState,
                         final int pass,
                         PlotIndexes currentIndex) {

        // do nothing if series is not visible
        if (!isSeriesVisible(series)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();

        Paint seriesPaint = getItemPaint(series, item);
        Stroke seriesStroke = getItemStroke(series, item);
        g2.setPaint(seriesPaint);
        g2.setStroke(seriesStroke);

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double domain1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double range1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // Intialize values for first point, but don't draw point.
        if (item == 0) {
            currentIndex.setPreviousDrawnItem(1);
            return;
        } else if (item > 0) {
            // get the previous data point...
            double x0 = dataset.getXValue(series,
                                          item
                                          - currentIndex.getPreviousDrawnItem());
            double y0 = dataset.getYValue(series,
                                          item
                                          - currentIndex.getPreviousDrawnItem());
            if (!Double.isNaN(y0)) {
                double domain0 = domainAxis.valueToJava2D(x0, dataArea,
                        xAxisLocation);
                double range0 = rangeAxis.valueToJava2D(y0, dataArea,
                        yAxisLocation);


                // Don't plot if it is within 2 pixels of
                // last point. Check is for x1 to x2 or y1 to y2.
                if (domain1 - domain0 > 2 || domain1 - domain0 < -2
                      || range1 - range0 > 2 || range1 - range0 < -2)
                  {
                    // Draw line step from start to end
                    drawStep(g2, state, domain0, range0, domain1, range1,dataArea);
                    currentIndex.setPreviousDrawnItem(1);
                } else {
                    currentIndex.incrementPreviousDrawnItem();
                }
            }
            currentIndex.setCurrentItem(item);
        }

        updateCrosshairValues(crosshairState, x1, y1, domain1, range1,
                              orientation);
    }

    /**
     * Draws the step shape on the graphic. Draws the horizontal line follwed by
     * the vertical step.
     *
     * @param g2 Graphics2D the graphic device.
     * @param state XYItemRendererState current state of the renderer.
     * @param x0 double the x-value of point 1.
     * @param y0 double the y-value of point 1.
     * @param x1 double the x-value of point 2.
     * @param y1 double the y-value of point 2.
     * @param dataArea Rectangle2D to plot area.
     */
    private void drawStep(final Graphics2D g2, final XYItemRendererState state,
                          final double x0, final double y0, final double x1,
                          final double y1,final Rectangle2D dataArea) {

        Line2D line = state.workingLine;
        line.setLine(x0, y0, x1, y0);
        // if that horizontal line is within the dataArea... draw.
        if (state.workingLine.intersects(dataArea)) {
            g2.draw(line);
            line.setLine(x1, y0, x1, y1);
            g2.draw(line);
        }
    }
}
