package com.mytechia.robobo.framework.hri.vision.colorMeasurement;

import com.mytechia.robobo.framework.IModule;

/**
 * Module that allows to measure te color in front of the camera and decomposes it in the three RGB
 * color channels
 */

public interface IColorMesaurementModule extends IModule {
    /**
     * Suscribes a listener to the newcolor notifications
     * @param listener The listener to be added
     */
    void suscribe(IColorMesauredListener listener);

    /**
     * Unsuscribes a listener from the newcolor notifications
     * @param listener The listener to be removed
     */
    void unsuscribe(IColorMesauredListener listener);
}
