package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
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
    public TextField fromDateTextField;

    @FXML
    public TextField toDateTextField;

    @FXML
    public Button applyButton;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showData() {
        updateTextFields();
        incomeOutcomeChart();
        categoryOutcomeChart();
    }

    private void updateTextFields() {
        fromDateTextField.setText(dateToString(statsManager.getCurrentStartDate().orElse(DEFAULT_START_DATE)));
        toDateTextField.setText(dateToString(statsManager.getCurrentEndDate().orElse(DEFAULT_END_DATE)));
    }

    public void handleApplyButton(ActionEvent actionEvent) {
        incomeOutcomeChart();
    }

    public void incomeOutcomeChart() {
        LocalDate fromDate = dateFromString(fromDateTextField.getText()).minusDays(1);
        LocalDate toDate = dateFromString(toDateTextField.getText()).plusDays(1);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Income", statsManager.getIncome(fromDate, toDate)));
        series.getData().add(new XYChart.Data<>("Outcome", statsManager.getOutcome(fromDate, toDate)));

        barChart.getData().clear();
        barChart.getData().add(series);
    }

    public void categoryOutcomeChart() {
        System.out.println(statsManager.getTotalOutcome().doubleValue());
        HashMap<TransactionCategory, BigDecimal> map = statsManager.getOutcomesInCategories();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for(TransactionCategory category: map.keySet()) {
            pieChartData.add(new PieChart.Data(category.toString(), map.get(category).doubleValue()));
        }

        pieChart.setData(pieChartData);

        double totalOutcome =  statsManager.getTotalOutcome().doubleValue();
        pieChart.getData().forEach(data -> {
            String percentage = String.format("%.2f%%", (data.getPieValue() / totalOutcome));
            Tooltip toolTip = new Tooltip(percentage);
            Tooltip.install(data.getNode(), toolTip);
        });
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
