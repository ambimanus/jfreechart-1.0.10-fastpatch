package org.jfree.chart.event;

import java.util.EventObject;
import org.jfree.data.xy.XYDataset;

/**
 * An event that can be forwarded to any
 * {@link dpg.graphics.DataAppenedListener} to signal a change to a plot.
 *
 */
/**
 * Note: Original Data Appended code/concept are from Lindsay Pender, acquired
 * from the JFreeChart Forums posted Tue Jan25,2005 "Another dynamic
 * data solution".
 *
 */

public class DataAppendedEvent extends EventObject
{
    // Fields

    /**
     * The dataset that contains this series to which data has been appended.
     */

    protected XYDataset dataset;

    /**
     * The index of the series in the containing dataset.
     */

    protected int index;

    // Methods

    /**
     * Creates a new DataAppendedEvent.
     *
     * @param source    The source of the event - the series
     * @param   set     The dataset that contains this series.
     * @param   i       The index of this series in the containing dataset.
     */

    public DataAppendedEvent(Object source, XYDataset set, int i)
    {
        super(source);
        dataset = set;
        index = i;
    }

    /**
     * Method to get the dataset that contains this series to which data has
     * been appended.
     *
     * @return  The dataset that contains this series to which data has
     *          been appended.
     */

    public XYDataset getDataset()
    {
        return dataset;
    }

    /**
     * Method to get the index of the series to which data has been appended
     * within the containing dataset.
     *
     * @return  The index of the series to which data has been appended
     *          within the containing dataset.
     */

    public int getSeriesIndex()
    {
        return index;
    }
}
