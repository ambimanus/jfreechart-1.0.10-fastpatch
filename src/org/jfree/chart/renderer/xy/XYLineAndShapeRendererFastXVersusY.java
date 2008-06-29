package org.jfree.chart.renderer.xy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastXYPlot.PlotIndexes;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

/**
 * <p>Title: XYLineAndShapeRendererFastSogud.</p>
 *
 * <p>Description: A class used to render XvsY plots. It draws the last point
 * as a Shape, and has various efficiency improvements.</p>
 *

 */
public class XYLineAndShapeRendererFastXVersusY extends XYLineAndShapeRenderer {

    /**
     * The Shape to draw at the final point.
     */
    public static final Shape pointShape =
            new java.awt.geom.Ellipse2D.Double( -4.0, -4.0, 8.0, 8.0);

    /**
     *  For serialization.
     */
    private static final long serialVersionUID = -7435846137915425885L;

    /**
     * The object that carries an individual series various indexes.
     */
    private PlotIndexes currentIndex = null;


    private Shape lastPointShape;

    public Shape getLastPointShape() {
        return lastPointShape;
    }
    /**
     * Sets the PlotIndexes object.
     * @param index PlotIndexes
     */
    public void setCurrentIndex(PlotIndexes index) {
        currentIndex = index;
    }


    /**
     * Creates a new renderer with both lines and shapes visible.
     */
    public XYLineAndShapeRendererFastXVersusY() {
        super(true, false);
    }

    /**
     * Constructor that calls to XYLineAndShapeRenderer.
     * @param lines boolean lines condition.
     * @param shapes boolean shapes condition.
     */
    public XYLineAndShapeRendererFastXVersusY(final boolean lines,
                                           final boolean shapes) {
        super(lines, shapes);
    }

    /**
     * Calls the standard drawItem, which will in turn call the drawPrimaryLine
     * method, which is where the work of this class is done.
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
     * Draws the item (first pass). This method draws the lines
     * connecting the items. Only points that are greater than 2 units apart
     * will be used to draw the lines.
     *
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     */
    public void drawPrimaryLine(final XYItemRendererState state,
                                final Graphics2D g2, final XYPlot plot,
                                final XYDataset dataset, final int pass,
                                final int series, final int item,
                                final ValueAxis domainAxis,
                                final ValueAxis rangeAxis,
                                final Rectangle2D dataArea) {
        if (item == 0) {
            currentIndex.setPreviousDrawnItem(1);
            currentIndex.setLastShape(null);
            return;
        }

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        // and get the data point before it...
        double x0 = dataset.getXValue(series,
                                      item - currentIndex.getPreviousDrawnItem());
        double y0 = dataset.getYValue(series,
                                      item - currentIndex.getPreviousDrawnItem());
        if (Double.isNaN(y0) || Double.isNaN(x0)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // convert both to pixel co-ords
        double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
        double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // only draw if we have good values
        if (Double.isNaN(transX0) || Double.isNaN(transY0)
            || Double.isNaN(transX1) || Double.isNaN(transY1)) {
            return;
        }


        // If the two items are more than 2 pixels away from each other, draw.
        if (transX1 - transX0 > 2 || transX1 - transX0 < -2
            || transY1 - transY0 > 2 || transY1 - transY0 < -2) {

            state.workingLine.setLine(transX0, transY0, transX1, transY1);

            //check that the line created is inside the view of the current
            //data area. Don't try to draw if not.
            if (state.workingLine.intersects(dataArea)) {
                drawFirstPassShape(g2, pass, series, item, state.workingLine);
                currentIndex.setPreviousDrawnItem(1);
            }
        } else {
            //If item fails the 2 pixel check...
            currentIndex.incrementPreviousDrawnItem();
        }
        currentIndex.setCurrentItem(item);

        //if we have reached the last point
        if (item == (dataset.getItemCount(series) - 1)) {


           // draw over the shape at the previous last point with the
           // background color in XOR mode.
           if (currentIndex.getLastShape() != null) {
               g2.setPaint(plot.getBackgroundPaint());
               g2.setXORMode(Color.black);
               g2.draw(currentIndex.getLastShape());

           }

           // draw the last point as a shape
           lastPointShape = ShapeUtilities.createTranslatedShape(
                   pointShape, transX1, transY1
                         );
           if (lastPointShape.intersects(dataArea)) {
               if (currentIndex.getLastShape() == null) {
                   g2.setPaint(plot.getBackgroundPaint());
                   g2.setXORMode(Color.black);
               }
               g2.draw(lastPointShape);
               currentIndex.setLastShape(lastPointShape);
           }

           // reset from XOR mode.
            g2.setPaintMode();
       }

    }


}
