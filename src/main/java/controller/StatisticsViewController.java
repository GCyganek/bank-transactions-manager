package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.LocalDateStringConverter;
import model.TransactionStatsManager;
import model.util.TransactionCategory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class StatisticsViewController {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final LocalDate DEFAULT_START_DATE = LocalDate.of(2000,1,1);
    private static final LocalDate DEFAULT_END_DATE = LocalDate.of(2000,1,1);
    private static final String INCOME = "Income";
    private static final String OUTCOME = "Outcome";

    private final TransactionStatsManager statsManager;

    private Stage stage;

    @Inject
    public StatisticsViewController(TransactionStatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @FXML
    public BarChart<String, Number> barChart;

    @FXML
    public PieChart pieChart;

    @FXML
    public TextField barChartFromDateTextField;

    @FXML
    public TextField barChartToDateTextField;

    @FXML
    public Button barChartApplyButton;

    @FXML
    public TextField pieChartFromDateTextField;

    @FXML
    public TextField pieChartToDateTextField;

    @FXML
    public Button pieChartApplyButton;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showData() {
        updateTextFields();
        incomeOutcomeChart();
        categoryOutcomeChart();
    }

    private void updateTextFields() {
        barChartFromDateTextField.setText(dateToString(statsManager.getCurrentStartDate().orElse(DEFAULT_START_DATE)));
        pieChartFromDateTextField.setText(dateToString(statsManager.getCurrentStartDate().orElse(DEFAULT_START_DATE)));
        barChartToDateTextField.setText(dateToString(statsManager.getCurrentEndDate().orElse(DEFAULT_END_DATE)));
        pieChartToDateTextField.setText(dateToString(statsManager.getCurrentEndDate().orElse(DEFAULT_END_DATE)));
    }

    public void handleBarChartApplyButton(ActionEvent actionEvent) {
        incomeOutcomeChart();
    }

    public void handlePieChartApplyButton(ActionEvent actionEvent) { categoryOutcomeChart(); }

    public void incomeOutcomeChart() {
        LocalDate fromDate = dateFromString(barChartFromDateTextField.getText()).minusDays(1);
        LocalDate toDate = dateFromString(barChartToDateTextField.getText()).plusDays(1);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(INCOME, statsManager.getIncome(fromDate, toDate)));
        series.getData().add(new XYChart.Data<>(OUTCOME, statsManager.getOutcome(fromDate, toDate)));

        barChart.getData().clear();
        barChart.getData().add(series);
    }

    public void categoryOutcomeChart() {
        LocalDate fromDate = dateFromString(pieChartFromDateTextField.getText()).minusDays(1);
        LocalDate toDate = dateFromString(pieChartToDateTextField.getText()).plusDays(1);

        HashMap<TransactionCategory, BigDecimal> outcomesInCategories =
                statsManager.getOutcomesInCategories(fromDate, toDate);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for(TransactionCategory category: outcomesInCategories.keySet()) {
            pieChartData.add(new PieChart.Data(category.toString(), outcomesInCategories.get(category).doubleValue()));
        }

        pieChart.setData(pieChartData);

        double totalOutcome =  statsManager.getOutcome(fromDate, toDate).doubleValue();
        pieChart.getData().forEach(data -> installToolTip(data, totalOutcome));
    }

    private void installToolTip(PieChart.Data data, double totalOutcome) {
        String percentage = String.format("Percentage of the total: %.2f%%", (data.getPieValue() / totalOutcome) * 100);
        String amount = String.format("Amount: %.2f", data.getPieValue());
        Tooltip toolTip = new Tooltip(amount + "\n" + percentage);
        toolTip.setShowDelay(Duration.millis(150));
        Tooltip.install(data.getNode(), toolTip);
    }

    private String dateToString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateStringConverter converter = new LocalDateStringConverter(formatter, formatter);
        return converter.toString(date);
    }

    private LocalDate dateFromString(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateStringConverter converter = new LocalDateStringConverter(formatter, formatter);
        return converter.fromString(date);
    }
}
