package yyd.com.myclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

/**
 * Http请求发送工具，针对协议服务端的请求接收特别实现，如果协议服务端的请求接收有所不同需要不同实现。
 */
public final class ClientUtil {
	private static final boolean DEBUG = true;
	public static Document post(String url, String context) {

		URL link = null;
		Writer writer = null;
		BufferedReader reader = null;

		try {
			link = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) link.openConnection();
			connection.setRequestMethod("POST");// 提交模式
			// conn.setConnectTimeout(10000);//连接超时 单位毫秒
			// conn.setReadTimeout(2000);//读取超时 单位毫秒
			// 发送POST请求必须设置如下两行
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Pragma:", "no-cache");
			connection.setRequestProperty("Cache-Conctol", "no-cache");
			connection.setRequestProperty("Content-Type", "text/xml");
			writer = new PrintWriter(connection.getOutputStream());
			writer.write(context);
			writer.flush();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "gbk"));
			SAXReader readerxml = new SAXReader();
			Document doc = readerxml.read(reader);
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				return DocumentHelper.parseText(errorXml(e));
			} catch (DocumentException e1) {
				e1.printStackTrace();
				return null;
			}
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String errorXml(Throwable t) {
		StringBuilder s = new StringBuilder("<?xml version=\"1.0\" encoding=\"GBK\"?>");
		s.append("<GNNT>").append("<REP name=\"EXCEPTION\">").append("<RESULT>").append("<RETCODE>-500</RETCODE>");
		if(DEBUG) {
			s.append("<MESSAGE>").append(t.getMessage()).append("</MESSAGE>");
		} else {
			s.append("<MESSAGE>MarketClient协议客户端出现错误</MESSAGE>");
		}
		s.append("</RESULT>").append("</REP>")
				.append("</GNNT>");
		return s.toString();
	}


}
