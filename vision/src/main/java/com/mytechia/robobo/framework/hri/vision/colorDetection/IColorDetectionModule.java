/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
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

package com.mytechia.robobo.framework.hri.vision.colorDetection;

import android.graphics.Bitmap;

import com.mytechia.robobo.framework.IModule;

// OLD
public interface IColorDetectionModule extends IModule {

    /**
     * Suscribes a listener to the newcolor notifications
     * @param listener The listener to be added
     */
    void suscribe(IColorListener listener);

    /**
     * Unsuscribes a listener from the newcolor notifications
     * @param listener The listener to be removed
     */
    void unsuscribe(IColorListener listener);

    /**
     * Starts or resumes the color detection
     */
    void startDetection();

    /**
     * Pauses the color detection algorithm
     */
    void pauseDetection();
}
