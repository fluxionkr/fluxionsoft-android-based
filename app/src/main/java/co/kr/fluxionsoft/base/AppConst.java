package co.kr.fluxionsoft.base;

public class AppConst {

    /** Log Display TAG */
    public static final String LOG_TAG = "FLUXIONSOFT";

    public static final boolean LOG_FLAG = true;

    /**  TEST_MODE 서버 IP 설정.
     * 	true : 테스트 서버로 접속
     *   false :라이브 서버로 접속
     **/
    public static final boolean TEST_SERVER = true;
//*/
    public static String server_url = TEST_SERVER ? "https://dev.theclozet.co.kr/"
            :"https://www.closetshare.com";

}
