package com.yuanquan.common.widget.popup.color;

import java.util.ArrayList;
import java.util.List;

/**
 * a class to controller ui
 *
 * @author fenglibin
 * @experiment
 */
public class FastUiSettings {

    private static List<String> toolsColors = new ArrayList<String>() {
        {
            add("#EC3455");
            add("#F5AD46");
            add("#68AB5D");
            add("#32C5FF");
            add("#005BF6");
            add("#6236FF");
            add("#9E51B6");
            add("#6D7278");
        }
    };


    public static List<String> getToolsColors() {
        return toolsColors;
    }

    /**
     * change toolbox colors
     *
     * @param toolsColors
     */
    public static void setToolsColors(List<String> toolsColors) {
        FastUiSettings.toolsColors = toolsColors;
    }
}
