import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

//数据构造、发送请求、处理结果的操作均写在main中
public class java_client {
    public static void main(String[] args) throws JsonProcessingException, UnsupportedEncodingException {

        //获取uuid用于生成csid
        String uuid = UUID.randomUUID().toString();
        //----------下面生成header。appid、appkey、request_url均为使用者提供。这里的request_url的值只是测试用----------------
        String appid = "";
        String appkey = "";
        String request_url = "http://10.128.12.15:5000/v1/request";
        Map<String,String> headerMap = new HashMap<>();
        Map<String,Object> xServerParam = new HashMap<>();
        xServerParam.put("appid", appid);
        String capaname = request_url.split("/")[request_url.split("/").length - 1];
        if (capaname.length() <= 24) {
            while (capaname.length() < 24) {
                capaname+="0";
            }
        } else {
            capaname=capaname.substring(0,24);
        }
        String csid = appid + capaname + UUID.randomUUID().toString().replaceAll("-", "");
        xServerParam.put("csid",csid);
        ObjectMapper xServerParamMapper=new ObjectMapper();
        String xServerParamString = xServerParamMapper.writeValueAsString(xServerParam);
        System.out.println(xServerParamString);

        final byte[] textByte = xServerParamString.getBytes("UTF-8");
        final BASE64Encoder encoder = new BASE64Encoder();
        final String encodedText = encoder.encode(textByte).replaceAll("\n", "");
        headerMap.put("X-Server-Param", encodedText);
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("GMT");
        cal.setTimeZone(tz);
        String curTime = String.valueOf(cal.getTimeInMillis());
        headerMap.put("X-CurTime", curTime);
        String checkNum = appkey + curTime + encodedText;
        headerMap.put("X-CheckSum", stringToMD5(checkNum));
        //-----------------------------至此header构造完成-----------------------------------

        //请求数据明文
        String data = String.format("{\"csid\":\"%s\",\"prediction_data\":{\"amount\": 0,\"s3_flux\": 0,\"s2_flux\":0,\"s1_flux\":0,\"sc_1\":0,\"sc_2\":5," +
                "\"buka_cnt\":0,\"home_city\":591,\"imei_ischang\":0,\"model_desc_ischang\":-5,\"ic_no_num\":20201106101340," +
                "\"imei_num\":591,\"call_called_num\":0,\"rec_cnt\":1,\"all_cnt\":202011060000000,\"brand_id\":0,\"chan_cnt\":0," +
                "\"create_time\":352225}}",csid);
        System.out.println(data);
        //下面将data编码为base64
        String b64_data = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        System.out.println(b64_data);
        //下面的X-Server-Param与header中的X-Server-Param起名重合,二者并没有关系
        String request_body = String.format("{\"X-Server-Param\":\"%s\"}",b64_data);
        try{
            URL url = new URL(request_url);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("X-Server-Param", headerMap.get("X-Server-Param"));
            connection.setRequestProperty("X-CurTime", headerMap.get("X-CurTime"));
            connection.setRequestProperty("X-CheckSum", headerMap.get("X-CheckSum"));
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.connect();

            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());
            //发送请求数据
            out.writeBytes(request_body);
            out.flush();
            out.close();
            //获取响应数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer stringBuffer = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                stringBuffer.append(lines);
            }
            String response = stringBuffer.toString();
            System.out.println(response);
            //下面解析响应的结果。响应结果中的·X-Server-Param与请求数据以及header中的重名，他们三者没有联系
            /*
            * {"code": 1000, "msg": "ok", "X-Server-Param": "b'eydzaWQnOiAnMzc4ODk2NTUnLCAncmVzdWx0JzogW3snUFJPQkFCSUxJVF
            * knOiB7J2FycmF5JzogWzAuMDExNjE2MDkxMiwgMC45ODgzODM5MDg4XSwgJ3ZhbHVlcyc6IFswLjAxMTYxNjA5MTIsIDAuOTg4MzgzOTA4OF
            * 19LCAnUFJFRElDVElPTic6IDEuMCwgJ2Ftb3VudCc6IDAsICdzM19mbHV4JzogMCwgJ3MyX2ZsdXgnOiAwLCAnczFfZmx1eCc6IDAsICdzY18xJzo
            * gMCwgJ3NjXzInOiA1LCAnYnVrYV9jbnQnOiAwLCAnaG9tZV9jaXR5JzogNTkxLCAnaW1laV9pc2NoYW5nJzogMCwgJ21vZGVsX2Rlc2NfaXNjaGFuZyc
            * 6IC01LCAnaWNfbm9fbnVtJzogMjAyMDExMDYxMDEzNDAsICdpbWVpX251bSc6IDU5MSwgJ2NhbGxfY2FsbGVkX251bSc6IDAsICdyZWNfY250JzogMSwg
            * J2FsbF9jbnQnOiAyMDIwMTEwNjAwMDAwMDAsICdicmFuZF9pZCc6IDAsICdjaGFuX2NudCc6IDAsICdjcmVhdGVfdGltZSc6IDM1MjIyNX1dfQ=='", "X
            * -Code": 1000000, "X-Desc": "success", "X-IsSync": true}

            上面的X-Server-Param参数的明文形式如下：
            {'sid': '37889655', 'result': [{'PROBABILITY': {'array': [0.0116160912, 0.9883839088], 'values': [0.0116160912, 0.9883839088]}, 'P
            * REDICTION': 1.0, 'amount': 0, 's3_flux': 0, 's2_flux': 0, 's1_flux': 0, 'sc_1': 0, 'sc_2': 5, 'buka_cnt': 0, 'home_city': 591,
            * 'imei_ischang': 0, 'model_desc_ischang': -5, 'ic_no_num': 20201106101340, 'imei_num': 591, 'call_called_num': 0, 'rec_cnt': 1, '
            * all_cnt': 202011060000000, 'brand_id': 0, 'chan_cnt': 0, 'create_time': 352225}]}



            * */
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode jsonNode2 = jsonNode.get("X-Server-Param");
            String jsonNode3 = jsonNode2.toString();
            String jsonNode4 = jsonNode3.substring(3,jsonNode3.length()-2);
            System.out.println(jsonNode3.substring(3,jsonNode3.length()-2));
            byte[] base64decodedBytes = Base64.getDecoder().decode(jsonNode4.getBytes(StandardCharsets.UTF_8));
            String jsonNode5 = new String(base64decodedBytes, "utf-8").replace("\'","\"");
            System.out.println("原始字符串: " + jsonNode5);
            JsonNode jsonNode6 = objectMapper.readTree(jsonNode5);
            String temp = jsonNode6.get("result").toString();
            String probablity = objectMapper.readTree(temp.substring(1,temp.length()-1)).get("PROBABILITY").get("array").toString();
            System.out.println("预测概率是:"+probablity);
            String result = objectMapper.readTree(temp.substring(1,temp.length()-1)).get("PREDICTION").toString();
            System.out.println("结果是:"+result);
            reader.close();
            // 断开连接
            connection.disconnect();


        }catch (MalformedURLException e){
            e.printStackTrace();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

}
