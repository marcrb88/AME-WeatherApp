package com.example.weather;

public class City {
    private String key = "";
    private String type = "";
    private String localizedName = "";
    private String regionId = "";
    private String countryId = "";

    public City(
            String key,
            String type,
            String localizedName,
            String regionId,
            String countryId) {

        this.key = key;
        this.type = type;
        this.localizedName = localizedName;
        this.regionId = regionId;
        this.countryId = countryId;
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
        return  "[ key: " + key +
                ", type: " + type +
                ", localizedName: " + localizedName +
                ", regionId: " + regionId +
                ", countryId: " + countryId +
                 " ]" +"\n\n";
    }
}
