package com.example.weather;

public class City {
    private String key = "";
    private String type = "";
    private String localizedName = "";
    private String regionId = "";
    private String countryId = "";
    private String latitude = "";
    private String longitude = "";

    public City(
            String key,
            String type,
            String localizedName,
            String regionId,
            String countryId,
            String latitude,
            String longitude) {

        this.key = key;
        this.type = type;
        this.localizedName = localizedName;
        this.regionId = regionId;
        this.countryId = countryId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getCountryId() {
        return countryId;
    }


    @Override
    public String toString() {
        return "Weather of " + localizedName + " located in: " + countryId + ":" + "\n" +
                " key: " + key +
                ", type: " + type +
                ", region: " + regionId +
                ", latitude: " + latitude +
                ", longitude: " + longitude +
                "\n\n";
    }
}
