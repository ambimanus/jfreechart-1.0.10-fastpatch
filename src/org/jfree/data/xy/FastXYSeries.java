package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.event.EventListenerList;

import org.jfree.chart.event.DataAppendedEvent;
import org.jfree.chart.event.DataAppendedListener;
import org.jfree.data.Range;

/**
 *
 * <p>Title: FastXYSeries.</p>
 *
 * <p>Description: A list of DataItems. The DataItems are either time&value or
 * value&value. Each telemetry word has one of these.</p>
 *
 */
 
/**
 * Note: Original Data Appended code/concept are from Lindsay Pender, acquired
 * from the JFreeChart Forums posted Tue Jan25,2005 "Another dynamic
 * data solution".
 *
 */

public class FastXYSeries extends XYSeries implements Cloneable, Serializable {

    private static final int INITIAL_LIST_SIZE = 10000;


    /**
     * Storage for the DataItems in the series.
     */

    protected List data;

    /**
     * The minimum X value.
     */

    protected double minX;

    /**
     * The maximum X value.
     */

    protected double maxX;

    /**
     * The minimum Y value.
     */

    protected double minY;

    /**
     * The maximum Y value.
     */

    protected double maxY;

    /**
     * The number of items for the series.
     */

    protected int itemCount;

    /**
     * The index of the last item processed.
     */

    protected int lastItemProcessed;

    /**
     * The dataset that contains this series.
     */

    protected FastXYDataset dataset;

    /**
     * The index of this series in the containing dataset.
     */
    protected int index;

    /**
     * Storage for registered data appended listeners.
     */

    private EventListenerList dataAppendedListeners;

    /**
     * The series hash code.
     */

    protected int hashCode;

    /**
     * The random number generator for production of a hash code.
     */

    protected static final Random hashGenerator = new Random();

    /**
     * The default length for the storage arrays.
     */

    protected static final int DEFAULT_LENGTH = 1000;

    // Methods

    /**
     * Creates a new empty series.
     *
     * @param name  the series name (<code>null</code> not permitted).
     */

    public FastXYSeries(String name) {
        super(name);
        data = new ArrayList(INITIAL_LIST_SIZE);
        minX = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;
        itemCount = 0;
        lastItemProcessed = -1;
        dataAppendedListeners = new EventListenerList();
        hashCode = hashGenerator.nextInt();
    }

    /**
     * Returns the number of items in the series.
     *
     * @return The item count.
     */

    public int getItemCount() {
        return itemCount;
    }

    /**
     * Method to set the index of the last item processed.
     *
     * @param   index   The index of the last item processed.
     */

    public void setLastItemProcessed(int index) {
        lastItemProcessed = index;
    }

    /**
     * Returns the index of the last item processed.
     *
     * @return The index of the last item processed.
     */

    public int getLastItemProcessed() {
        return lastItemProcessed;
    }


    /**
     * Returns the DataItem at the index;
     * @param index int the index;
     * @return DataItem the DataItem returned.
     */
    public DataItem getDataAt(int index) {
        return (DataItem) data.get(index);
    }

    /**
     * Adds a data item to the series and sends a {@link SeriesChangeEvent} to
     * all registered listeners.
     *
     * @param x  the x value.
     * @param y  the y value.
     */

    public void add(long x, double y) {
        add((double) x, y, false);
    }

    /**
     * Adds a data item to the series and sends a {@link SeriesChangeEvent} to
     * all registered listeners.
     *
     * @param x  the x value.
     * @param y  the y value.
     */

    public void add(double x, double y) {
        add(x, y, false);
    }

    /**
     * Adds a data item to the series and sends a {@link SeriesChangeEvent} to
     * all registered listeners.
     *
     * @param x  the x value.
     * @param y  the y value.
     */

    public void add(Number x, Number y) {
        add(x, y, false);
    }

    /**
     * Adds new data to the series and, if requested, sends a
     * {@link SeriesChangeEvent} to all registered listeners.
     *
     * @param x  the x-value (<code>null</code> not permitted).
     * @param y  the y-value (<code>null</code> permitted).
     * @param notify    a flag the controls whether or not a
     *                  {@link SeriesChangeEvent} is sent to all registered
     *                  listeners.
     * @throws IllegalArgumentException If an input argument is
     *              <code>null</code>
     */

    public void add(Number x, Number y, boolean notify) throws
            IllegalArgumentException {
        // Check arguments.

        if (x == null || y == null) {
            throw new IllegalArgumentException("Null argument.");
        }

        add(x.longValue(), y.doubleValue(), notify);
    }


    public boolean addAsValue(final double x, final double y, final boolean notify) {
        boolean adjusted = false;
        if (x < minX) {
            minX = x;
            adjusted = true;
        }

        if (x > maxX) {
            maxX = x;
            adjusted = true;
        }

        if (y < minY) {
            minY = y;
            adjusted = true;
        }

        if (y > maxY) {
            maxY = y;
            adjusted = true;
        }

        DataItem newItem = new DataItem(x, y);
        data.add(newItem);
        itemCount++;
//        if (notify && adjusted) {
//            fireSeriesChanged();
//        }
        return adjusted;
    }

    /**
     * Adds a data item to the series and, if requested, sends a
     * {@link SeriesChangeEvent} to all registered listeners.
     *
     * @param x  the x value.
     * @param y  the y value.
     * @param notify    a flag that controls whether or not a
     *                  {@link SeriesChangeEvent} is sent to all registered
     *                  listeners.
     */

    public void add(final double x, final double y, boolean notify) {
        DataItem newItem = new DataItem(x, y);
        if (x < minX) {
            minX = x;
        }

        if (x > maxX) {
            maxX = x;
        }

        if (y < minY) {
            minY = y;
        }

        if (y > maxY) {
            maxY = y;
        }

        // Now search the list and insert. Duplicates are overwritten with last
        // submitted value.

        // make the change (if it's not a duplicate time period)...

        int count = data.size();
        if (count == 0) {
            data.add(newItem);
            itemCount++;
        } else {
            DataItem last = (DataItem) data.get(count - 1);
            if (newItem.compareTo(last) > 0) {
                data.add(newItem);
                itemCount++;
            } else {
                int index = Collections.binarySearch(this.data, newItem);
                if (index < 0) {
                    data.add( -index - 1, newItem);
                    itemCount++;
                } else {
                    ((DataItem) data.get(index)).yvalue = newItem.yvalue;
                }
            }
        }
        if (notify) {
            fireSeriesChanged();
//          fireDataAppended();
        }

    }



    /**
     * Deletes a range of items from the series and sends a
     * {@link SeriesChangeEvent} to all registered listeners.
     *
     * @param start  the start index (zero-based - inclusive).
     * @param end  the end index (zero-based - exclusive).
     */

    public void delete(int start, int end) {
        data.subList(start, end).clear();
        itemCount -= end - start;
        fireSeriesChanged();
        lastItemProcessed = itemCount - 1;
    }

    /**
     * Removes all data items from the series.
     */
    public void clear() {
        if (itemCount > 0) {
            minX = Double.MAX_VALUE;
            maxX = -Double.MAX_VALUE;
            minY = Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;
            itemCount = 0;
            fireSeriesChanged();
            data.clear();
        }
    }

    /**
     * Returns the x-value at the specified index.
     *
     * @param index  the index (zero-based).
     *
     * @return The x-value (never <code>null</code>).
     */

    public Number getX(int index) {
        return new Double(((DataItem) data.get(index)).xvalue);
    }

    /**
     * Returns the x-value at the specified index.
     *
     * @param index  the index (zero-based).
     *
     * @return The x-value.
     */

    public double getXValue(int index) {
        return ((DataItem) data.get(index)).xvalue;
    }

    /**
     * Returns the y-value as a Number at the specified index.
     *
     * @param index  the index (zero-based).
     *
     * @return The y-value (possibly <code>null</code>).
     */

    public Number getY(int index) {
        return new Double(((DataItem) data.get(index)).yvalue);
    }

    /**
     * Returns the y-value at the specified index.
     *
     * @param index  the index (zero-based).
     *
     * @return The y-value.
     */

    public double getYValue(int index) {
        return ((DataItem) data.get(index)).yvalue;
    }


    /**
     * Returns the range of the x-values.
     *
     * @return The range of the x-values.
     */

    public Range getDomainExtent() {
        if (minX == Double.MAX_VALUE && maxX == -Double.MAX_VALUE) {
            return new Range( -1, 1);
        }
        return new Range(minX, maxX);
    }

    /**
     * Returns the range of the y-values.
     *
     * @return The range of the y-values.
     */

    public Range getRangeExtent() {
        if (minY == Double.MAX_VALUE && maxY == -Double.MAX_VALUE) {
            return new Range( -1, 1);
        }
        return new Range(minY, maxY);
    }

    /**
     * Method to set the dataset which contains this series.
     *
     * @param   set The dataset that contains this series.
     */

    public void setDataset(FastXYDataset set) {
        dataset = set;
    }

    /**
     * Method to set the index of this series in the containing dataset.
     *
     * @param   i   The index of this series in the containing dataset.
     */

    public void setSeriesIndex(int i) {
        index = i;
    }

    /**
     * Returns the hash code.
     *
     * @return The hash code.
     */

    public int hashCode() {
        return hashCode;
    }

///////////////////////////////////////////////////////////////////////
    /**
     * The basis of the original concept for the minimal draw as created by
     * Lindsay Pender was event driven. Each new point that was recieved
     * fired an event saying 'please draw me'. For our application, we have
     * changed this to a manual call due to our charts being updated
     * often, and by an unknown amount (too many events and redraws).
     * We leave Lindsay's event coding here in case someone wishes to use it.
     *
     */
//////////////////////////////////////////////////////////////////////


    /**
     * Registers an object with this series, to receive notification whenever
     * the data is appended.
     * <P>
     * Objects being registered must implement the {@link DataAppendedListener}
     * interface.
     *
     * @param listener  the listener to register.
     */

    public void addDataAppendedListener(final DataAppendedListener listener) {
        dataAppendedListeners.add(DataAppendedListener.class, listener);
    }

    /**
     * Deregisters an object, so that it not longer receives notification
     * whenever the data is appended.
     *
     * @param listener  the listener to deregister.
     */

    public void removeChangeListener(final DataAppendedListener listener) {
        dataAppendedListeners.remove(DataAppendedListener.class, listener);
    }

    /**
     * General method for signalling to registered listeners that the series
     * has has been appended.
     */

    public void fireDataAppended() {
        if (getNotify()) {
            notifyListeners(new DataAppendedEvent(this, dataset, index));
        }
    }

    /**
     * Sends a data appended event to all registered listeners.
     *
     * @param event Contains information about the event that triggered the
     *              notification.
     */
    protected void notifyListeners(final DataAppendedEvent event) {
        final Object[] listenerList = dataAppendedListeners.getListenerList();
        for (int i = listenerList.length - 2; i >= 0; i -= 2) {
            if (listenerList[i] == DataAppendedListener.class) {
                ((DataAppendedListener) listenerList[i + 1]).
                        dataAppended(event);
            }
        }
    }

    /**
     * Returns a clone of the series.
     *
     * @return a clone of the time series.
     *
     * @throws CloneNotSupportedException if there is a cloning problem.
     */

    public Object clone() throws CloneNotSupportedException {
        final Object clone = createCopy(0, itemCount - 1);
        return clone;
    }

    /**
     * Creates a new series by copying a subset of the data in this time series.
     *
     * @param start  the index (inclusive) of the first item to copy.
     * @param end  the index (exclusive) of the last item to copy.
     *
     * @return a series containing a copy of this series from start until end.
     *
     * @throws CloneNotSupportedException if there is a cloning problem.
     */

    public XYSeries createCopy(int start, int end) throws
            CloneNotSupportedException {
        final FastXYSeries copy = (FastXYSeries)super.clone();
        copy.data = new ArrayList(data.subList(start, end));
        copy.itemCount = copy.data.size();

        return copy;
    }


    /**
     *
     * <p>Title: DataItem.</p>
     *
     * <p>Description: Represents one point received.</p>
     *

     */
    public class DataItem implements Comparable {
        /**
         * The y value.
         */
        public double yvalue;

        /**
         * The x value.
         */
        public double xvalue;

        /**
         * Constructor.
         * @param inx double the x value.
         * @param iny double the y value.
         */
        public DataItem(final double inx, final double iny) {
            xvalue = inx;
            yvalue = iny;
        }

        /**
         * Implements comparable.
         * @param o Object the object to compare to.
         * @return int the result.
         */
        public int compareTo(Object o) {

            if (xvalue < ((DataItem) o).xvalue) {
                return -1;
            }
            if (xvalue > ((DataItem) o).xvalue) {
                return +1;
            }
            return 0;
        }
    }

}
