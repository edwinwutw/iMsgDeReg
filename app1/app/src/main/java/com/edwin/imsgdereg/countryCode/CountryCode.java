package com.edwin.imsgdereg.countryCode;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by edwinwu on 2018/3/12.
 */

public class CountryCode  implements Comparable<String> {
    // isoCode and countryCode map
    public static final Map<String, Integer> COUNTRYCODE_MAP = new HashMap<String, Integer>() {{
        put("ar", 54);  //Argentina
        put("au", 61);  //Australia
        put("at", 43);  //Austria
        put("be", 32);  //Belgium
        put("bo", 591); //Bolivia
        put("br", 55);  //Brazil
        put("ca", 1);   //Canada
        put("cl", 56);  //Chile
        put("cn", 86);  //China
        put("co", 57);  //Colombia
        put("cz", 420); //Czechia
        put("dk", 45);  //Denmark
        put("do", 1);   //Dominican Republic
        put("ec", 593); //Ecuador
        put("sv", 503); //El Salvador
        put("FI", 358); //Finland
        put("fr", 33);  //France
        put("de", 49);  //Germany
        put("gr", 30);  //Greece
        put("gu", 1);   //Guam
        put("gt", 502); //Guatemala
        put("hn", 504); //Honduras
        put("hk", 852); //Hong Kong
        put("hu", 36);  //Hungary
        put("is", 354); //Iceland
        put("in", 91);  //India
        put("id", 62);  //Indonesia
        put("ie", 353); //Ireland
        put("il", 972); //Israel
        put("it", 39);  //Italy
        put("jp", 81);  //Japan
        put("kz", 7);   //Kazakhstan
        put("lt", 370); //Lithuania
        put("lu", 352); //Luxembourg
        put("mo", 853); //Macau
        put("my", 60);  //Malaysia
        put("mx", 52);  //Mexico
        put("nl", 31);  //Netherlands
        put("nz", 64);  //New Zealand
        put("ni", 505); //Nicaragua
        put("no", 47);  //Norway
        put("pa", 507); //Panama
        put("py", 595); //Paraguay
        put("pe", 51);  //Peru
        put("ph", 63);  //Philippines
        put("pl", 48);  //Poland
        put("pt", 351); //Portugal
        put("pr", 1);   //Puerto Rico
        put("ro", 40);  //Romania
        put("ru", 7);   //Russia
        put("sa", 966); //Saudi Arabia
        put("sg", 65);  //Singapore
        put("za", 27);  //South Africa
        put("kr", 82);  //South Korea
        put("es", 34);  //Spain
        put("se", 46);  //Sweden
        put("ch", 41);  //Switzerland
        put("tw", 886); //Taiwan
        put("th", 66);  //Thailand
        put("tr", 90);  //Turkey
        put("ua", 380); //Ukraine
        put("ae", 971); //United Arab Emirates
        put("gb", 44);  //United Kingdom
        put("us", 1);   //United States
        put("uy", 598); //Uruguay
        put("ve", 58);  //Venezuela
        put("vn", 84);  //Vietnam
    }};

    public int countryCode;
    public String isoCode;
    public String countryName;

    public CountryCode(int countryCode, String isoCode) {
        this.countryCode = countryCode;
        this.isoCode = isoCode;
        this.countryName = getCountryName(isoCode, Locale.getDefault());
    }

    private String getCountryName(String countryCode, Locale language) {
        return (countryCode == null || countryCode.equals("ZZ") ||
                countryCode.equals(PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY))
                ? "" : new Locale("", countryCode).getDisplayCountry(language);
    }

    @Override
    public int compareTo(String another) {
        return isoCode != null && another != null ? isoCode.compareTo(another) : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o != null && o instanceof CountryCode) {
            CountryCode other = (CountryCode) o;

            return isoCode != null &&
                    isoCode.equals(other.isoCode);
        }

        return false;
    }

    @Override
    public String toString() {
        return isoCode;
    }
}
