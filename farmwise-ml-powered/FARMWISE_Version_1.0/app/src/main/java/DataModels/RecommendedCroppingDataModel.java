package DataModels;

public class RecommendedCroppingDataModel {
    int plant_image;
    String plant_name, planting_week, planting_date, harvesting_week, harvesting_date;

    public RecommendedCroppingDataModel(int plant_image, String plant_name, String planting_week, String planting_date, String harvesting_week, String harvesting_date) {
        this.plant_image = plant_image;
        this.plant_name = plant_name;
        this.planting_week = planting_week;
        this.planting_date = planting_date;
        this.harvesting_week = harvesting_week;
        this.harvesting_date = harvesting_date;
    }

    public int getPlant_image() {
        return plant_image;
    }

    public void setPlant_image(int plant_image) {
        this.plant_image = plant_image;
    }

    public String getPlant_name() {
        return plant_name;
    }

    public void setPlant_name(String plant_name) {
        this.plant_name = plant_name;
    }

    public String getPlanting_week() {
        return planting_week;
    }

    public void setPlanting_week(String planting_week) {
        this.planting_week = planting_week;
    }

    public String getPlanting_date() {
        return planting_date;
    }

    public void setPlanting_date(String planting_date) {
        this.planting_date = planting_date;
    }

    public String getHarvesting_week() {
        return harvesting_week;
    }

    public void setHarvesting_week(String harvesting_week) {
        this.harvesting_week = harvesting_week;
    }

    public String getHarvesting_date() {
        return harvesting_date;
    }

    public void setHarvesting_date(String harvesting_date) {
        this.harvesting_date = harvesting_date;
    }
}
