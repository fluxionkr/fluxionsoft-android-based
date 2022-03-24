package co.kr.fluxionsoft.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ydse on 2017-12-19.
 */

public class DateUtil {
    /**
     * 지정된 형식으로 전달된 time에서 날짜 문자열을 반환한다.
     * @param time 날짜로 변환할 밀리세컨드
     * @param format ex : "yyyyMMddHHmmss"
     * @return 날짜 문자열
     */
    public static String getDateString(long time, String format) {
        Date date = new Date(time);
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        return fmt.format(date);
    }

    /**
     * 지정된 형식으로 전달된 문자열을 날짜 문자열로 반환한다.
     * @param timeString	날짜를 나타내는 문자열
     * @param format ex : "yyyyMMddHHmmss"
     * @return 날짜 문자열
     */
    public static String getDateString(String timeString, String format) {
        String newstring = "";
        try{
            Date date = new SimpleDateFormat("yyyyMMdd").parse(timeString);

            newstring = new SimpleDateFormat(format).format(date);
        }catch(Exception e){
            e.printStackTrace();
        }

        return newstring;
    }
}
