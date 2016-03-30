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
 * ����log��־ ����������Ϣ
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
			System.out.println("�����ѯ�ؼ���(exit �˳�)��");
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
			System.out.println("��ǰ����ļ���"+f.getName());
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
						System.out.println("�ļ�����"+f.getName()+"�����кţ�"+lineNum);
						System.out.println("-------------------End------------------");
					}
				}
			} catch (FileNotFoundException e) {
				System.out.println(f.getName()+"����ļ�δ�ҵ�!");
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
		System.out.println("-----------�����ؼ��ֵ��ļ��У�----------");
		if(res.size()==0){
			System.out.println("û��");
		}else{
			System.out.println("---------����-------------");
		}
		for(String o:res){
			System.out.println(o);
		}
		System.out.println("-------------------------------");
		main(null);
	}

}
