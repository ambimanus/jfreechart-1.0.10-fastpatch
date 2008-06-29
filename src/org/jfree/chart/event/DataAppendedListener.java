package org.jfree.chart.event;

import java.util.EventListener;

/**
 * The interface that must be supported by classes that wish to receive
 * notification of data being appended to a series.
 *
 */
/**
 * Note: Original Data Appended code/concept are from Lindsay Pender, acquired
 * from the JFreeChart Forums posted Tue Jan25,2005 "Another dynamic
 * data solution".
 *
 */

public interface DataAppendedListener extends EventListener
{
    // Methods

    /**
     * Receives notification of a data appended event.
     *
     * @param event  The event.
     */

    public void dataAppended(DataAppendedEvent event);
}
