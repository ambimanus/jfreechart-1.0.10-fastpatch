package org.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * <p>Title: FastCombinedDomainXYPlot.</p>
 *
 * <p>Description: drawAppendedData appends data without redrawing the chart.
 * Calls <code>drawAppendedData()</code> method of {@link FastXYPlot}.</p>
 *
 */

/**
 * Note: Original Data Appended code/concept are from Lindsay Pender, acquired
 * from the JFreeChart Forums posted Tue Jan25,2005 "Another dynamic
 * data solution".
 *
 */

public class FastCombinedDomainXYPlot extends CombinedDomainXYPlot {

    /**
     * Default constructor.
     */
    public FastCombinedDomainXYPlot() {
        this(new NumberAxis());
    }

    /**
     * Creates a new combined plot that shares a domain axis among multiple
     * subplots.
     *
     * @param domainAxis  the shared axis.
     */
    public FastCombinedDomainXYPlot(ValueAxis domainAxis) {
        super(domainAxis);
    }


    /**
     * Does only the bare minimum calculations required to pass on to the next
     * step of the minimal draw route.
     *
     * @param g2  the graphics device.
     * @param area  the plot area (in Java2D space).
     * @param anchor  an anchor point in Java2D space (<code>null</code>
     *                permitted).
     * @param parentState  the state from the parent plot, if there is one
     *                     (<code>null</code> permitted).
     * @param info  collects chart drawing information (<code>null</code>
     *              permitted).
     */


    public void drawAppendedData(Graphics2D g2,
                                 Rectangle2D area,
                                 Point2D anchor,
                                 PlotState parentState,
                                 PlotRenderingInfo info) {

        // set up info collection...
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust the drawing area for plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        AxisSpace space = calculateAxisSpace(g2, area);
        Rectangle2D dataArea = space.shrink(area, null);

        // set the width and height of non-shared axis of all sub-plots
        setFixedRangeAxisSpaceForSubplots(space);

        // draw the shared axis
        ValueAxis axis = getDomainAxis();
        RectangleEdge edge = getDomainAxisEdge();
        double cursor = RectangleEdge.coordinate(dataArea, edge);
        AxisState axisState =  new AxisState(cursor);

        if (parentState == null) {
            parentState = new PlotState();
        }
        parentState.getSharedAxisStates().put(axis, axisState);

        // draw all the subplots
        for (int i = 0; i < this.subplots.size(); i++) {
            XYPlot plot = (XYPlot)this.subplots.get(i);
            PlotRenderingInfo subplotInfo = null;
            if (info != null) {
                subplotInfo = new PlotRenderingInfo(info.getOwner());
                info.addSubplotInfo(subplotInfo);
            }
            // If we are using the FastXYPlot which appends data...
            if (plot instanceof FastXYPlot) {
                ((FastXYPlot) plot).drawAppendedData(
                        g2, this.subplotAreas[i], anchor, parentState,
                        subplotInfo
                        );

            } else {
                plot.draw(
                        g2, this.subplotAreas[i], anchor, parentState,
                        subplotInfo
                        );
            }
        }

        if (info != null) {
            info.setDataArea(dataArea);
        }

    }
}
