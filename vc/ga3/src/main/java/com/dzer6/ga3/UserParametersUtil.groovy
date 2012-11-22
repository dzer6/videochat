package com.dzer6.ga3

class UserParametersUtil {
  
    static def lifePeriodMap = ["0": UserParametersConstants.LIFE_PERIOD_UNKNOWN, 
                                "1": UserParametersConstants.LIFE_PERIOD_1, 
                                "2": UserParametersConstants.LIFE_PERIOD_2, 
                                "3": UserParametersConstants.LIFE_PERIOD_3, 
                                "4": UserParametersConstants.LIFE_PERIOD_4, 
                                "5": UserParametersConstants.LIFE_PERIOD_5,
                                "6": UserParametersConstants.LIFE_PERIOD_6]
  
    static def sexMap = ["0": UserParametersConstants.SEX_UNKNOWN, 
                         "1": UserParametersConstants.SEX_1, 
                         "2": UserParametersConstants.SEX_2, 
                         "3": UserParametersConstants.SEX_3, 
                         "4": UserParametersConstants.SEX_4, 
                         "5": UserParametersConstants.SEX_5]
  
    static def lifePeriodUIMap = [:]

    static def sexUIMap = [:]
  
    public static String convertLifePeriodUIToModel(String value) {
        return lifePeriodMap[value]
    }
  
    public static String convertSexUIToModel(String value) {
        return sexMap[value]
    }
  
    public static String convertLifePeriodModelToUI(String value) {
        if (lifePeriodUIMap.size() == 0) {
            lifePeriodMap.each { k, v ->
                lifePeriodUIMap[v] = k;
            }
        }
        return lifePeriodUIMap[value]
    }
  
    public static String convertSexModelToUI(String value) {
        if (sexUIMap.size() == 0) {
            sexMap.each { k, v ->
                sexUIMap[v] = k;
            }
        }
        return sexUIMap[value]
    }
  
}
