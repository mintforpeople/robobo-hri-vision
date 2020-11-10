/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2017 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo HRI Modules.
 *
 *   Robobo HRI Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo HRI Modules is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo HRI Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.hri.vision.blobTracking;


import com.mytechia.robobo.framework.IModule;

/**
 * Module that allows to perform blob tracking
 */
public interface IBlobTrackingModule extends IModule{

    /**
     * Configures detection for the four detectable colors
     * @param detectRed Red flag
     * @param detectGreen Green flag
     * @param detectBlue Blue flag
     * @param detectCustom Custom flag
     */
    void configureDetection(boolean detectRed, boolean detectGreen, boolean detectBlue, boolean detectCustom);

    /**
     * Suscribes a listener to the notifications stream
     * @param listener The listener
     */
    void suscribe(IBlobListener listener);
    /**
     * Unsuscribes a listener from the notifications stream
     * @param listener The listener
     */
    void unsuscribe(IBlobListener listener);

    /**
     * Sets the minimum number of pixels to fire a detection
     * @param th number of frames without detection to be considered lost
     * @param min_area minimum area to be considered a detection
     */
    void setThreshold(int th, int min_area);


}
