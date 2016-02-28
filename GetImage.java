import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;

import com.htjc.cht.api.util.HtjcSymmetryCodeUtil;

public class GetImage {

	/**
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	static String file_id = null;
	static String temp_path = null;
	static String password = null;
	static Integer pwd = null;
	static String file_path = null;
	/*-------------------------------*/
	static String dbpass = null;
	static String dbuser = null;
	static String dbname = null;
	static String dbport = null;
	static String dbip = null;
	/*-------------------------------*/
	static int connect_timeout = 0;
	static int network_timeout = 0;
	static String base_path = null;
	static String tracker_server = null;
	static boolean setG_anti_steal_token = false;
	static String setG_charset = null;
	static int setG_tracker_http_port = 0;

	/*-------------------------------*/
	static FastdfsFile file = null;
	static Map<?, ?> map = null;

	static {
		System.out.println("初始化数据中,读取配置文件中..");
		map = read(new File("conf.properties"));
		System.out.println("配置内容如下");
		for (Entry<?, ?> e : map.entrySet()) {
			System.out.println(e.getKey() + "---->" + e.getValue());
		}
		dbip = (String) map.get("dbip");
		dbport = (String) map.get("dbport");
		dbname = (String) map.get("dbname");
		dbuser = (String) map.get("dbuser");
		dbpass = (String) map.get("dbpass");

		// 201601220000003529
		temp_path = (String) map.get("temp_path");

		network_timeout = Integer.valueOf((String) map.get("network_timeout"));
		base_path = (String) map.get("base_path");
		tracker_server = (String) map.get("tracker_server");
		setG_anti_steal_token = Boolean.valueOf((String) map
				.get("setG_anti_steal_token"));
		setG_charset = (String) map.get("setG_charset");
		setG_tracker_http_port = Integer.valueOf((String) map
				.get("setG_tracker_http_port"));
	}

	public static void main(String[] args) {

		File temp_fp = new File(temp_path);
		if (!temp_fp.exists()) {
			temp_fp.mkdir();
		}
		System.out.println("输入文件id");
		file_id = getInputStr();
		String sql = "select p.file_Id,p.file_Path,p.password from Pub_Upload_Files p where file_Id='"
				+ file_id + "'";
		Connection conn = null;
		Statement stm = null;
		ResultSet rs = null;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// 建立到数据库的连接"jdbc:oracle:thin:@"+dbip+":"+dbport+":"+dbname+""
			System.out.println("jdbc:oracle:thin:@" + dbip + ":" + dbport + ":"
					+ dbname + "");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + dbip
					+ ":" + dbport + ":" + dbname + "", dbuser, dbpass);
			if (conn != null) {
				System.out.println("连接数据库成功");
			}else{
				System.out.println("数据库连接信息不对..");
				System.exit(-1);
			}
			stm = conn.createStatement();
			rs = stm.executeQuery(sql);

			if (rs.next()) {
				file_id = rs.getString("file_id");
				file_path = rs.getString("file_path");
				password = rs.getString("password");

				try {
					pwd = Integer.valueOf(HtjcSymmetryCodeUtil
							.decrypt(password));
				} catch (NumberFormatException e) {
					System.out.println("解密转换异常, 确保加解密服务正常");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("根据文件id没有查询到数据");
			}

			System.out.println("文件在fastDFS中的位置:" + file_path);
			file = downloadFile(file_path);
			if (file != null) {
				System.out.println("图片存在,并且找到压缩文件");
			}
		} catch (ClassNotFoundException e) {
			System.out.println("驱动没有找到");
		} catch (SQLException e) {
			System.out.println("连接数据库出问题或执行sql失败");
			System.exit(-1);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("分布式系统取文件失败");
		} finally {

			try {
				if (stm != null) {
					stm.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out.println("关闭数据库出错");
			}
		}

		byte[] bytes = null;
		OutputStream os = null;
		File f2 = null;
		String tf = null;
		if (file != null) {
			bytes = file.getFile();

			tf = temp_path + "temp." + file.getExtFileName();
			f2 = new File(tf);
			if (!f2.exists()) {
				try {
					f2.createNewFile();
				} catch (IOException e) {
					System.out.println("创建文件失败..");
					e.printStackTrace();
				}
			}
		} else {

			System.out.println("文件不存在..");
			System.exit(-1);
		}
		try {
			os = new FileOutputStream(f2);
			os.write(bytes);
			System.out.println("写文件到" + temp_path + "目录下成功");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("写文件失败");
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// String filePathNew=temp_path+"zip";

		ZipFile zipFile;
		try {
			zipFile = new ZipFile(tf);
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(String.valueOf(pwd));
			}
			zipFile.extractAll(temp_path);
			System.out.println("解压文件成功到" + temp_path);
			if (f2.delete()) {
				System.out.println("删除临时文件 " + f2.getAbsolutePath());
			}
		} catch (ZipException e) {
			System.out.println("解压文件失败..");
			e.printStackTrace();
		}

		System.out.println("图片下载程序结束.");

	}

	private static Map<String, String> read(File file) {

		InputStream is = null;

		Properties pro = new Properties();

		Map<String, String> map1 = new HashMap<String, String>();

		try {

			is = new BufferedInputStream(new FileInputStream(file));

			pro.load(is);

			Enumeration<?> enum1 = pro.propertyNames();

			while (enum1.hasMoreElements()) {

				String strKey = (String) enum1.nextElement();

				String strValue = pro.getProperty(strKey);

				map1.put(strKey, strValue);

			}

		} catch (IOException e) {

			System.out.println("读取配置文件出错");

			return Collections.emptyMap();
		} finally {

			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				System.out.println("关闭异常..");
				e.printStackTrace();
			}
		}

		return map1;
	}

	public static FastdfsFile downloadFile(String filePath) throws Exception {

		/*
		 * connect_timeout=2000 network_timeout=30000 base_path=/root/fastdfs
		 * tracker_server=192.168.60.203:22122 setG_anti_steal_token=false
		 * setG_charset=UTF-8 setG_tracker_http_port=80
		 */
		ClientGlobal.setG_connect_timeout(connect_timeout);
		// 网络超时的时限，单位为毫秒
		ClientGlobal.setG_network_timeout(network_timeout);
		ClientGlobal.setG_anti_steal_token(setG_anti_steal_token);
		// 字符集
		ClientGlobal.setG_charset(setG_charset);

		ClientGlobal.setG_secret_key(null);

		// HTTP访问服务的端口号
		ClientGlobal.setG_tracker_http_port(setG_tracker_http_port);
		// Tracker服务器列表
		String trackerServer = "192.168.60.203:22122";// 获取所有的地址
		InetSocketAddress[] tracker_servers = null;
		String[] serverStrings = trackerServer.split(",");
		tracker_servers = new InetSocketAddress[serverStrings.length];
		for (int i = 0; i < serverStrings.length; i++) {
			String servers = serverStrings[i];
			String[] serverString = servers.split(":");
			String ipString = serverString[0];
			int port = Integer.parseInt(serverString[1]);
			tracker_servers[i] = new InetSocketAddress(ipString, port);
		}
		ClientGlobal.setG_tracker_group(new TrackerGroup(tracker_servers));
		TrackerServer tServer = null;
		FastdfsFile fastdfsFile = null;
		try {
			int index = filePath.indexOf("/");
			if (index < 0) {
				return null;
			}
			String groupName = filePath.substring(0, index);
			String remoteFileName = filePath.substring(index + 1);
			// 建立连接
			TrackerClient tracker = new TrackerClient();
			tServer = tracker.getConnection();
			StorageServer sServer = null;
			StorageClient sClient = new StorageClient(tServer, sServer);
			byte[] file = sClient.download_file(groupName, remoteFileName);
			fastdfsFile = new FastdfsFile(file);
			NameValuePair[] nameValuePairs = sClient.get_metadata(groupName,
					remoteFileName);
			if (nameValuePairs != null) {
				fastdfsFile.setNameValuePairs(nameValuePairs);
			}
			tServer.close();
		} finally {
			if (tServer != null) {
				tServer.close();
			}
		}
		return fastdfsFile;
	}

	/***
	 * 获取上传时保存的Map
	 * 
	 * @param groupName
	 * @param remoteFileName
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> getNaValuePairs(String groupName,
			String remoteFileName) throws Exception {
		NameValuePair[] nameValuePairs = null;
		TrackerServer tServer = null;
		Map<String, String> map = null;
		try {
			// 建立连接
			TrackerClient tracker = new TrackerClient();
			tServer = tracker.getConnection();
			StorageServer sServer = null;
			StorageClient sClient = new StorageClient(tServer, sServer);

			nameValuePairs = sClient.get_metadata(groupName, remoteFileName);
			if (nameValuePairs != null) {
				map = new HashMap<String, String>();
				for (NameValuePair nameValuePair : nameValuePairs) {
					map.put(nameValuePair.getName(), nameValuePair.getValue());
				}
			}
		} finally {
			if (tServer != null) {
				tServer.close();
			}
		}
		return map;
	}

	private static String getInputStr() {

		BufferedReader strin = new BufferedReader(new InputStreamReader(
				System.in));

		String str = null;

		try {

			str = strin.readLine();

		} catch (IOException e) {

			System.out.println("��ȡ����̨��ݳ��,");
			e.printStackTrace();
		}

		return str;
	}

}

class FastdfsFile implements Serializable {
	private static final long serialVersionUID = -996760121932438618L;
	private static final String EXT_FILE_NAME = "extFileName"; // 文件扩展名key
	private static final String USER_ID = "extUserId"; // userid
	private byte[] file;// 上传附件二进制
	private Map<String, String> map = new HashMap<String, String>();

	public FastdfsFile(byte[] file) {
		super();
		this.file = file;
	}

	/**
	 * 
	 * @param file
	 * @param extFileName
	 *            文件扩展名
	 */
	public FastdfsFile(byte[] file, String extFileName) {
		super();
		this.file = file;
		map.put(EXT_FILE_NAME, extFileName);
	}

	/**
	 * 
	 * @param file
	 * @param extFileName
	 *            文件扩展名
	 * @param file
	 *            用户ID
	 */
	public FastdfsFile(byte[] file, String extFileName, String userId) {
		super();
		this.file = file;
		map.put(EXT_FILE_NAME, extFileName);
		map.put(USER_ID, userId);
	}

	/**
	 * 
	 * @param file
	 * @param map
	 *            保存的参数map
	 */
	public FastdfsFile(byte[] file, String extFileName, Map<String, String> map) {
		if (map != null) {
			this.map = map;
		}
		map.put(EXT_FILE_NAME, extFileName);
	}

	/**
	 * 
	 * @return 获取NameValuePair数组
	 */
	public NameValuePair[] getNameValuePairs() {
		NameValuePair[] nameValuePairs = null;
		if (map != null && !map.isEmpty()) {
			nameValuePairs = new NameValuePair[map.size()];
			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) it.next();
				nameValuePairs[i] = new NameValuePair(entry.getKey(), entry
						.getValue());
				i++;
			}
		}
		return nameValuePairs;
	}

	/**
	 * 
	 * @param nameValuePairs
	 *            设置NameValuePair数组
	 */
	public void setNameValuePairs(NameValuePair[] nameValuePairs) {
		if (nameValuePairs != null) {
			for (int i = 0; i < nameValuePairs.length; i++) {
				NameValuePair nameValuePair = nameValuePairs[i];
				map.put(nameValuePair.getName(), nameValuePair.getValue());
			}
		}
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public String getExtFileName() {
		return map.get(EXT_FILE_NAME);
	}

	public void setExtFileName(String extFileName) {
		map.put(EXT_FILE_NAME, extFileName);
	}

	public void getUserId() {
		map.get(USER_ID);
	}

	public void setUserId(String userId) {
		map.put(USER_ID, userId);
	}

}
