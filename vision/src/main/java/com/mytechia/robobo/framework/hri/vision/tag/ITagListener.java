package com.mytechia.robobo.framework.hri.vision.tag;

import java.util.List;

public interface ITagListener {
    void onAruco(List<Tag> markers);
    //void onAruco(List<Mat> corners, Mat ids);
}
