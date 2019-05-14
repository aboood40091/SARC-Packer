import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
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
	
	private static File srcFile = null,
					   outFile = null;

	private static Label popupLbl = null;
	private static TextArea cliOutput = null;
	private static Tab popupTab = null;
	private static TabPane main;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("YAZ0 Compressed File", "*.szs", "*.sarc"));
		
		DirectoryChooser dirChooser = new DirectoryChooser();
		
		//popup.setAlignment(Pos.CENTER);
		popupLbl = new Label("Finished");
		popupLbl.setFont(lblFont);
		popupLbl.setAlignment(Pos.BASELINE_CENTER);
		
		cliOutput = new TextArea();
		cliOutput.setEditable(false);
		cliOutput.setFont(new Font("Consolas", 12));
		cliOutput.setMinSize(WIDTH -16, HEIGHT -105);
		
		GridPane popup = new GridPane();
		popup.setPadding(mainIns);
		popup.setVgap(5);
		popup.add(popupLbl, 1, 1);
		popup.add(cliOutput, 1, 2);
		
		popupTab = new Tab("Finished");
		popupTab.setClosable(false);
		popupTab.setContent(popup);
		
		
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
			
			compress(srcFile, outFile, compr, isLittle);
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
		extractBttn.setOnAction(evt -> extract(srcFile, outFile));
		
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
		main = new TabPane();
		main.getTabs().addAll(compressTab, extractTab, popupTab);
		
		Scene sc = new Scene(main);
		primaryStage.setScene(sc);
		primaryStage.setTitle(TITLE);
		primaryStage.setWidth(WIDTH);
		primaryStage.setHeight(HEIGHT);
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(e -> System.exit(0));
		primaryStage.show();
	}
	
	// for some reason the p.waitFor() in extract doesn't work, so we need to continue trying to move
	// this directory until it lets us.
	public static void move(File oldf, File newf) {
		try {
			Runtime.getRuntime().exec("cmd.exe /C move \"" + oldf.toString() + "\" \"" + newf.toString() + "\"");
		} catch (IOException e) {
			e.printStackTrace();
			try {
				Thread.sleep(250); // wait 0.25 seconds before trying again
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			move(oldf, newf);
		}
	}
	
	// extracts a YAZ0 compressed file
	public static void extract(File src, File newOut) {
		new Thread(() -> {
			Platform.runLater(() -> {
				popupTab.setText("Working...");
				popupLbl.setText("Working...");
				cliOutput.setText("");
				main.getSelectionModel().select(popupTab);
			});
			
			StringBuilder sb = new StringBuilder();
			
			String exec = new File("main.py").getAbsolutePath();
			File curOut = new File(src.toString().replaceFirst("[.][^.]+$", ""));
			
			ArrayList<String> procList = new ArrayList<String>();
			procList.add("cmd.exe");
			procList.add("/C");
			procList.add(".\\main.py");
			procList.add("\"" + src.toString() + "\"");
			
			ProcessBuilder pb = new ProcessBuilder(procList);
			Process p = null;
			try {
				p = pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				 // reads the output stream, so it doesn't fill up and cause p.waitFor() to fail
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null)
					sb.append(line + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// wait for previous process to finish
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			move(curOut, newOut);

			Platform.runLater(() -> {
				popupTab.setText("Finished");
				popupLbl.setText("Finished");
				cliOutput.setText(sb.toString());
			});
		}).start();
	}
	
	// compresses a directory in YAZ0 format
	public static void compress(File src, File out, int compr, boolean isLittleEndian) {
		new Thread(() -> {
			Platform.runLater(() -> {
				popupTab.setText("Working...");
				popupLbl.setText("Working...");
				cliOutput.setText("");
				main.getSelectionModel().select(popupTab);
			});

			StringBuilder sb = new StringBuilder();
			
			ArrayList<String> procList = new ArrayList<String>();
			procList.add("cmd.exe");
			procList.add("/C");
			procList.add(".\\main.py");
			if (isLittleEndian)
				procList.add("-little");
			procList.add("-compress");
			procList.add(compr + "");
			procList.add("-o \"" + out.toString() + "\"");
			procList.add("\"" + src.toString() + "\"");
			
			ProcessBuilder pb = new ProcessBuilder(procList);
			Process p = null;
			try {
				p = pb.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				 // reads the output stream, so it doesn't fill up and cause p.waitFor() to fail
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null)
					sb.append(line + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Platform.runLater(() -> {
				popupTab.setText("Finished");
				popupLbl.setText("Finished");
				cliOutput.setText(sb.toString());
			});
		}).start();
	}

}
