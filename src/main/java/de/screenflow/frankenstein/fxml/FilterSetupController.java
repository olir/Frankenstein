package de.screenflow.frankenstein.fxml;

import de.screenflow.frankenstein.vf.LocalVideoFilter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class FilterSetupController {

	@FXML ComboBox<LocalVideoFilter> cbFilter;

	private ProcessingSceneController parent;

	private Stage stage;

	private LocalVideoFilter selectedFilter = null;
	
	public void configure(ProcessingSceneController parent, Stage stage) {
		this.parent = parent;
		this.stage = stage;
	
		cbFilter.getItems().setAll(parent.getLocalFilters());
		
		cbFilter.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LocalVideoFilter>() {
		      @Override public void changed(ObservableValue<? extends LocalVideoFilter> selected, LocalVideoFilter oldFilter, LocalVideoFilter newFilter) {
		        if (oldFilter != null) {
		        	selectedFilter = null;
		        }
		        if (newFilter != null) {
		        	selectedFilter = newFilter;
		        }
		      }
		    });
		}
	
	public void ok() {
		System.out.println("ok");
		stage.close();
	}

	public LocalVideoFilter getSelectedFilterType() {
		return selectedFilter;
	}

}
