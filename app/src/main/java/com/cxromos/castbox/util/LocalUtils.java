package com.cxromos.castbox.util;


import android.text.Html;

public class LocalUtils {
    public static String getCountryCodeFromCountry(String country) {
        switch (country) {
            case "United States":
                return "us";
            case "United Kingdom":
                return "gb";
            case "Spain":
                return "es";
            case "France":
                return "fr";
            case "Germany":
                return "de";
            case "Portugal":
                return "po";
            case "Canada":
                return "ca";
            case "Australia":
                return "au";
            case "China":
                return "cn";
            default:
                return "us";
        }
    }

    public static String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }
}
