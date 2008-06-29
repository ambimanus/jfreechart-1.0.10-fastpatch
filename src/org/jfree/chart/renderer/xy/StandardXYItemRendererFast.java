package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastXYPlot.PlotIndexes;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.UnitType;

/**
 *
 * <p>Title: StandardXYItemRendererFast class.</p>
 *
 * <p>Description: An extension of the StandardXYItemRenderer that only renders
 * points that are greater than 2 pixel from the previously drawn point.</p>
 *

 */
public class StandardXYItemRendererFast extends StandardXYItemRenderer {

    /**
     * Constructs a new renderer.
     */
    public StandardXYItemRendererFast() {
        super();
    }

    /**
     * Constructs a new renderer.
     * <p>
     * To specify the type of renderer, use one of the constants: SHAPES, LINES
     * or SHAPES_AND_LINES.
     *
     * @param type  the type.
     */
    public StandardXYItemRendererFast(final int type) {
        super(type);
    }

    /**
     * Constructs a new renderer.
     * <p>
     * To specify the type of renderer, use one of the constants: SHAPES, LINES
     * or SHAPES_AND_LINES.
     *
     * @param type  the type of renderer.
     * @param toolTipGenerator  the item label generator (<code>null</code>
     *                          permitted).
     */
    public StandardXYItemRendererFast(final int type,
                                      final XYToolTipGenerator toolTipGenerator) {
        super(type, toolTipGenerator);
    }

    /**
     * Constructs a new renderer.
     * <p>
     * To specify the type of renderer, use one of the constants: SHAPES, LINES
     * or SHAPES_AND_LINES.
     *
     * @param type  the type of renderer.
     * @param toolTipGenerator  the item label generator (<code>null</code>
     *                          permitted).
     * @param urlGenerator  the URL generator.
     */
    public StandardXYItemRendererFast(final int type,
                                      final XYToolTipGenerator toolTipGenerator,
                                      final XYURLGenerator urlGenerator) {
        super(type, toolTipGenerator, urlGenerator);
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     * @param currentIndex the index into the series.
     */
    public void drawItem(Graphics2D g2, XYItemRendererState state,
                         Rectangle2D dataArea, PlotRenderingInfo info,
                         XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis,
                         XYDataset dataset, int series, int item,
                         CrosshairState crosshairState, int pass,
                         PlotIndexes currentIndex) {

        if (!getItemVisible(series, item)) {
            return;
        }

        // setup for collecting optional entity info...
        boolean bAddEntity = false;
        Shape entityArea = null;
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        PlotOrientation orientation = plot.getOrientation();
        Paint paint = getItemPaint(series, item);
        Stroke seriesStroke = getItemStroke(series, item);
        g2.setPaint(paint);
        g2.setStroke(seriesStroke);

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(x1) || Double.isNaN(y1)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        if (getPlotLines()) {
            if (item == 0) {
                if (this.drawSeriesLineAsPath) {
                    State s = (State) state;
                    s.seriesPath.reset();
                    s.lastPointGood = false;
                }
                currentIndex.setPreviousDrawnItem(0);
            }

            if (this.drawSeriesLineAsPath) {
                State s = (State) state;
                // update path to reflect latest point
                if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
                    float x = (float) transX1;
                    float y = (float) transY1;
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        x = (float) transY1;
                        y = (float) transX1;
                    }
                    if (s.isLastPointGood()) {
                        //** @todo Check threshold */
                        s.seriesPath.lineTo(x, y);
                    } else {
                        s.seriesPath.moveTo(x, y);
                    }
                    s.setLastPointGood(true);
                } else {
                    s.setLastPointGood(false);
                }
                if (item == dataset.getItemCount(series) - 1) {
                    // draw path
                    g2.setStroke(getSeriesStroke(series));
                    g2.setPaint(getSeriesPaint(series));
                    g2.draw(s.seriesPath);
                }
            } else if (item != 0) {
                // get the previous data point...
                double x0 = dataset.getXValue(series, item - currentIndex.getPreviousDrawnItem());
                double y0 = dataset.getYValue(series, item - currentIndex.getPreviousDrawnItem());
                if (!Double.isNaN(x0) && !Double.isNaN(y0)) {
                    boolean drawLine = true;
                    if (getPlotDiscontinuous()) {
                        // only draw a line if the gap between the current and
                        // previous data point is within the threshold
                        int numX = dataset.getItemCount(series);
                        double minX = dataset.getXValue(series, 0);
                        double maxX = dataset.getXValue(series, numX - 1);
                        if (this.gapThresholdType == UnitType.ABSOLUTE) {
                            drawLine = Math.abs(x1 - x0) <= this.gapThreshold;
                        } else {
                            drawLine = Math.abs(x1 - x0)
                                       <= ((maxX - minX) / numX
                                           * getGapThreshold());
                        }
                    }
                    if (drawLine) {
                        double transX0 = domainAxis.valueToJava2D(x0, dataArea,
                                xAxisLocation);
                        double transY0 = rangeAxis.valueToJava2D(y0, dataArea,
                                yAxisLocation);

                        // only draw if we have good values
                        if (Double.isNaN(transX0) || Double.isNaN(transY0)
                            || Double.isNaN(transX1) || Double.isNaN(transY1)) {
                            return;
                        }

                        // Only draw line if it is more than 2 pixels away from
                        // the previous one.
                        if ((transX1 - transX0 > 2 || transX1 - transX0 < -2
                             || transY1 - transY0 > 2 || transY1 - transY0 < -2)) {
                            currentIndex.setPreviousDrawnItem(1);

                            if (orientation == PlotOrientation.HORIZONTAL) {
                                state.workingLine.setLine(transY0, transX0,
                                        transY1, transX1);
                            } else if (orientation == PlotOrientation.VERTICAL) {
                                state.workingLine.setLine(transX0, transY0,
                                        transX1, transY1);
                            }

                            if (state.workingLine.intersects(dataArea)) {
                                g2.draw(state.workingLine);
                            }
                        } else {
                            //Increase counter for the previous drawn item.
                            currentIndex.incrementPreviousDrawnItem();
                            bAddEntity = false;
                        }
                    }
                }
                currentIndex.setCurrentItem(item);
            }
        }

        if (getBaseShapesVisible()) {

            Shape shape = getItemShape(series, item);
            if (orientation == PlotOrientation.HORIZONTAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, transY1,
                        transX1);
            } else if (orientation == PlotOrientation.VERTICAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, transX1,
                        transY1);
            }
            if (shape.intersects(dataArea)) {
                bAddEntity = true;
                if (getItemShapeFilled(series, item)) {
                    g2.fill(shape);
                } else {
                    g2.draw(shape);
                }
            }
            entityArea = shape;

        }

        if (getPlotImages()) {
            Image image = getImage(plot, series, item, transX1, transY1);
            if (image != null) {
                Point hotspot = getImageHotspot(plot, series, item, transX1,
                                                transY1, image);
                g2.drawImage(image, (int) (transX1 - hotspot.getX()),
                             (int) (transY1 - hotspot.getY()), null);
                entityArea = new Rectangle2D.Double(transX1 - hotspot.getX(),
                        transY1 - hotspot.getY(), image.getWidth(null),
                        image.getHeight(null));
            }

        }

        // draw the item label if there is one...
        if (isItemLabelVisible(series, item)) {
            double xx = transX1;
            double yy = transY1;
            if (orientation == PlotOrientation.HORIZONTAL) {
                xx = transY1;
                yy = transX1;
            }
            drawItemLabel(g2, orientation, dataset, series, item, xx, yy,
                          (y1 < 0.0));
        }

        updateCrosshairValues(crosshairState, x1, y1, transX1, transY1,
                              orientation);

        // add an entity for the item...
        if (entities != null && bAddEntity) {
            addEntity(entities, entityArea, dataset, series, item, transX1,
                      transY1);
        }
    }
}
