/**
 * *****************************************************************************
 * <p>
 * Copyright (C) 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright (C) 2017 Luis Llamas <luis.llamas@mytechia.com>
 * <p>
 * This file is part of Robobo App Setup.
 * ****************************************************************************
 */
package com.mytechia.robobo.framework.hri.vision.util;

import java.io.Serializable;

/**
 * HSV Range of a color
 *
 * @author Luis Llamas luis.llamas@mytechia.com.
 */
public interface IColorCalibrationData extends Serializable {
    int type_HSV = 0;
    int type_HIST = 1;
    int type_NONE = -1;

    int type = type_NONE;

    int getType();
}
