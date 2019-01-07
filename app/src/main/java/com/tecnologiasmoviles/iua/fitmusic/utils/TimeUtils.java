package com.tecnologiasmoviles.iua.fitmusic.utils;

import java.util.ArrayList;
import java.util.List;

public class TimeUtils {

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String minutesString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000*60*60));
        int minutes = (int) (milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);

        // Add hours if there
        if (hours > 0) {
            // Prepending 0 to hours if it is one digit
            if(hours < 10){
                finalTimerString = "0" + hours + "h";
            } else {
                finalTimerString = "" + hours + "h";
            }
        }

        // Prepending 0 to minutes if it is one digit
        if(minutes < 10){
            minutesString = "0" + minutes + "'";
        } else {
            minutesString = "" + minutes + "'";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds + "''";
        } else {
            secondsString = "" + seconds + "''";
        }

        finalTimerString = finalTimerString + minutesString + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = 0d;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static String normalizeDuration(String raceDuration) {
        String durationNormalized = "";

        if (raceDuration.contains("h")) {
            durationNormalized = raceDuration.replace("h", ":");

            if (raceDuration.contains("''")) {
                durationNormalized = durationNormalized.replace("''", "");
            }

            if (raceDuration.contains("'")) {
                durationNormalized = durationNormalized.replace("'", ":");
            }
        } else {
            if (raceDuration.contains("''")) {
                durationNormalized = raceDuration.replace("''", "");
            }

            if (raceDuration.contains("'")) {
                durationNormalized = durationNormalized.replace("'", ":");
            }
        }

        return durationNormalized;
    }

    public static String parseDurationAsSentence(String raceDuration) {
        List<String> durationSplitted = new ArrayList<>();

        if (raceDuration.contains("h")) {
            durationSplitted.add(raceDuration.substring(0,3));
            durationSplitted.add(raceDuration.substring(3,6));
            durationSplitted.add(raceDuration.substring(6));
        } else {
            durationSplitted.add(raceDuration.substring(0,3));
            durationSplitted.add(raceDuration.substring(3));
        }

        String hours = "";
        String minutes = "";
        String seconds = "";

        if (durationSplitted.size() == 3) { // duration is at least is more than one hour
            if (durationSplitted.get(0).charAt(0) == '0') {
                if (durationSplitted.get(0).charAt(1) == '1') {
                    hours = durationSplitted.get(0).charAt(1) + " hour";
                } else {
                    hours = durationSplitted.get(0).charAt(1) + " hours";
                }
            } else {
                hours = durationSplitted.get(0).substring(0,2) + " hours";
            }

            if (!durationSplitted.get(1).substring(0,2).equals("00")) { // duration have minutes
                if (!durationSplitted.get(2).substring(0,2).equals("00")) { // duration have seconds
                    if (durationSplitted.get(1).charAt(0) == '0') {
                        if (durationSplitted.get(1).charAt(1) == '1') {
                            minutes = ", " + durationSplitted.get(1).charAt(1) + " minute";
                        } else {
                            minutes = ", " + durationSplitted.get(1).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = ", " + durationSplitted.get(1).substring(0,2) + " minutes";
                    }

                    if (durationSplitted.get(2).charAt(0) == '0') {
                        if (durationSplitted.get(2).charAt(1) == '1') {
                            seconds = " and " + durationSplitted.get(2).charAt(1) + " second";
                        } else {
                            seconds = " and " + durationSplitted.get(2).charAt(1) + " seconds";
                        }
                    } else {
                        seconds = " and " + durationSplitted.get(2).substring(0,2) + " seconds";
                    }
                } else {
                    if (durationSplitted.get(1).charAt(0) == '0') {
                        if (durationSplitted.get(1).charAt(1) == '1') {
                            minutes = " and " + durationSplitted.get(1).charAt(1) + " minute";
                        } else {
                            minutes = " and " + durationSplitted.get(1).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = " and " + durationSplitted.get(1).substring(0,2) + " minutes";
                    }
                }
            } else {
                if (durationSplitted.get(2).charAt(0) == '0') {
                    if (durationSplitted.get(2).charAt(1) == '1') {
                        seconds = " and " + durationSplitted.get(2).charAt(1) + " second";
                    } else {
                        seconds = " and " + durationSplitted.get(2).charAt(1) + " seconds";
                    }
                } else {
                    seconds = " and " + durationSplitted.get(2).substring(0,2) + " seconds";
                }
            }
        } else {
            if (!durationSplitted.get(0).substring(0,2).equals("00")) { // duration have minutes
                if (!durationSplitted.get(1).substring(0,2).equals("00")) { // duration have seconds
                    if (durationSplitted.get(0).charAt(0) == '0') {
                        if (durationSplitted.get(0).charAt(1) == '1') {
                            minutes = durationSplitted.get(0).charAt(1) + " minute";
                        } else {
                            minutes = durationSplitted.get(0).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = durationSplitted.get(0).substring(0,2) + " minutes";
                    }

                    if (durationSplitted.get(1).charAt(0) == '0') {
                        if (durationSplitted.get(1).charAt(1) == '1') {
                            seconds = " and " + durationSplitted.get(1).charAt(1) + " second";
                        } else {
                            seconds = " and " + durationSplitted.get(1).charAt(1) + " seconds";
                        }
                    } else {
                        seconds = " and " + durationSplitted.get(1).substring(0,2) + " seconds";
                    }
                } else {
                    if (durationSplitted.get(0).charAt(0) == '0') {
                        if (durationSplitted.get(0).charAt(1) == '1') {
                            minutes = durationSplitted.get(0).charAt(1) + " minute";
                        } else {
                            minutes = durationSplitted.get(0).charAt(1) + " minutes";
                        }
                    } else {
                        minutes = durationSplitted.get(0).substring(0,2) + " minutes";
                    }
                }
            } else {
                if (durationSplitted.get(1).charAt(0) == '0') {
                    if (durationSplitted.get(1).charAt(1) == '1') {
                        seconds = durationSplitted.get(1).charAt(1) + " second";
                    } else {
                        seconds = durationSplitted.get(1).charAt(1) + " seconds";
                    }
                } else {
                    seconds = durationSplitted.get(1).substring(0,2) + " seconds";
                }
            }
        }

        return hours + minutes + seconds;
    }

}