package playMarketParser.gui.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.CheckComboBox;
import playMarketParser.entities.App;
import playMarketParser.Global;
import playMarketParser.Prefs;
import playMarketParser.gui.customElements.RowNumCellFactory;
import playMarketParser.gui.customElements.TableContextMenu;
import playMarketParser.gui.customElements.TextAreaDialog;
import playMarketParser.modules.appsParser.AppsParser;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static playMarketParser.Global.showAlert;

public class AppsParserController implements Initializable, AppsParser.AppParsingListener {

    @FXML private Button addBtn;
    @FXML private Button importBtn;
    @FXML private Button startBtn;
    @FXML private Button exportBtn;
    @FXML private Button clearBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private Label appsCntLbl;
    @FXML private Label progLbl;
    @FXML private ProgressBar progBar;
    @FXML private TableView<App> table;
    @FXML private TableColumn<App, String> rowNumCol;
    @FXML private TableColumn<App, String> idCol;
    @FXML private TableColumn<App, String> urlCol;
    @FXML private TableColumn<App, String> nameCol;
    @FXML private TableColumn<App, Long> installsCountCol;
    @FXML private TableColumn<App, Integer> ratesCountCol;
    @FXML private TableColumn<App, String> releaseDateCol;
    @FXML private TableColumn<App, String> lastUpdateCol;
    @FXML private TableColumn<App, Double> avgRateCol;
    @FXML private TableColumn<App, String> categoryCol;
    @FXML private TableColumn<App, String> sizeMbCol;
    @FXML private TableColumn<App, Integer> containsAdsCol;
    @FXML private TableColumn<App, Integer> offersPurchasesCol;
    @FXML private TableColumn<App, String> contentCostCol;
    @FXML private TableColumn<App, String> minAgeCol;
    @FXML private TableColumn<App, String> devUrlCol;
    @FXML private TableColumn<App, String> devNameCol;
    @FXML private TableColumn<App, String> devWebSiteCol;
    @FXML private TableColumn<App, String> devEmailCol;
    @FXML private TableColumn<App, String> iconUrlCol;
    @FXML private TableColumn<App, String> descriptionCol;
    @FXML private TableColumn<App, String> verCol;
    @FXML private TableColumn<App, String> whatsNewCol;
    @FXML private TableColumn<App, String> minSdkVerCol;
    @FXML private TableColumn<App, String> simApp1Col;
    @FXML private TableColumn<App, String> simApp2Col;
    @FXML private TableColumn<App, String> simApp3Col;
    @FXML private TableColumn<App, String> simApp4Col;
    @FXML private VBox rootPane;
    @FXML private CheckComboBox<String> columnsCcb;

    private CheckBox titleFirstChb;

    private AppsParser appsParser;
    private MenuItem removeItem;
    private ResourceBundle rb;

    private ObservableList<App> apps = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rb = Global.getBundle();

        //�������
        rowNumCol.setPrefWidth(RowNumCellFactory.WIDTH);
        rowNumCol.setCellFactory(new RowNumCellFactory<>());
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        installsCountCol.setCellValueFactory(new PropertyValueFactory<>("installsCount"));
        ratesCountCol.setCellValueFactory(new PropertyValueFactory<>("ratesCount"));
        releaseDateCol.setCellValueFactory(new PropertyValueFactory<>("releaseDate"));
        lastUpdateCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
        avgRateCol.setCellValueFactory(new PropertyValueFactory<>("avgRate"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        sizeMbCol.setCellValueFactory(new PropertyValueFactory<>("sizeMb"));
        containsAdsCol.setCellValueFactory(new PropertyValueFactory<>("containsAds"));
        offersPurchasesCol.setCellValueFactory(new PropertyValueFactory<>("offersPurchases"));
        contentCostCol.setCellValueFactory(new PropertyValueFactory<>("contentCost"));
        minAgeCol.setCellValueFactory(new PropertyValueFactory<>("minAge"));
        devUrlCol.setCellValueFactory(new PropertyValueFactory<>("devUrl"));
        devNameCol.setCellValueFactory(new PropertyValueFactory<>("devName"));
        devWebSiteCol.setCellValueFactory(new PropertyValueFactory<>("devWebSite"));
        devEmailCol.setCellValueFactory(new PropertyValueFactory<>("devEmail"));
        iconUrlCol.setCellValueFactory(new PropertyValueFactory<>("iconUrl"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        verCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        whatsNewCol.setCellValueFactory(new PropertyValueFactory<>("whatsNew"));
        minSdkVerCol.setCellValueFactory(new PropertyValueFactory<>("minSdkVer"));
        simApp1Col.setCellValueFactory(new PropertyValueFactory<>("simApp1"));
        simApp2Col.setCellValueFactory(new PropertyValueFactory<>("simApp2"));
        simApp3Col.setCellValueFactory(new PropertyValueFactory<>("simApp2"));
        simApp4Col.setCellValueFactory(new PropertyValueFactory<>("simApp2"));
        table.setItems(apps);

        //CheckComboBox ��� ���������� ���������� ������� �������
        columnsCcb.setTitle(rb.getString("columns"));
        for (TableColumn col : table.getColumns()) columnsCcb.getItems().add(col.getText());
        for (TableColumn col : table.getColumns())
            columnsCcb.getItemBooleanProperty(col.getText()).bindBidirectional(col.visibleProperty());

        //Context menus
        TableContextMenu tableContextMenu = new TableContextMenu(table);
        removeItem = tableContextMenu.getRemoveItem();

        //PopOver � ���������
        titleFirstChb = new CheckBox(rb.getString("titleFirst"));
        titleFirstChb.setSelected(Prefs.getBoolean("title_first"));
        Global.addPopOver(importBtn, titleFirstChb);

        //��������
        appsCntLbl.textProperty().bind(Bindings.size(apps).asString());

        //��������� ������ � ���������
        addBtn.setTooltip(new Tooltip(rb.getString("addAppsUrls")));
        importBtn.setTooltip(new Tooltip(rb.getString("importAppsUrls")));
        clearBtn.setTooltip(new Tooltip(rb.getString("clearData")));
        exportBtn.setTooltip(new Tooltip(rb.getString("exportResults")));
        titleFirstChb.setTooltip(new Tooltip(rb.getString("skipFirstTip")));

        enableReadyMode();
    }

    @FXML
    private void addApps() {
        TextAreaDialog dialog = new TextAreaDialog("", rb.getString("enterAppsUrls"), rb.getString("addingAppsUrls"), "");

        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            apps.clear();
            Arrays.stream(((String) result.get()).split("\\r?\\n"))
                    .distinct()
                    .forEachOrdered(s -> apps.add(new App(s)));
            enableReadyMode();
        }
    }

    @FXML
    private void importApps() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("txtDescr"), "*.txt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(rb.getString("csvDescr"), "*.csv"));
        fileChooser.setInitialDirectory(Global.getInitDir("input_path"));
        File inputFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (inputFile == null) return;
        Prefs.put("input_path", inputFile.getParentFile().toString());
        Prefs.put("title_first", titleFirstChb.isSelected());

        enableReadyMode();
        apps.clear();

        try (Stream<String> lines = Files.lines(inputFile.toPath(), StandardCharsets.UTF_8)) {
            lines.skip(titleFirstChb.isSelected() ? 1 : 0)
                    .distinct()
                    .map(App::new)
                    .forEachOrdered(q -> apps.add(q));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("unableToReadFile"), Global.ERROR);
        }
    }

    @FXML
    private void start() {
        if (apps.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noAppsUrls"), Global.ALERT);
            return;
        }
        for (App app : apps) app.reset();
        table.refresh();

        appsParser = new AppsParser(apps, this);
        appsParser.setMaxThreadsCount(Prefs.getInt("apps_threads_cnt"));
        if (!Prefs.getString("parsing_lang").equals("-")) appsParser.setLanguage(Prefs.getString("parsing_lang"));
        if (!Prefs.getString("parsing_country").equals("-")) appsParser.setCountry(Prefs.getString("parsing_country"));

        progBar.setProgress(0);
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", 0f) + "%"));

        enableLoadingMode();
        Global.log(rb.getString("appsStarted") + "\n" +
                String.format("%-30s%s%n", rb.getString("threadsCount"), Prefs.getInt("apps_threads_cnt")) +
                String.format("%-30s%s%n", rb.getString("parsingLang"), Prefs.getString("parsing_lang")) +
                String.format("%-30s%s%n", rb.getString("parsingCountry"), Prefs.getString("parsing_country")) +
                String.format("%-30s%s%n", rb.getString("acceptLang"), Prefs.getString("accept_language")) +
                String.format("%-30s%s%n", rb.getString("timeout"), Prefs.getInt("timeout")) +
                String.format("%-30s%s%n", rb.getString("proxy"), Prefs.getString("proxy")) +
                String.format("%-30s%s%n", rb.getString("userAgent"), Prefs.getString("user_agent"))
        );
        appsParser.start();
    }

    @FXML
    private void pause() {
        appsParser.pause();
        Global.log(rb.getString("appsPaused"));
        enablePauseMode();
    }

    @FXML
    private void resume() {
        appsParser.resume();
        Global.log(rb.getString("appsResumed"));
        enableLoadingMode();
    }

    @FXML
    private void stop() {
        appsParser.stop();
        onFinish();
    }

    @FXML
    private void exportResults() {
        if (apps == null || apps.size() == 0) {
            showAlert(rb.getString("error"), rb.getString("noResults"), Global.ALERT);
            return;
        }

        String curDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis()));

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName(rb.getString("outApps") + " " + curDate);
        fileChooser.setInitialDirectory(Global.getInitDir("output_path"));
        File outputFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (outputFile == null) return;
        if (!outputFile.getParentFile().canWrite()) {
            showAlert(rb.getString("error"), rb.getString("cantWrite"), Global.ERROR);
            return;
        }
        Prefs.put("output_path", outputFile.getParentFile().toString());

        try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile))) {
            //��������� ��������� ����� UTF-8
            ps.write('\ufeef');
            ps.write('\ufebb');
            ps.write('\ufebf');

            String csvDelim = Global.getCsvDelim();
            List<String> newContent = new ArrayList<>();
            //��������� ���������
            StringBuilder firstRow = new StringBuilder();
            for (TableColumn col : table.getColumns()) {
                if (col.isVisible() && !col.getText().equals("#"))
                firstRow.append(col.getText()).append(csvDelim);
            }
            newContent.add(firstRow.toString());
            //����� ������ ����������, ���������� ��� ������ � ������� ������� �������
            for (int r = 0; r < table.getItems().size(); r++) {
                StringBuilder newRow = new StringBuilder();
                for (TableColumn col : table.getColumns())
                    if (col.isVisible() && !col.getText().equals("#")) {
                        Object cellData = col.getCellData(r);
                        String cellString = cellData != null ? cellData.toString() : "";
                        //�������� ���� ������� ����� ������� � CSV
                        if (cellString.contains(csvDelim) || cellString.contains("\"")) {
                            cellString = cellString.replaceAll("\"", "\"\"");
                            cellString = "\"" + cellString + "\"";
                        }
                        newRow.append(cellString).append(csvDelim);
                    }
                newContent.add(newRow.toString());
            }


            Files.write(outputFile.toPath(), newContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            showAlert(rb.getString("saved"), rb.getString("fileSaved"), Global.ACCEPT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("alreadyUsing"), Global.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(rb.getString("error"), rb.getString("fileNotSaved"), Global.ERROR);
        }
    }

    @FXML
    private void clearApps() {
        table.getItems().clear();
        enableReadyMode();
    }

    private void enableReadyMode() {
        addBtn.setDisable(false);
        importBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(true);
        removeItem.setDisable(false);
        Global.setBtnParams(startBtn, true, true);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, false);
    }

    private void enableLoadingMode() {
        addBtn.setDisable(true);
        importBtn.setDisable(true);
        titleFirstChb.setDisable(true);
        clearBtn.setDisable(true);
        exportBtn.setDisable(true);
        removeItem.setDisable(true);
        Global.setBtnParams(startBtn, false, false);
        Global.setBtnParams(pauseBtn, true, true);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, true);
    }

    private void enableCompleteMode() {
        addBtn.setDisable(false);
        importBtn.setDisable(false);
        titleFirstChb.setDisable(false);
        clearBtn.setDisable(false);
        exportBtn.setDisable(false);
        removeItem.setDisable(false);
        Global.setBtnParams(startBtn, true, true);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, false, false);
        Global.setBtnParams(stopBtn, true, false);
    }

    private void enablePauseMode() {
        addBtn.setDisable(true);
        importBtn.setDisable(true);
        titleFirstChb.setDisable(true);
        clearBtn.setDisable(true);
        exportBtn.setDisable(false);
        removeItem.setDisable(true);
        Global.setBtnParams(startBtn, false, false);
        Global.setBtnParams(pauseBtn, false, false);
        Global.setBtnParams(resumeBtn, true, true);
        Global.setBtnParams(stopBtn, true, true);
    }

    @Override
    public void onAppParsed(App app, boolean isSuccess) {
        if (!isSuccess) Global.log(String.format("%-30s%s", app.getId(), rb.getString("connTimeout")));
        table.refresh();
        progBar.setProgress(appsParser.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", appsParser.getProgress() * 100) + "%"));
    }

    @Override
    public void onFinish() {
        enableCompleteMode();
        Global.log(rb.getString("appsComplete"));
        progBar.setProgress(appsParser.getProgress());
        Platform.runLater(() -> progLbl.setText(String.format("%.1f", appsParser.getProgress() * 100) + "%"));
    }
}
