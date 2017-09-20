package com.bth.running.coaching;

/**
 * @author Martin Macheiner
 *         Date: 20.09.2017.
 */

public interface Coach {

    void setUserBodyInformation(int height, double weight);

    double getUserWeight();

    int getUserHeight();

}
