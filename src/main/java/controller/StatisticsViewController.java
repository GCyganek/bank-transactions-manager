package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showData() {
        incomeOutcomeChart();
        updateTextFields();
        categoryOutcomeChart();
    }

    private void updateTextFields() {
        fromDateTextField.setText(dateToString(statsManager.getCurrentStartDate()));
        toDateTextField.setText(dateToString(statsManager.getCurrentEndDate()));
    }

    public void incomeOutcomeChart() {

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Income", statsManager.getTotalIncome()));
        series.getData().add(new XYChart.Data<>("Outcome", statsManager.getTotalOutcome()));

        barChart.getData().addAll(series);
    }

    public void categoryOutcomeChart() {

        HashMap<TransactionCategory, BigDecimal> map = statsManager.getOutcomesInCategories();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for(TransactionCategory category: map.keySet()) {
            pieChartData.add(new PieChart.Data(category.toString(), map.get(category).doubleValue()));
        }

        pieChart.setData(pieChartData);

        pieChart.getData().forEach(data -> {
            String percentage = String.format("%.2f%%", (data.getPieValue() / statsManager.getTotalOutcome().doubleValue()));
            Tooltip toolTip = new Tooltip(percentage);
            Tooltip.install(data.getNode(), toolTip);
        });
    }

    private String dateToString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateStringConverter converter = new LocalDateStringConverter(formatter, formatter);
        return converter.toString(date);
    }
}
