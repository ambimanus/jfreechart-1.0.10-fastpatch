package org.jfree.chart.plot;

// FastXYSeries



import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.StandardXYItemRendererFast;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRendererFastScatter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRendererFastXVersusY;
import org.jfree.chart.renderer.xy.XYStepRendererFast;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 *
 * <p>Title: FastXYPlot </p>
 *
 * <p>Description: Upon recieving a minimalDraw command, decides what kind
 * of renderer is being used, and passes to it the appropriate indexes
 * to be used in order to draw the least number of lines/points possible.</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 */

/**
 * Note: Original Data Appended code/concept are from Lindsay Pender, acquired
 * from the JFreeChart Forums posted Tue Jan25,2005 "Another dynamic
 * data solution".
 *
 */

public class FastXYPlot extends XYPlot implements Serializable {
    // Fields

    /**
     * Flag to indicate that the renderer is to perform a minimal draw.
     */
    private boolean minimalDraw;

    /**
     * A list of indexes pertaining to each series used by a renderer.
     */
    private List indexList;

    // Methods

    /**
     * Default constructor.
     */

    public FastXYPlot() {
        this(null, null, null, null);
    }

    /**
     * Creates a new plot.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param domainAxis  the domain axis (<code>null</code> permitted).
     * @param rangeAxis  the range axis (<code>null</code> permitted).
     * @param renderer  the renderer (<code>null</code> permitted).
     */

    public FastXYPlot(XYDataset dataset, ValueAxis domainAxis,
                      ValueAxis rangeAxis, XYItemRenderer renderer) {
        super(dataset, domainAxis, rangeAxis, renderer);
        int numSeries = super.getSeriesCount();
        indexList = new ArrayList();
        for (int i = 0; i < numSeries; i++) {
            indexList.add(new PlotIndexes(i, dataset.getSeriesKey(i)));
        }
    }

    /**
     * Draws the plot within the specified area on a graphics device.
     *
     * This method is called by a full draw.
     *
     * @param g2        The graphics device.
     * @param area      The plot area (in Java2D space).
     * @param anchor    An anchor point in Java2D space (<code>null</code>
     *                  permitted).
     * @param parentState   The state from the parent plot, if there is one
     *                      (<code>null</code>  permitted).
     * @param info      Collects chart drawing information (<code>null</code>
     *                  permitted).
     */
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
                     PlotState parentState, PlotRenderingInfo info) {
        minimalDraw = false;
        super.draw(g2, area, anchor, parentState, info);
    }

    /**
     * Draws the appended data for the plot within the specified area on a
     * graphics device.
     *
     * This method is called for a minimal (or appended) draw.
     *
     * Does the minimum of calculations necessary in order to pass the
     * correct informatio on to the renderer.
     *
     * @param g2        The graphics device.
     * @param area      The plot area (in Java2D space).
     * @param anchor    An anchor point in Java2D space (<code>null</code>
     *                  permitted).
     * @param info      Collects plot drawing information (<code>null</code>
     *                  not permitted).
     */

    public void drawAppendedData(Graphics2D g2, Rectangle2D area,
                                 Point2D anchor, PlotState parentState,
                                 PlotRenderingInfo info) {

        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        minimalDraw = true;

        // record the plot area...
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust the drawing area for the plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        AxisSpace space = calculateAxisSpace(g2, area);
        Rectangle2D dataArea = space.shrink(area, null);
        this.axisOffset.trim(dataArea);

        if (info != null) {
            info.setDataArea(dataArea);
        }

        if (anchor != null && !dataArea.contains(anchor)) {
            anchor = null;
        }
        CrosshairState crosshairState = new CrosshairState();
        crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);
        crosshairState.setAnchor(anchor);
        crosshairState.setCrosshairX(getDomainCrosshairValue());
        crosshairState.setCrosshairY(getRangeCrosshairValue());
        Shape originalClip = g2.getClip();
        Composite originalComposite = g2.getComposite();

        g2.clip(dataArea);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                getForegroundAlpha()));

        // Now render data items...

        boolean foundData = false;
        DatasetRenderingOrder order = getDatasetRenderingOrder();
        if (order == DatasetRenderingOrder.FORWARD) {

            for (int i = 0; i < getDatasetCount(); i++) {
                foundData = render(g2, dataArea, i, info, crosshairState)
                            || foundData;
            }
        } else { // DatasetRenderingOrder.REVERSE

            for (int i = getDatasetCount() - 1; i >= 0; i--) {
                foundData = render(g2, dataArea, i, info, crosshairState)
                            || foundData;
            }
        }

        g2.setClip(originalClip);
        g2.setComposite(originalComposite);

    }

    /**
     * Draws a representation of the data within the dataArea region, using the
     * current renderer.
     * <P>
     * The <code>info</code> and <code>crosshairState</code> arguments may be
     * <code>null</code>.
     *
     * This method overrides the one in the super class, so BOTH full and
     * minimal draws come through here. Based on the status of the minimalDraw
     * flag different actions are taken.
     *
     * @param g2                The graphics device.
     * @param dataArea          The region in which the data is to be drawn.
     * @param index             The dataset index.
     * @param info              An optional object for collection dimension
     *                          information.
     * @param crosshairState    Collects crosshair information
     *                          (<code>null</code> permitted).
     *
     * @return A flag that indicates whether any data was actually rendered.
     */

    public boolean render(Graphics2D g2, Rectangle2D dataArea, int index,
                          PlotRenderingInfo info, CrosshairState crosshairState) {

        boolean foundData = false;
        XYDataset dataset = getDataset(index);
        if (!DatasetUtilities.isEmptyOrNull(dataset)) {

            foundData = true;
            ValueAxis xAxis = getDomainAxis();
            ValueAxis axis = getDomainAxisForDataset(index);
            if (axis != null) {
                xAxis = axis;
            }

            ValueAxis yAxis = getRangeAxis();
            axis = getRangeAxisForDataset(index);
            if (axis != null) {
                yAxis = axis;
            }

            XYItemRenderer renderer = getRenderer(index);
            if (renderer == null) {
                renderer = getRenderer();
            }

            XYItemRendererState state = renderer.initialise(g2, dataArea, this,
                    dataset, info);

            int passCount = renderer.getPassCount();

            // all renderers customised to use only one pass for efficiency's
            // sake. But just in case...
            for (int pass = 0; pass < passCount; pass++) {
                int seriesCount = dataset.getSeriesCount();
                for (int series = 0; series < seriesCount; series++) {

                    // if plot is a Continuous
                    // All the calls to renderers follow this form, so minimal
                    // commenting after this.
                    if (renderer instanceof StandardXYItemRendererFast) {
                        int itemCount = dataset.getItemCount(series);
                        PlotIndexes currentIndex = (PlotIndexes) indexList.get(
                                series);

                        // if minimalDraw is false, a full draw has been asked
                        // for, so set the current item to the beginning, or
                        // zero.
                        // In the case the itemCount is less than the current
                        // index, this means that points have been removed
                        // from the series. Redraw the whole plot again.
                        if (!minimalDraw
                            || (currentIndex.getCurrentItem() >= itemCount)) {
                            currentIndex.setCurrentItem(-1);
                            currentIndex.setPreviousDrawnItem(1);
                        }

                        // Now loop through the renderer from the beginning,
                        // in the case of a full draw, or from the first new
                        // point recieved, in the case of a minimal draw.
                        for (int item = currentIndex.getCurrentItem() + 1;
                                        item < itemCount; item++) {

                            ((StandardXYItemRendererFast) renderer).drawItem(g2,
                                    state, dataArea, info, this, xAxis, yAxis,
                                    dataset, series, item, crosshairState, pass,
                                    currentIndex);
                        }

                        //if plot is a Step plot
                    } else if (renderer instanceof XYStepRendererFast) {
                        int itemCount = dataset.getItemCount(series);
                        PlotIndexes currentIndex = (PlotIndexes) indexList.get(
                                series);

                        // if minimalDraw is false, a full draw has been asked
                        // for, so set the current item to the beginning, or
                        // zero.
                        // In the case the itemCount is less than the current
                        // index, this means that points have been removed
                        // from the series. Redraw the whole plot again.
                        if (!minimalDraw
                            || (currentIndex.getCurrentItem() >= itemCount)) {
                            currentIndex.setCurrentItem(-1);
                            currentIndex.setPreviousDrawnItem(1);
                            currentIndex.setPrintIt(false);
                        } else {
                            currentIndex.setPrintIt(true);
                        }

                        for (int item = currentIndex.getCurrentItem() + 1;
                                        item < itemCount; item++) {

                            ((XYStepRendererFast) renderer).drawItem(g2, state,
                                    dataArea, info, this, xAxis, yAxis, dataset,
                                    series, item, crosshairState, pass,
                                    currentIndex);
                        }
                        // if plot is a scatter plot
                    } else if (renderer
                               instanceof XYLineAndShapeRendererFastScatter) {
                        int itemCount = dataset.getItemCount(series);
                        PlotIndexes currentIndex = (PlotIndexes) indexList.get(
                                series);
                        ((XYLineAndShapeRendererFastScatter) renderer).
                                setCurrentIndex(currentIndex);

                        // if minimalDraw is false, a full draw has been asked
                        // for, so set the current item to the beginning, or
                        // zero.
                        // In the case the itemCount is less than the current
                        // index, this means that points have been removed
                        // from the series. Redraw the whole plot again.
                        if (!minimalDraw
                            || (currentIndex.getCurrentItem() >= itemCount)) {
                            currentIndex.setCurrentItem(-1);
                            currentIndex.setPreviousDrawnItem(1);
                        }

                        for (int item = currentIndex.getCurrentItem() + 1;
                                        item < itemCount; item++) {
                            ((XYLineAndShapeRendererFastScatter) renderer).
                                    drawSecondaryPass(g2, this, dataset, pass,
                                    series, item, xAxis, dataArea, yAxis,
                                    crosshairState, null);

                        }
                        // if plot is an XvsY plot
                    } else if (renderer
                               instanceof XYLineAndShapeRendererFastXVersusY) {
                        if (pass == 0) {
                            int itemCount = dataset.getItemCount(series);
                            PlotIndexes currentIndex = (PlotIndexes) indexList.
                                    get(series);
                            ((XYLineAndShapeRendererFastXVersusY) renderer).
                                    setCurrentIndex(currentIndex);

                            // if minimalDraw is false, a full draw has been asked
                            // for, so set the current item to the beginning, or
                            // zero.
                            // In the case the itemCount is less than the current
                            // index, this means that points have been removed
                            // from the series. Redraw the whole plot again.
                            if (!minimalDraw
                                || (currentIndex.getCurrentItem() >= itemCount)) {
                                currentIndex.setCurrentItem(-1);
                                currentIndex.setPreviousDrawnItem(1);
                            }

                            for (int item = currentIndex.getCurrentItem() + 1;
                                            item < itemCount; item++) {
                                ((XYLineAndShapeRendererFastXVersusY) renderer).
                                        drawPrimaryLine(state, g2, this,
                                        dataset, pass, series, item, xAxis,
                                        yAxis, dataArea);

                            }
                        }
                        // if plot is anything else (ie a plot that has no
                        // capacity for a minimal draw, or more accurately,
                        // a kind of plot not supported by our application)
                    } else {

                        int itemCount = dataset.getItemCount(series);
                        for (int item = 0; item < itemCount; item++) {
                            renderer.drawItem(g2, state, dataArea, info, this,
                                              xAxis, yAxis, dataset, series,
                                              item, crosshairState, pass);
                        }
                    }
                }
            }
        }

        return foundData;
    }

    /**
     *
     * <p>Title: PlotIndexes.</p>
     *
     * <p>Description: A 'struct' that stores indexes and other information
     * used by individual series that share a common renderer.</p>
     *
     */
    public class PlotIndexes {
        /**
         * The last item that has been drawn.
         */
        private int lastDrawnItem = 0;

        /**
         * The previously drawn Item. Used by renderer to record which points
         * pass the 2 pixel efficency check.
         */
        private int previousDrawnItem = 0;

        /**
         * Flag to enable print of debug messages.
         */
        private boolean printit = false;
        /**
         * The name of the series.
         */
        private String seriesName = null;
        /**
         * The index to the series saved.
         */
        private int currentItem = 0;
        /**
         * The index of the series that uses this object.
         */
        private int index;

        /**
         * For XvsY plots. The last translated shape drawn.
         */
        private Shape lastShape = null;
        /**
         * Constructor.
         * @param seriesIndex int the series Index.
         * @param seriesName String the name of the series as a string.
         */
        public PlotIndexes(final int seriesIndex, final  Object seriesName) {
            this.index = seriesIndex;
            this.seriesName = (String) seriesName;
        }

        /**
         * Accessor.
         * @return int lastDrawnItem.
         */

        public int getLastDrawnItem() {
            return lastDrawnItem;
        }
        /**
         * The series name.
         * @return String series name,
         */
        public String getSeriesName() {
            return seriesName;

        }

        /**
         * Accessor.
         * @return int lastDrawnItem.
         */
        public int getPreviousDrawnItem() {
            return previousDrawnItem;
        }


        /**
         * Mutator.
         * @param item int the item to set it to.
         */
        public void setPreviousDrawnItem(final int item) {
            previousDrawnItem = item;
        }


        /**
         * Increases the previousDrawnItem by one.
         */
        public void incrementPreviousDrawnItem() {
            previousDrawnItem++;
        }

        /**
         * Accessor.
         * @return int
         */
        public Shape getLastShape() {
            return lastShape;
        }

        /**
         * Mutator.
         * @param last Shape the Shape to set it to.
         */
        public void setLastShape(final Shape last) {
            lastShape = last;
        }

        /**
         * For debug.
         * @return boolean whether to print messages or not.
         */
        public boolean printit() {
            return printit;
        }
        /**
         * For debug.
         * @param print boolean whether to print messages or not.
         */
        public void setPrintIt(final boolean print) {
            printit = print;
        }

        /**
         * Sets the current item.
         * @param currentItem int to set the index in the series.
         */
        public void setCurrentItem(final int currentItem) {
            this.currentItem = currentItem;
        }
        /**
         * Gets the current item that has been processed in the series.
         * @return int item
         */
        public int getCurrentItem() {
            return currentItem;
        }
    }
}
