package com.mytechia.robobo.framework.hri.vision.colorMesaurement;

import com.mytechia.robobo.framework.IModule;

/**
 * Created by luis on 19/1/17.
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
