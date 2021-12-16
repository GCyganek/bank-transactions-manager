package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.stage.Stage;

public class StatisticsViewController {

    private Stage stage;

    @FXML
    public BarChart<String, Number> barChart;

    @FXML
    public PieChart pieChart;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showData() {
        incomeOutcomeChart();
        categoryIncomeChart();
    }

    public void incomeOutcomeChart() {

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Income", 2000));
        series.getData().add(new XYChart.Data<>("Outcome", 1000));

        barChart.getData().addAll(series);
    }

    public void categoryIncomeChart() {

        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Health and beauty", 30),
                        new PieChart.Data("Food", 50),
                        new PieChart.Data("Clothes and shoes", 10));

        pieChart.setData(pieChartData);
    }
}
