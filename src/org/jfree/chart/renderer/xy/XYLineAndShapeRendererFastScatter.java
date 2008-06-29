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
 * ---------------------------
 * XYLineAndShapeRenderer.java
 * ---------------------------
 * (C) Copyright 2004, 2005, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: XYLineAndShapeRenderer.java,v 1.20.2.5 2005/11/28 12:06:35 mungady Exp $
 *
 * Changes:
 * --------
 * 27-Jan-2004 : Version 1 (DG);
 * 10-Feb-2004 : Minor change to drawItem() method to make cut-and-paste
 *               overriding easier (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 25-Aug-2004 : Added support for chart entities (required for tooltips) (DG);
 * 24-Sep-2004 : Added flag to allow whole series to be drawn as a path
 *               (necessary when using a dashed stroke with many data
 *               items) (DG);
 * 04-Oct-2004 : Renamed BooleanUtils --> BooleanUtilities (DG);
 * 11-Nov-2004 : Now uses ShapeUtilities to translate shapes (DG);
 * 27-Jan-2005 : The getLegendItem() method now omits hidden series (DG);
 * 28-Jan-2005 : Added new constructor (DG);
 * 09-Mar-2005 : Added fillPaint settings (DG);
 * 20-Apr-2005 : Use generators for legend tooltips and URLs (DG);
 * 22-Jul-2005 : Renamed defaultLinesVisible --> baseLinesVisible,
 *               defaultShapesVisible --> baseShapesVisible and
 *               defaultShapesFilled --> baseShapesFilled (DG);
 * 29-Jul-2005 : Added code to draw item labels (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastXYPlot.PlotIndexes;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

/**
 *
 * <p>Title: XYLineAndShapeRendererFastScatter class.</p>
 *
 * <p>Description: A renderer used to draw the Scatter plots, with various
 * efficiency improvements.
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Telesat Canada</p>
 *
 * @author Brian, Jamie
 * @version 0.0.4
 */
public class XYLineAndShapeRendererFastScatter extends XYLineAndShapeRenderer {

    /**
     *  For serialization.
     */
    private static final long serialVersionUID = -7435246835986425885L;

    /**
    * The object that carries an individual series various indexes.
    */
    private PlotIndexes currentIndex = null;
    /**
     * Creates a new renderer with lines not visible and shapes visible.
     */
    public XYLineAndShapeRendererFastScatter() {
        super(false, true);
    }

    /**
     * Creates a new renderer with the passed in parameters.
     * @param lines boolean lines to be drawn or not.
     * @param shapes boolean shapes to be drawn or not.
     */
    public XYLineAndShapeRendererFastScatter(boolean lines, boolean shapes) {
        super(lines, shapes);
    }

    /**
     * Sets the PlotIndexes object.
     * @param index PlotIndexes
     */
    public void setCurrentIndex(PlotIndexes index) {
        currentIndex = index;
    }

    /**
       * Calls the standard drawItem, which will in turn call the
       * drawSecondaryPass method, which is where the work of this class is
       * done.
       *
       * @param g2 Graphics2D
       * @param state XYItemRendererState
       * @param dataArea Rectangle2D
       * @param info PlotRenderingInfo
       * @param plot XYPlot
       * @param domainAxis ValueAxis
       * @param rangeAxis ValueAxis
       * @param dataset XYDataset
       * @param series int
       * @param item int
       * @param crosshairState CrosshairState
       * @param pass int
     */
    public void drawItem(Graphics2D g2,
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         int item,
                         CrosshairState crosshairState,
                         int pass) {
        super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
                       dataset, series, item, crosshairState, pass);
    }


    /**
     * Draws the item shapes and adds chart entities (second pass). This method
     * draws the shapes which mark the item positions. If <code>entities</code>
     * is not <code>null</code> it will be populated with entity information.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area within which the data is being drawn.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  the crosshair state.
     * @param entities the entity collection.
     */
    public void drawSecondaryPass(Graphics2D g2, XYPlot plot,
                                  XYDataset dataset,
                                  int pass, int series, int item,
                                  ValueAxis domainAxis,
                                  Rectangle2D dataArea,
                                  ValueAxis rangeAxis,
                                  CrosshairState crosshairState,
                                  EntityCollection entities) {

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        if (getItemShapeVisible(series, item)) {
            Shape shape = getItemShape(series, item);
            shape = ShapeUtilities.createTranslatedShape(
                    shape, transX1, transY1
                    );

            double x0;
            double y0;
            double transX0 = Double.MAX_VALUE;
            double transY0 = Double.MAX_VALUE;
            // get the previous data point...
            if (item > 0) {
                x0 = dataset.getXValue(series,
                                       item
                                       - currentIndex.getPreviousDrawnItem());
                y0 = dataset.getYValue(series,
                                       item
                                       - currentIndex.getPreviousDrawnItem());

                transX0 = domainAxis.valueToJava2D(x0, dataArea,
                        xAxisLocation);
                transY0 = rangeAxis.valueToJava2D(y0, dataArea,
                                                  yAxisLocation);
            }
            //check if the shape is within the dataArea...
            if (shape.intersects(dataArea)) {

                // Only draw the shape if greater than 2 pixels away from
                // previous shape.
                if (transX1 - transX0 > 2 || transX1 - transX0 < -2
                    || transY1 - transY0 > 2 || transY1 - transY0 < -2) {

                    currentIndex.setPreviousDrawnItem(1);
                    if (getUseOutlinePaint()) {
                        g2.setPaint(getItemOutlinePaint(series, item));
                    } else {
                        g2.setPaint(getItemPaint(series, item));
                    }
                    g2.setStroke(getItemOutlineStroke(series, item));
                    if (shape.intersects(dataArea)) {
                        g2.draw(shape);
                    }
                } else {
                    //Increase counter for the previous drawn item.
                    currentIndex.incrementPreviousDrawnItem();
                }

            }
            currentIndex.setCurrentItem(item);
        }

    }

}
