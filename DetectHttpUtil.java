import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

class DectectRequest {
    @JSONField(name = "StrBatchNumber")
    private String StrBatchNumber;
    @JSONField(name = "ImgCheckInput")
    private List<ImgCheckInput> ImgCheckInput;

    @JSONField(name = "StrBatchNumber")
    public String getStrBatchNumber() {
        return StrBatchNumber;
    }

    public void setStrBatchNumber(String strBatchNumber) {
        StrBatchNumber = strBatchNumber;
    }

    @JSONField(name = "ImgCheckInput")
    public List<ImgCheckInput> getImgCheckInput() {
        return ImgCheckInput;
    }

    public void setImgCheckInput(List<ImgCheckInput> imgCheckInput) {
        ImgCheckInput = imgCheckInput;
    }
}

class ImgCheckInput {
    @JSONField(name = "ImgGuId")
    private String ImgGuId;
    @JSONField(name = "creimg_base64")
    private String creimg_base64;
    @JSONField(name = "Tab")
    private String Tab;

    @JSONField(name = "ImgGuId")
    public String getImgGuId() {
        return ImgGuId;
    }

    public void setImgGuId(String imgGuId) {
        ImgGuId = imgGuId;
    }

    @JSONField(name = "creimg_base64")
    public String getCreimg_base64() {
        return creimg_base64;
    }

    public void setCreimg_base64(String creimg_base64) {
        this.creimg_base64 = creimg_base64;
    }

    @JSONField(name = "Tab")
    public String getTab() {
        return Tab;
    }

    public void setTab(String tab) {
        Tab = tab;
    }
}

class Result {
    private boolean isSuccess;
    private int responseCode;
    private String reponseText;

    public Result() {
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getReponseText() {
        return reponseText;
    }

    public void setReponseText(String reponseText) {
        this.reponseText = reponseText;
    }
}


public class DetectHttpUtil {

    public static Map<String, String> createHeader(String appid, String appkey, String url) {
        try {
            Map<String, String> headerMap = new HashMap<>();
            JSONObject xServerParam = new JSONObject();
            xServerParam.put("appid", appid);
            String capaname = url.split("/")[url.split("/").length - 1];
            if (capaname.length() <= 24) {
                while (capaname.length() < 24) {
                    capaname+="0";
                }
            } else {
                capaname=capaname.substring(0,24);
            }
            xServerParam.put("csid", appid + capaname + UUID.randomUUID().toString().replaceAll("-", ""));
            final String text = xServerParam.toJSONString();
            final byte[] textByte = text.getBytes("UTF-8");
            final BASE64Encoder encoder = new BASE64Encoder();
            final String encodedText = encoder.encode(textByte).replaceAll("\n", "");
            headerMap.put("X-Server-Param", encodedText);
            String curtime = getUTCTimeStr();
            headerMap.put("X-CurTime", curtime);
            String checkNum = appkey + curtime + encodedText;
            headerMap.put("X-CheckSum", stringToMD5(checkNum));
            return headerMap;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("???????????????????????????????????????" + e.getMessage());
        }
        return null;
    }


    public static Result postForObject(Map<String, String> headerMap, String url, String param) {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        Result result = new Result();
        result.setSuccess(true);
        try {
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-Server-Param", headerMap.get("X-Server-Param"));
            connection.setRequestProperty("X-CurTime", headerMap.get("X-CurTime"));
            connection.setRequestProperty("X-CheckSum", headerMap.get("X-CheckSum"));
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            os = connection.getOutputStream();
            if (param != null) {
                os.write(param.getBytes());
            }
            int responceCode = connection.getResponseCode();
            result.setResponseCode(responceCode);
            if (responceCode != 200) {
                result.setSuccess(false);
            }
            is = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sbf = new StringBuffer();
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sbf.append(temp);
                sbf.append("\r\n");
            }
            result.setReponseText(sbf.toString());
            if (result.getReponseText() == null || result.getReponseText().length() <= 0) {
                result.setSuccess(false);
                result.setReponseText("???????????????????????????????????????");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setReponseText(e.getMessage());
            e.printStackTrace();
        } finally {
            // ????????????
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // ?????????????????????url?????????
            connection.disconnect();
        }
        return result;
    }

    public static String getUTCTimeStr() throws Exception {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("GMT");
        cal.setTimeZone(tz);
        return String.valueOf(cal.getTimeInMillis());// ?????????UTC?????????
    }

    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("????????????md5?????????");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static String img2Base64(String filePath) {
        InputStream in = null;
        byte[] data = null;
        // ????????????????????????
        try {
            in = new FileInputStream(filePath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("?????????base64??????????????????????????????" + e.getMessage());
            return null;
        }
        String base64 = Base64.encodeBase64String(data);
        if (base64 == null) {
            System.out.println("?????????base64??????????????????????????????");
        }
        return base64;

    }

    public static void main(String[] args) {
        //APPID ????????????????????????-???????????? APPKey ????????????????????????-????????????
        //????????????????????????????????????
        String appid = "";
        String appkey = "";
        String url = "http://10.255.77.9:9050/object_detection";
        Map<String, String> requestHeaders = createHeader(appid, appkey,url);
        if (requestHeaders == null) {
            System.out.println("????????????????????????????????????");
            return;
        }
        //????????????,??????????????????????????????
        // ????????????:
        // StrBatchNumber:?????????;
        // ImgCheckInput:???????????????????????????id???base64???????????????????????????
        //Tab??????????????????????????????0:GPS??????/1:????????????/2:???????????????/3:?????????/4:??????/5:?????????/6:??????/7:?????????/8:?????????/9:?????????/10:???????????????;
        // ImgGuId??????????????????id
        //creimg_base64?????????base64?????????????????????
        DectectRequest dectectRequest = new DectectRequest();
        dectectRequest.setStrBatchNumber("test");
        List<ImgCheckInput> imgCheckInputs = new ArrayList<>();
        dectectRequest.setImgCheckInput(imgCheckInputs);
        ImgCheckInput imgCheckInput = new ImgCheckInput();
        imgCheckInputs.add(imgCheckInput);
        imgCheckInput.setTab("1");
        imgCheckInput.setImgGuId("renjintuojia");
        //????????????
        String imgPath = "renjintuojia.jpg";
        imgCheckInput.setCreimg_base64(img2Base64(imgPath));
        if (imgCheckInput.getCreimg_base64() == null) {
            System.out.println("???????????????????????????????????????");
            return;
        }
        String param = JSONObject.toJSONString(dectectRequest);
        //??????????????????
        Result result = postForObject(requestHeaders, url, param);
        if (result.isSuccess()) {
            System.out.println("???????????????????????????" + result.getResponseCode() + ";???????????????" + result.getReponseText());
        } else {
            System.out.println("???????????????????????????" + result.getResponseCode() + ";???????????????" + result.getReponseText());
        }

    }
}
