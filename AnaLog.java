package analog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 分析log日志 检索错误信息
 * @author KangZheng
 *
 */

public class AnaLog {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String path = System.getProperty("user.dir");
		System.out.println(path);
		String keyword=null;
		while (keyword == null||"".equals(keyword)) {
			System.out.println("输入查询关键字(exit 退出)：");
			Scanner scan = new Scanner(System.in);
			keyword = scan.nextLine();
			if("exit".equals(keyword)){
				System.out.println("bye!");
				System.exit(0);
			}
		}
		
		path = path.replace('\\', '/');
		File dir = new File(path);
		
		BufferedReader reader = null;
		File[] files = dir.listFiles();
		List<String> res=new ArrayList<String>(files.length);
		for (File f : files) {
			if(f.isDirectory()){
				continue;
			}
			System.out.println("当前检查文件："+f.getName());
			try {
				reader = new BufferedReader(new FileReader(f));
				String str = null;
				int lineNum=0;
				int j=0;
				while ((str = reader.readLine()) != null) {
					lineNum++;
					if (str.contains(keyword)) {
						j++;
						if(!res.contains(f.getName())){
							res.add(f.getName());
						}
						System.out.println("-------------------Found--Num "+j+"--------------");
						System.out.println("文件名："+f.getName()+"所在行号："+lineNum);
						System.out.println("-------------------End------------------");
					}
				}
			} catch (FileNotFoundException e) {
				System.out.println(f.getName()+"这个文件未找到!");
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(reader!=null){
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("-----------包含关键字的文件有：----------");
		if(res.size()==0){
			System.out.println("没有");
		}else{
			System.out.println("---------如下-------------");
		}
		for(String o:res){
			System.out.println(o);
		}
		System.out.println("-------------------------------");
		main(null);
	}

}
