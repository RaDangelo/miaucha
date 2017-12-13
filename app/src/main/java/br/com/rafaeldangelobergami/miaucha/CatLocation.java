package br.com.rafaeldangelobergami.miaucha;

public class CatLocation {

    private Double latitude;
    private Double longitude;

    public CatLocation(Double lat, Double lng) {
        this.setLatitude(lat);
        this.setLongitude(lng);
    }

    public CatLocation() {

    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
