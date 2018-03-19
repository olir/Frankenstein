package de.screenflow.frankenstein.fxml;

import java.net.URL;

import de.screenflow.frankenstein.vf.LocalVideoFilter;
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

	private LocalVideoFilter selectedFilter = null;

	@FXML ComboBox<LocalVideoFilter> cbFilter;

	@FXML BorderPane bpContainer;
	
	public void configure(ProcessingSceneController parent, Stage stage) {
		this.parent = parent;
		this.stage = stage;
	
		cbFilter.getItems().setAll(parent.getLocalFilters());
		
		cbFilter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LocalVideoFilter>() {
		      @Override public void changed(ObservableValue<? extends LocalVideoFilter> selected, LocalVideoFilter oldFilter, LocalVideoFilter newFilter) {
		        if (oldFilter != null) {
		        	bpContainer.setCenter(null);		        	
		        	selectedFilter = null;
		        }
		        if (newFilter != null) {
		        	selectedFilter = newFilter.createInstance();
		    		URL url = getClass().getResource("application.css");
		    		String stylesheet = url.toExternalForm();
		        	Scene scene = selectedFilter.createConfigurationScene(FxMain.getLocale(), stylesheet);
		        	bpContainer.setCenter(scene.getRoot());		        	
		        }
		      }
		    });
		}
	
	public void ok() {
		System.out.println("ok");
		stage.close();
	}

	public LocalVideoFilter getSelectedFilterInstance() {
		return selectedFilter;
	}

}
