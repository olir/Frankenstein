package de.screenflow.frankenstein.fxml;

import java.net.URL;

import de.screenflow.frankenstein.vf.SegmentVideoFilter;
import de.screenflow.frankenstein.vf.segment.NativeSegmentFilter;
import de.screenflow.frankenstein.vf.segment.SegmentConfigController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

public class FilterSetupController {

	private ProcessingSceneController parent;

	private Stage stage;

	private SegmentVideoFilter selectedFilter = null;

	@FXML ComboBox<SegmentVideoFilter> cbFilter;

	@FXML BorderPane bpContainer;
	
	public void configure(ProcessingSceneController parent, Stage stage) {
		this.parent = parent;
		this.stage = stage;
	
		cbFilter.getItems().setAll(parent.getLocalFilters());
		
		cbFilter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SegmentVideoFilter>() {
		      @Override public void changed(ObservableValue<? extends SegmentVideoFilter> selected, SegmentVideoFilter oldFilter, SegmentVideoFilter newFilter) {
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
		        	SegmentConfigController c = ((SegmentVideoFilter)selectedFilter).getConfigController();
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
