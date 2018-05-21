package moe.lyrebird.view.components;

import moe.tristan.easyfxml.api.FxmlController;
import moe.tristan.easyfxml.api.FxmlFile;
import moe.tristan.easyfxml.api.FxmlNode;
import moe.lyrebird.view.components.controlbar.ControlBarController;
import moe.lyrebird.view.components.timeline.TimelineController;

public enum Components implements FxmlNode {
    TIMELINE("timeline/Timeline.fxml", TimelineController.class),
    CONTROL_BAR("controlbar/ControlBar.fxml", ControlBarController.class);

    private static String COMPONENTS_BASE_PATH = "moe/lyrebird/view/components/";

    private final String filePath;
    private final Class<? extends FxmlController> controllerClass;

    Components(final String filePath, final Class<? extends FxmlController> controllerClass) {
        this.filePath = filePath;
        this.controllerClass = controllerClass;
    }

    @Override
    public FxmlFile getFile() {
        return () -> COMPONENTS_BASE_PATH + filePath;
    }

    @Override
    public Class<? extends FxmlController> getControllerClass() {
        return controllerClass;
    }
}