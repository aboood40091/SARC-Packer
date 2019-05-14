package dev.gigaboy.sarctoolgui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String TITLE = "SARC Tool GUI";
	public static final int WIDTH = 500,
							HEIGHT = 350;
	
	private static final Insets mainIns = new Insets(5),
								headingIns = new Insets(2, 5, 2, 5);
	
	private static final Font lblFont = new Font("Sans Serif", 18);
	
	public static File srcFile = null,
					   outFile = null;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("YAZ0 Compressed File", "*.szs", "*.sarc", "*.yaz0"));
		
		DirectoryChooser dirChooser = new DirectoryChooser();
		
		
		
		/////////////////////////////// COMPRESS TAB ///////////////////////////////
		Label compressLbl = new Label("Source Directory:");
		compressLbl.setFont(lblFont);
		
		// text field which displays absolute path of selected directory
		TextField dirPathField = new TextField();
		dirPathField.setPrefWidth(350);
		dirPathField.setEditable(false);
		
		// select directory button
		Button selDirBttn = new Button("Select...");
		selDirBttn.setOnAction(e -> {
			srcFile = dirChooser.showDialog(primaryStage);
			
			if (srcFile != null)
				dirPathField.setText(srcFile.toString());
		});
		
		// horizontal box which includes the absolute path text field and the select directory button
		HBox dirChooserBox = new HBox();
		dirChooserBox.getChildren().addAll(dirPathField, selDirBttn);
		HBox.setMargin(dirPathField, mainIns);
		HBox.setMargin(selDirBttn, mainIns);
		
		// radio button label
		Label compressRBLbl = new Label("Console: ");
		compressRBLbl.setFont(lblFont);
		
		// the Switch and 3DS use a different format than Wii U, so we need to get which one
		final ToggleGroup compressTG = new ToggleGroup();
		
		RadioButton switch3dsRB = new RadioButton("Switch/3DS");
		switch3dsRB.setSelected(true);
		switch3dsRB.setToggleGroup(compressTG);
		
		RadioButton wiiuRB = new RadioButton("Wii U");
		wiiuRB.setToggleGroup(compressTG);
		
		HBox compressRBBox = new HBox();
		compressRBBox.getChildren().addAll(switch3dsRB, wiiuRB);
		HBox.setMargin(switch3dsRB, mainIns);
		HBox.setMargin(wiiuRB, mainIns);
		
		// compression level label
		Label compressionLbl = new Label("Compression [0 - fastest/largest; 9 - slowests/smallest]:");
		compressionLbl.setFont(lblFont);
		
		// compression level
		SpinnerValueFactory compressionFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9, 3);
		Spinner compressionSpnr = new Spinner();
		compressionSpnr.setEditable(true);
		compressionSpnr.setValueFactory(compressionFactory);
		compressionSpnr.getEditor().setAlignment(Pos.BASELINE_CENTER);
		compressionSpnr.getEditor().textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		            compressionSpnr.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		
		// output file label
		Label outputFileLbl = new Label("Output File: ");
		outputFileLbl.setFont(lblFont);
		
		TextField outputPathField = new TextField();
		outputPathField.setPrefWidth(350);
		outputPathField.setEditable(false);
		
		// output file button
		Button selOutputBttn = new Button("Select...");
		selOutputBttn.setOnAction(e -> {
			outFile = fileChooser.showSaveDialog(primaryStage);
			
			if (outFile != null)
				outputPathField.setText(outFile.toString());
		});
		
		// horizontal box which includes the absolute path text field and the select file button
		HBox outputChooserBox = new HBox();
		outputChooserBox.getChildren().addAll(outputPathField, selOutputBttn);
		HBox.setMargin(outputPathField, mainIns);
		HBox.setMargin(selOutputBttn, mainIns);
		
		// compress button
		Button compressBttn = new Button("Compress");
		// compress directory on click
		compressBttn.setOnAction(evt -> {
			int compr = Integer.parseInt(compressionSpnr.getEditor().getText());
			String rbVal = ((RadioButton) compressTG.getSelectedToggle()).getText();
			boolean isLittle = rbVal.equalsIgnoreCase("Wii U") ? false : true;
			
			try {
				compress(srcFile, outFile, compr, isLittle);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		// vbox to hold all the contents of this tab
		VBox compressVbox = new VBox();
		compressVbox.getChildren().addAll(
				compressLbl, dirChooserBox,
				compressRBLbl, compressRBBox,
				compressionLbl, compressionSpnr,
				outputFileLbl, outputChooserBox,
				compressBttn);
		VBox.setMargin(compressLbl, headingIns);
		VBox.setMargin(compressRBLbl, headingIns);
		VBox.setMargin(compressionLbl, headingIns);
		VBox.setMargin(outputFileLbl, headingIns);
		VBox.setMargin(compressionSpnr, mainIns);
		VBox.setMargin(compressBttn, mainIns);
		
		// compress tab
		Tab compressTab = new Tab("Compress");
		compressTab.setClosable(false);
		compressTab.setContent(compressVbox);
		
		
		
		/////////////////////////////// EXTRACT TAB ///////////////////////////////
		Label extractLbl = new Label("Source File:");
		extractLbl.setFont(lblFont);
		
		// text field which displays absolute path of selected file
		TextField filePathField = new TextField();
		filePathField.setPrefWidth(350);
		filePathField.setEditable(false);
		
		// select file button
		Button selFileBttn = new Button("Select...");
		selFileBttn.setOnAction(e -> {
			srcFile = fileChooser.showOpenDialog(primaryStage);
			
			if (srcFile != null)
				filePathField.setText(srcFile.toString());
		});
		
		// horizontal box which includes the absolute path text field and the select file button
		HBox fileChooserBox = new HBox();
		fileChooserBox.getChildren().addAll(filePathField, selFileBttn);
		HBox.setMargin(filePathField, mainIns);
		HBox.setMargin(selFileBttn, mainIns);
		
		// output file label
		Label outputDirLbl = new Label("Output Directory: ");
		outputDirLbl.setFont(lblFont);
		
		TextField outputDirField = new TextField();
		outputDirField.setPrefWidth(350);
		outputDirField.setEditable(false);
		
		// output file button
		Button selOutputDirBttn = new Button("Select...");
		selOutputDirBttn.setOnAction(e -> {
			outFile = dirChooser.showDialog(primaryStage);
			
			if (outFile != null)
				outputDirField.setText(outFile.toString());
		});
		
		// horizontal box which includes the absolute path text field and the select file button
		HBox outputDirChooserBox = new HBox();
		outputDirChooserBox.getChildren().addAll(outputDirField, selOutputDirBttn);
		HBox.setMargin(outputDirField, mainIns);
		HBox.setMargin(selOutputDirBttn, mainIns);
		
		// compress button
		Button extractBttn = new Button("Extract");
		// extract file on click
		extractBttn.setOnAction(evt -> {
			try {
				extract(srcFile, outFile);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		// vbox to hold all the contents of this tab
		VBox extractVbox = new VBox();
		extractVbox.getChildren().addAll(
				extractLbl, fileChooserBox,
				outputDirLbl, outputDirChooserBox,
				extractBttn);
		VBox.setMargin(extractLbl, headingIns);
		VBox.setMargin(outputDirLbl, headingIns);
		VBox.setMargin(extractBttn, mainIns);
		
		// extraction tab
		Tab extractTab = new Tab("Extract");
		extractTab.setClosable(false);
		extractTab.setContent(extractVbox);
		
		// main pain which consists of an extract and compress tab
		TabPane main = new TabPane();
		main.getTabs().addAll(compressTab, extractTab);
		
		Scene sc = new Scene(main);
		primaryStage.setScene(sc);
		primaryStage.setTitle(TITLE);
		primaryStage.setWidth(WIDTH);
		primaryStage.setHeight(HEIGHT);
		primaryStage.setResizable(false);
		primaryStage.show();
	}
	
	// extracts a YAZ0 compressed file
	public static void extract(File src, File newOut) throws IOException, InterruptedException {
		String exec = new File("main.py").getAbsolutePath();
		
		File curOut = new File(src.toString().replaceFirst("[.][^.]+$", ""));
		System.out.println("cmd /c python" + " \"" + exec + "\" \"" + src.toString() + "\"");
		Process p = Runtime.getRuntime().exec("cmd /c python" + " \"" + exec + "\" \"" + src.toString() + "\"");
		p.waitFor();
		Files.move(curOut.toPath(), newOut.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	// compresses a directory in YAZ0 format
	public static void compress(File src, File out, int compr, boolean isLittleEndian) throws IOException, InterruptedException {
		ArrayList<String> procList = new ArrayList<String>();
		procList.add("cmd /c python");
		procList.add("\"" + new File("main.py").getAbsolutePath() + "\"");
		if (isLittleEndian)
			procList.add("-little");
		procList.add("-compress " + compr);
		procList.add("-o \"" + out.toString() + "\"");
		procList.add("\"" + src.toString() + "\"");
		
		ProcessBuilder pb = new ProcessBuilder(procList);
		System.out.println(pb.command());
		Process p = pb.start();
		p.waitFor();
	}

}
