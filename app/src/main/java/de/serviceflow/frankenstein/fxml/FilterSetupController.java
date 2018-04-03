package de.serviceflow.frankenstein.fxml;

import java.net.URL;
import java.util.List;

import de.serviceflow.frankenstein.Configuration;
import de.serviceflow.frankenstein.plugin.api.SegmentConfigController;
import de.serviceflow.frankenstein.plugin.api.SegmentVideoFilter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FilterSetupController {

	// private ProcessingSceneController parent;

	private Stage stage;

	private SegmentVideoFilter selectedFilter = null;

	@FXML
	ComboBox<SegmentVideoFilter> cbFilter;

	@FXML
	BorderPane bpContainer;

	public void configure(ProcessingSceneController parent, Stage stage) {
		// this.parent = parent;
		this.stage = stage;

		List<SegmentVideoFilter> flist = ((Configuration)Configuration.getInstance()).getPluginManager().getLocalFilters();
		if (flist == null) {
			new Error("No Filters!?").printStackTrace();
		}
		cbFilter.getItems().setAll(flist);

		cbFilter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SegmentVideoFilter>() {
			@Override
			public void changed(ObservableValue<? extends SegmentVideoFilter> selected, SegmentVideoFilter oldFilter,
					SegmentVideoFilter newFilter) {
				if (oldFilter != null) {
					bpContainer.setCenter(null);
					selectedFilter = null;
				}
				if (newFilter != null) {
					selectedFilter = newFilter.createInstance();
					URL url = getClass().getResource("application.css");
					String stylesheet = url.toExternalForm();
					Scene scene = selectedFilter.createConfigurationScene(stylesheet);
					bpContainer.setCenter(scene.getRoot());
					SegmentConfigController c = ((SegmentVideoFilter) selectedFilter).getConfigController();
					c.bind(parent, selectedFilter);
					c.fireChange();
				}
			}
		});
	}

	public void ok() {
		System.out.println("ok");
		stage.close();
	}

	public SegmentVideoFilter getSelectedFilterInstance() {
		return selectedFilter;
	}

}
