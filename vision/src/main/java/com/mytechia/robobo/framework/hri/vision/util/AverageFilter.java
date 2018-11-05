/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo Remote Control Module.
 *
 *   Robobo Remote Control Module is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo Remote Control Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo Remote Control Module.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.hri.vision.util;

import java.util.ArrayList;
import java.util.LinkedList;


public class AverageFilter implements IFilter {

    private int size = 5;
    private int sum = 0;
    private LinkedList<Integer> buffer;

    public AverageFilter(int size){
        this.size = size;
        buffer = new LinkedList<>();
        for (int i = 0; i < size; i++){
            buffer.push(0);
        }
    }

    @Override
    public int filter(int value) {
        buffer.removeFirst();
        buffer.push(value);
        for (Integer val:buffer){
            sum = sum+val;
        }
        return sum/size;
    }

    @Override
    public void resetFilter() {
        buffer = new LinkedList<>();
        for (int i = 0; i < size; i++){
            buffer.push(0);
        }
    }
}
