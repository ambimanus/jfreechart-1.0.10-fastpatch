package org.jfree.data.xy;


import java.util.*;
import org.jfree.data.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;

/**
 *
 * <p>Title: FastXYDataset </p>
 *
 * <p>Description: A class that contains a list of FastXYSeries.</p>
 *
 */

/**
 * Note: Original Data Appended code/concept are from Lindsay Pender, acquired
 * from the JFreeChart Forums posted Tue Jan25,2005 "Another dynamic
 * data solution".
 *
 */
public class FastXYDataset extends XYSeriesCollection {


    /**
     * The series that are included in the dataset.
     */

    private List data;


    /**
     * Constructs a dataset and with no series.
     */

    public FastXYDataset() {
        this(null);
    }

    /**
     * Constructs a dataset and populates it with a single series.
     *
     * @param series  the series (<code>null</code> ignored).
     */

    public FastXYDataset(FastXYSeries series) {
        this.data = new java.util.ArrayList();
        if (series != null) {
            this.data.add(series);
            series.addChangeListener(this);
            series.setDataset(this);
            series.setSeriesIndex(0);
        }
    }

    /**
     * Adds a series to the collection and sends a {@link DatasetChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series (<code>null</code> not permitted).
     */

    public void addSeries(FastXYSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Null 'series' argument.");
        }

        this.data.add(series);
        series.addChangeListener(this);
        series.setDataset(this);
        series.setSeriesIndex(getSeriesCount() - 1);
        fireDatasetChanged();
    }


    /**
     * Removes a series from the collection and sends a
     * {@link DatasetChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     */

    public void removeSeries(int series) {
        if ((series < 0) || (series > getSeriesCount())) {
            throw new IllegalArgumentException("Series index out of bounds.");
        }

        // Fetch the series, remove the change listener, then remove the series.

        final FastXYSeries ts = (FastXYSeries)this.data.get(series);
        ts.removeChangeListener(this);
        this.data.remove(series);
        for (int i = 0; i < this.data.size(); i++) {
            final FastXYSeries s = (FastXYSeries)this.data.get(i);
            s.setSeriesIndex(i);
        }

        fireDatasetChanged();
    }

    /**
     * Removes a series from the collection and sends a
     * {@link DatasetChangeEvent} to all registered listeners.
     *
     * @param series  the series (<code>null</code> not permitted).
     */

    public void removeSeries(FastXYSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Null 'series' argument.");
        }

        if (this.data.contains(series)) {
            series.removeChangeListener(this);
            this.data.remove(series);
            for (int i = 0; i < this.data.size(); i++) {
                final FastXYSeries s = (FastXYSeries)this.data.get(i);
                s.setSeriesIndex(i);
            }

            fireDatasetChanged();
        }
    }

    /**
     * Removes all the series from the collection and sends a
     {@link DatasetChangeEvent} to all registered listeners.
     */

    public void removeAllSeries() {
        // Unregister the collection as a change listener to each series in
        // the collection.

        for (int i = 0; i < this.data.size(); i++) {
            final Series series = (Series)this.data.get(i);
            series.removeChangeListener(this);
        }

        // Remove all the series from the collection and notify listeners.

        this.data.clear();
        fireDatasetChanged();
    }

    /**
     * Returns the number of series in the collection.
     *
     * @return The series count.
     */

    public int getSeriesCount() {
        return this.data.size();
    }

    /**
     * Returns a list of all the series in the collection.
     *
     * @return The list (which is unmodifiable).
     */

    public List getSeries() {
        return Collections.unmodifiableList(this.data);
    }

    /**
     * Returns a series from the collection.
     *
     * @param series  the series index (zero-based).
     *
     * @return The series.
     */

    public XYSeries getSeries(int series) {
        if ((series < 0) || (series > getSeriesCount())) {
            throw new IllegalArgumentException("Series index out of bounds");
        }

        return (XYSeries)this.data.get(series);
    }

    /**
     * Returns the name of a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The name of a series.
     */

    public String getSeriesName(int series) {
        Series s = (Series) data.get(series);
        return s.getDescription();
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The item count.
     */

    public int getItemCount(int series) {
        FastXYSeries s = (FastXYSeries) data.get(series);
        return s.getItemCount();
    }

    /**
     * Returns the exent of the range of the dataset.
     *
     * @return The exent of the range of the dataset.
     */

    public Range findRangeBounds() {
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int i = 0; i < this.data.size(); i++) {
            FastXYSeries series = (FastXYSeries)this.data.get(i);
            Range range = series.getRangeExtent();
            if (range.getLowerBound() < minY) {
                minY = range.getLowerBound();
            }

            if (range.getUpperBound() > maxY) {
                maxY = range.getUpperBound();
            }
        }

        return new Range(minY, maxY);
    }

    /**
     * Returns the order of the domain (X) values.
     *
     * @return The domain order.
     */

    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    /**
     * Returns the x-value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The value.
     */

    public Number getX(int series, int item) {
        FastXYSeries s = (FastXYSeries)this.data.get(series);
        return s.getX(item);
    }

    /**
     * Returns the y-value for the specified series and item.
     *
     * @param series  the series (zero-based index).
     * @param item  the index of the item of interest (zero-based).
     *
     * @return The value (possibly <code>null</code>).
     */
    public Number getY(int series, int item) {
        FastXYSeries s = (FastXYSeries)this.data.get(series);
        return s.getY(item);
    }

    /**
     * Returns the x-value (as a double primitive) for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */

    public double getXValue(int series, int item) {
        FastXYSeries s = (FastXYSeries)this.data.get(series);
        return s.getXValue(item);
    }

    /**
     * Returns the y-value (as a double primitive) for an item within a series.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The value.
     */

    public double getYValue(int series, int item) {
        FastXYSeries s = (FastXYSeries)this.data.get(series);
        return s.getYValue(item);
    }


}
